package fr.iolabs.leaf.authentication.oauth;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

/**
 * Published when a client presents an ID token issued by a social provider.
 *
 * The listener in charge of that provider verifies the token and, on success,
 * hands the identity it proves back through {@link #validate(OAuthUserInfo)}.
 * A provider no listener claims is unsupported, a claimed but refused token is
 * unauthorized. Applications can therefore support additional providers by
 * registering their own listener, without touching the library.
 */
public class OAuthTokenValidationEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private final String provider;
	private final String idToken;

	private boolean providerSupported = false;
	/** False by default, true once a listener verified the token. */
	private boolean validated = false;
	private OAuthUserInfo userInfo;
	private String error;

	public OAuthTokenValidationEvent(Object source, String provider, String idToken) {
		super(source);
		this.provider = normalize(provider);
		this.idToken = idToken;
	}

	public String getProvider() {
		return provider;
	}

	public String getIdToken() {
		return idToken;
	}

	/** Case-insensitive check listeners use to pick the events meant for them. */
	public boolean isFor(String provider) {
		return this.provider != null && this.provider.equals(normalize(provider));
	}

	public boolean isProviderSupported() {
		return providerSupported;
	}

	public boolean isValidated() {
		return validated;
	}

	public OAuthUserInfo getUserInfo() {
		return userInfo;
	}

	public String getError() {
		return error;
	}

	/** Marks the token as verified and carries the identity it proves. */
	public void validate(OAuthUserInfo userInfo) {
		this.providerSupported = true;
		this.validated = true;
		this.userInfo = userInfo;
		this.error = null;
	}

	/** Claims the provider but refuses the token. A prior validation always wins. */
	public void reject(String error) {
		this.providerSupported = true;
		if (!this.validated) {
			this.error = error;
		}
	}

	private static String normalize(String provider) {
		return provider == null ? null : provider.trim().toLowerCase(Locale.ROOT);
	}
}
