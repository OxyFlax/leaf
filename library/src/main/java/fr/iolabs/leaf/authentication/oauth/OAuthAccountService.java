package fr.iolabs.leaf.authentication.oauth;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.admin.whitelisting.WhitelistingService;
import fr.iolabs.leaf.authentication.AccountRegistrationEvent;
import fr.iolabs.leaf.authentication.LeafAccountHelper;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.actions.OAuthLoginAction;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.authentication.model.authentication.LeafAccountAuthentication;
import fr.iolabs.leaf.authentication.model.authentication.OAuthIdentity;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;

@Service
public class OAuthAccountService {

	private static final Logger logger = LoggerFactory.getLogger(OAuthAccountService.class);
	private static final int GENERATED_PASSWORD_LENGTH = 16;

	@Autowired
	private LeafAccountRepository accountRepository;
	@Autowired
	private LeafAccountService accountService;
	@Autowired
	private WhitelistingService whitelistingService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private LeafNotificationService notificationService;

	/**
	 * Main entry point for OAuth login/registration. Verifies the ID token, finds
	 * or creates an account, and returns a session token.
	 */
	public OAuthLoginResponse authenticateWithOAuth(String provider, OAuthLoginAction action) {
		OAuthUserInfo userInfo = this.verify(provider, action == null ? null : action.getIdToken());

		// 1. An account already owns this exact provider identity: plain login.
		LeafAccount account = accountRepository.findByOAuthIdentity(
				userInfo.getProvider(), userInfo.getProviderUserId());
		if (account != null) {
			logger.info("OAuth login: existing identity found for provider={} sub={}", userInfo.getProvider(),
					userInfo.getProviderUserId());
			this.refreshIdentity(account, userInfo);
			String sessionToken = accountService.createSessionAndCookie(account);
			accountRepository.save(account);
			return new OAuthLoginResponse(sessionToken, false, userInfo.getProvider());
		}

		// 2. An account already uses this e-mail address: attach the identity to it.
		//
		// This is only safe when the provider certifies that the user owns the
		// address, otherwise declaring somebody else's e-mail on the provider side
		// would be enough to take over their account.
		if (userInfo.isEmailVerified() && Strings.isNotBlank(userInfo.getEmail())) {
			account = accountRepository.findAccountByEmail(userInfo.getEmail().toLowerCase());
		}
		if (account != null) {
			logger.info("OAuth login: linking {} identity to existing account with email={}", userInfo.getProvider(),
					userInfo.getEmail());
			account.getAuthentication().addOAuthIdentityIfAbsent(this.toIdentity(userInfo));
			this.markEmailAsVerified(account, userInfo);
			account.getMetadata().updateLastModification();
			String sessionToken = accountService.createSessionAndCookie(account);
			accountRepository.save(account);
			return new OAuthLoginResponse(sessionToken, false, userInfo.getProvider());
		}

		// 3. Nothing matched: register a brand new account.
		logger.info("OAuth login: creating new account for provider={} email={}", userInfo.getProvider(),
				userInfo.getEmail());
		account = this.createAccountFromOAuth(userInfo, action);

		applicationEventPublisher.publishEvent(new AccountRegistrationEvent(this, account));

		account = accountRepository.save(account);
		// The session can only be created once the account has been saved, as it needs
		// the generated account id.
		String sessionToken = accountService.createSessionAndCookie(account);
		account = accountRepository.save(account);

		this.notificationService.emit(
				LeafNotification.of("LEAF_ACCOUNT_REGISTRATION", account.getId(), account.toMap()));

		return new OAuthLoginResponse(sessionToken, true, userInfo.getProvider());
	}

	/**
	 * Attaches a provider identity to an already authenticated account, so that the
	 * user can sign in with it later on.
	 */
	public LeafAccount linkProvider(LeafAccount account, String provider, OAuthLoginAction action) {
		OAuthUserInfo userInfo = this.verify(provider, action == null ? null : action.getIdToken());

		LeafAccount ownerOfIdentity = accountRepository.findByOAuthIdentity(
				userInfo.getProvider(), userInfo.getProviderUserId());
		if (ownerOfIdentity != null && !ownerOfIdentity.getId().equals(account.getId())) {
			throw new BadRequestException(
					"This " + userInfo.getProvider() + " account is already linked to another account");
		}

		account.getAuthentication().addOAuthIdentityIfAbsent(this.toIdentity(userInfo));
		this.markEmailAsVerified(account, userInfo);
		account.getMetadata().updateLastModification();
		return accountRepository.save(account);
	}

	/**
	 * Detaches a provider identity from an account. Refused when it would leave the
	 * account without any way to sign in.
	 */
	public LeafAccount unlinkProvider(LeafAccount account, String provider) {
		LeafAccountAuthentication authentication = account.getAuthentication();
		if (authentication.findOAuthIdentity(provider).isEmpty()) {
			throw new BadRequestException("No " + provider + " identity linked to this account");
		}
		boolean isLastSignInMethod = authentication.isPasswordless() && authentication.countOAuthIdentities() <= 1;
		if (isLastSignInMethod) {
			throw new BadRequestException(
					"Cannot unlink the last sign-in method, define a password on the account first");
		}

		authentication.removeOAuthIdentity(provider);
		account.getMetadata().updateLastModification();
		return accountRepository.save(account);
	}

	public List<OAuthIdentity> listIdentities(LeafAccount account) {
		List<OAuthIdentity> identities = account.getAuthentication().getOauthIdentities();
		return identities == null ? List.of() : identities;
	}

	/**
	 * Asks every verifier whether it is configured. Providers are discovered through
	 * an event so that the application can contribute its own.
	 */
	public List<String> listEnabledProviders() {
		OAuthProviderDiscoveryEvent event = new OAuthProviderDiscoveryEvent(this);
		this.applicationEventPublisher.publishEvent(event);
		return event.enabledProviders();
	}

	/**
	 * Hands the token to the verifiers through an event: the one in charge of the
	 * provider validates it (or not), the others ignore it.
	 */
	private OAuthUserInfo verify(String provider, String idToken) {
		if (provider == null || provider.isBlank()) {
			throw new BadRequestException("Missing OAuth provider");
		}

		OAuthTokenValidationEvent event = new OAuthTokenValidationEvent(this, provider, idToken);
		this.applicationEventPublisher.publishEvent(event);

		if (!event.isProviderSupported()) {
			throw new BadRequestException("Unsupported OAuth provider: " + provider);
		}
		if (!event.isValidated() || event.getUserInfo() == null) {
			throw new UnauthorizedException(
					event.getError() != null ? event.getError() : provider + " token verification failed");
		}
		return event.getUserInfo();
	}

	private OAuthIdentity toIdentity(OAuthUserInfo userInfo) {
		return new OAuthIdentity(userInfo.getProvider(), userInfo.getProviderUserId(), userInfo.getEmail());
	}

	/**
	 * The e-mail declared on the provider side can change over time whereas the
	 * identity itself never does: keep the stored copy current.
	 */
	private void refreshIdentity(LeafAccount account, OAuthUserInfo userInfo) {
		account.getAuthentication().addOAuthIdentityIfAbsent(this.toIdentity(userInfo));
	}

	/**
	 * The provider vouches for the e-mail address, so the account does not have to
	 * go through our own e-mail verification when both addresses match.
	 */
	private void markEmailAsVerified(LeafAccount account, OAuthUserInfo userInfo) {
		if (!userInfo.isEmailVerified() || Strings.isBlank(userInfo.getEmail())) {
			return;
		}
		if (account.getEmail() == null || !account.getEmail().equalsIgnoreCase(userInfo.getEmail())) {
			return;
		}
		if (account.getAccountVerification() != null && !account.getAccountVerification().isEmailVerified()) {
			account.getAccountVerification().setEmailVerified(true);
		}
	}

	private LeafAccount createAccountFromOAuth(OAuthUserInfo userInfo, OAuthLoginAction action) {
		if (Strings.isBlank(userInfo.getEmail())) {
			throw new BadRequestException("The " + userInfo.getProvider()
					+ " account does not expose any e-mail address, registration is not possible");
		}

		String email = userInfo.getEmail().toLowerCase();

		// Only reachable when the provider did not certify the address, otherwise the
		// identity would already have been attached to that account. Registering
		// anyway would silently create a second account sharing the same e-mail.
		if (this.accountRepository.findAccountByEmail(email) != null) {
			throw new BadRequestException("An account already uses this e-mail address, sign in with it to link "
					+ userInfo.getProvider());
		}

		if (this.whitelistingService.enabled() && this.whitelistingService.isEmailAllowed(email)) {
			throw new UnauthorizedException();
		}

		LeafAccount account = new LeafAccount();
		account.setMetadata(ResourceMetadata.create());
		account.setEmail(email);

		this.fillProfileFromOAuth(account, userInfo, action);
		this.markEmailAsVerified(account, userInfo);

		LeafAccountAuthentication auth = account.getAuthentication();
		// The account is only reachable through its OAuth identities: a random
		// password is stored so that no empty hash can ever be matched, and the
		// account is flagged as passwordless so the user can be offered to define a
		// real one.
		auth.setPassword(LeafAccountHelper.generateComplexPassword(GENERATED_PASSWORD_LENGTH));
		auth.hashPassword();
		auth.setPasswordless(true);
		auth.addOAuthIdentityIfAbsent(this.toIdentity(userInfo));

		return account;
	}

	/**
	 * Pre-fills the profile of a brand new account. This is the only moment the
	 * provider data reaches the profile: later sign-ins never touch it, so the user
	 * stays in control of what they edit afterwards. It is also the only moment
	 * Apple ever discloses the user name, which the front-end forwards in the
	 * action.
	 */
	private void fillProfileFromOAuth(LeafAccount account, OAuthUserInfo userInfo, OAuthLoginAction action) {
		String firstname = firstNonBlank(action == null ? null : action.getFirstname(), userInfo.getFirstname());
		String lastname = firstNonBlank(action == null ? null : action.getLastname(), userInfo.getLastname());

		if (Strings.isNotBlank(firstname)) {
			account.getProfile().setFirstname(firstname);
		}
		if (Strings.isNotBlank(lastname)) {
			account.getProfile().setLastname(lastname);
		}
		if (Strings.isNotBlank(userInfo.getAvatarUrl())) {
			account.getProfile().setAvatarUrl(userInfo.getAvatarUrl());
		}
		String username = this.resolveDisplayName(userInfo, action, firstname, lastname);
		if (username != null) {
			account.getProfile().setUsername(username);
		}
	}

	private String resolveDisplayName(OAuthUserInfo userInfo, OAuthLoginAction action, String firstname,
			String lastname) {
		String providedName = action == null ? null : action.getName();
		if (Strings.isNotBlank(providedName)) {
			return providedName.trim();
		}
		String composedName = ((Strings.isNotBlank(firstname) ? firstname : "") + " "
				+ (Strings.isNotBlank(lastname) ? lastname : "")).trim();
		if (Strings.isNotBlank(composedName)) {
			return composedName;
		}
		return Strings.isNotBlank(userInfo.getEmail()) ? userInfo.getEmail() : null;
	}

	private static String firstNonBlank(String first, String second) {
		return Strings.isNotBlank(first) ? first : second;
	}
}
