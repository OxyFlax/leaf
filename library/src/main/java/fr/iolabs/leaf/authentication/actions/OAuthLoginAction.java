package fr.iolabs.leaf.authentication.actions;

public class OAuthLoginAction {
	private String provider;
	private String idToken;
	/**
	 * Display name forwarded by the front-end. Apple only exposes the user name on
	 * the very first sign-in, outside of the ID token, so it cannot be recovered
	 * server side afterwards.
	 */
	private String name;
	private String firstname;
	private String lastname;

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
