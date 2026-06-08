package fr.iolabs.leaf.authentication.oauth;

import org.apache.logging.log4j.util.Strings;

import fr.iolabs.leaf.authentication.oauth.OAuthProviderProperties.ProviderConfig;

/**
 * A {@link AbstractUserInfoOAuthTokenVerifier} built entirely from
 * {@link OAuthProviderProperties} configuration, allowing new userinfo-based
 * OAuth2 providers to be added without writing any Java code.
 */
public class ConfigurableUserInfoTokenVerifier extends AbstractUserInfoOAuthTokenVerifier {

	private final String provider;
	private final ProviderConfig config;

	public ConfigurableUserInfoTokenVerifier(String provider, ProviderConfig config) {
		this.provider = provider;
		this.config = config;
	}

	@Override
	public String getProvider() {
		return this.provider;
	}

	@Override
	protected String getUserInfoUri() {
		return this.config.getUserInfoUri();
	}

	@Override
	protected String getIdClaim() {
		return orDefault(this.config.getIdClaim(), super.getIdClaim());
	}

	@Override
	protected String getEmailClaim() {
		return orDefault(this.config.getEmailClaim(), super.getEmailClaim());
	}

	@Override
	protected String getFirstnameClaim() {
		return orDefault(this.config.getFirstnameClaim(), super.getFirstnameClaim());
	}

	@Override
	protected String getLastnameClaim() {
		return orDefault(this.config.getLastnameClaim(), super.getLastnameClaim());
	}

	@Override
	protected String getNameClaim() {
		return orDefault(this.config.getNameClaim(), super.getNameClaim());
	}

	@Override
	protected String getAvatarClaim() {
		return orDefault(this.config.getAvatarClaim(), super.getAvatarClaim());
	}

	private static String orDefault(String value, String defaultValue) {
		return Strings.isBlank(value) ? defaultValue : value;
	}
}
