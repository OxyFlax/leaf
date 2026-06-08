package fr.iolabs.leaf.authentication.oauth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration-driven declaration of OAuth2 providers exposing a userinfo
 * endpoint.
 *
 * <p>
 * Any OAuth2 provider can be added without writing a single line of Java, just
 * by declaring it under {@code leaf.oauth.providers.<name>} in the application
 * configuration, for example:
 * </p>
 *
 * <pre>
 * leaf:
 *   oauth:
 *     providers:
 *       myprovider:
 *         userInfoUri: "https://example.com/oauth/userinfo"
 *         emailClaim: "email"
 * </pre>
 *
 * <p>
 * Each declared provider is registered in the {@link OAuthProviderRegistry}
 * alongside the built-in providers.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "leaf.oauth")
public class OAuthProviderProperties {

	private Map<String, ProviderConfig> providers = new HashMap<>();

	public Map<String, ProviderConfig> getProviders() {
		return providers;
	}

	public void setProviders(Map<String, ProviderConfig> providers) {
		this.providers = providers;
	}

	/**
	 * Claim mapping and endpoint for a single configuration-declared provider.
	 * Claim names left {@code null} fall back to the standard OIDC defaults.
	 */
	public static class ProviderConfig {
		private String userInfoUri;
		private String idClaim;
		private String emailClaim;
		private String firstnameClaim;
		private String lastnameClaim;
		private String nameClaim;
		private String avatarClaim;

		public String getUserInfoUri() {
			return userInfoUri;
		}

		public void setUserInfoUri(String userInfoUri) {
			this.userInfoUri = userInfoUri;
		}

		public String getIdClaim() {
			return idClaim;
		}

		public void setIdClaim(String idClaim) {
			this.idClaim = idClaim;
		}

		public String getEmailClaim() {
			return emailClaim;
		}

		public void setEmailClaim(String emailClaim) {
			this.emailClaim = emailClaim;
		}

		public String getFirstnameClaim() {
			return firstnameClaim;
		}

		public void setFirstnameClaim(String firstnameClaim) {
			this.firstnameClaim = firstnameClaim;
		}

		public String getLastnameClaim() {
			return lastnameClaim;
		}

		public void setLastnameClaim(String lastnameClaim) {
			this.lastnameClaim = lastnameClaim;
		}

		public String getNameClaim() {
			return nameClaim;
		}

		public void setNameClaim(String nameClaim) {
			this.nameClaim = nameClaim;
		}

		public String getAvatarClaim() {
			return avatarClaim;
		}

		public void setAvatarClaim(String avatarClaim) {
			this.avatarClaim = avatarClaim;
		}
	}
}
