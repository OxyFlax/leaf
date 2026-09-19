package fr.iolabs.leaf.authentication.oauth;

/**
 * Session token issued after an OAuth sign-in. It extends the shape of
 * {@link fr.iolabs.leaf.authentication.model.JWT} with the information the
 * front-end needs to tell a registration from a plain login: a brand new
 * account has to go through the post-registration flow (sponsoring,
 * onboarding redirection...) instead of the plain login one.
 */
public class OAuthLoginResponse {

	private String token;
	private boolean created;
	private String provider;

	public OAuthLoginResponse() {
	}

	public OAuthLoginResponse(String token, boolean created, String provider) {
		this.token = token;
		this.created = created;
		this.provider = provider;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
}
