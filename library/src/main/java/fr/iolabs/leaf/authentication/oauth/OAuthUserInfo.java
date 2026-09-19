package fr.iolabs.leaf.authentication.oauth;

public class OAuthUserInfo {
	private String provider;
	private String providerUserId;
	private String email;
	private boolean emailVerified;
	private String firstname;
	private String lastname;
	private String avatarUrl;

	public OAuthUserInfo() {
	}

	public OAuthUserInfo(String provider, String providerUserId, String email, boolean emailVerified, String firstname,
			String lastname, String avatarUrl) {
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.email = email;
		this.emailVerified = emailVerified;
		this.firstname = firstname;
		this.lastname = lastname;
		this.avatarUrl = avatarUrl;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return true when the provider guarantees that the user owns
	 *         {@link #getEmail()}. Attaching an OAuth identity to a pre-existing
	 *         account is only safe when this is true, otherwise anybody able to
	 *         declare an arbitrary e-mail address on the provider side could take
	 *         over an existing account.
	 */
	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
