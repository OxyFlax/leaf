package fr.iolabs.leaf.authentication.oauth;

import org.springframework.context.event.EventListener;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

/**
 * Base class of the token verifier of one provider.
 *
 * It answers the OAuth events published by {@link OAuthAccountService}, so that
 * supporting a new provider — from the library or from the application using
 * it — only takes one more bean extending this class. An application may also
 * listen to {@link OAuthTokenValidationEvent} and
 * {@link OAuthProviderDiscoveryEvent} directly when this shape does not fit.
 */
public abstract class OAuthTokenVerifier {

	private static final String UNAUTHORIZED_PREFIX = "Unauthorized - ";

	/**
	 * @return the provider name this verifier handles (e.g. "google", "apple"),
	 *         matched case-insensitively
	 */
	public abstract String getProvider();

	/**
	 * @return true when at least one client id has been configured for this
	 *         provider. Unconfigured providers are neither advertised to the
	 *         front-end nor usable to log in.
	 */
	public abstract boolean isConfigured();

	/**
	 * Verifies the ID token and extracts the identity it proves.
	 *
	 * @param idToken the raw ID token from the provider SDK
	 * @return the verified user info
	 * @throws UnauthorizedException if verification fails
	 */
	protected abstract OAuthUserInfo verify(String idToken);

	@EventListener
	public void onProviderDiscovery(OAuthProviderDiscoveryEvent event) {
		if (this.isConfigured()) {
			event.register(this.getProvider());
		}
	}

	@EventListener
	public void onTokenValidation(OAuthTokenValidationEvent event) {
		if (!event.isFor(this.getProvider())) {
			return;
		}
		if (!this.isConfigured()) {
			event.reject(this.getProvider() + " OAuth is not configured");
			return;
		}
		try {
			OAuthUserInfo userInfo = this.verify(event.getIdToken());
			if (userInfo == null || userInfo.getProviderUserId() == null || userInfo.getProviderUserId().isBlank()) {
				event.reject("Could not identify the user from the " + this.getProvider() + " token");
				return;
			}
			event.validate(userInfo);
		} catch (UnauthorizedException e) {
			event.reject(describe(e));
		}
	}

	/** The exception name already carries the HTTP status, which the event does not need. */
	private static String describe(UnauthorizedException e) {
		String name = e.getName();
		if (name != null && name.startsWith(UNAUTHORIZED_PREFIX)) {
			return name.substring(UNAUTHORIZED_PREFIX.length());
		}
		return name;
	}
}
