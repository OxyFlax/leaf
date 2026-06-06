package fr.iolabs.leaf.authentication.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OAuth2 provider for the AMPECO EV charging platform.
 *
 * <p>
 * AMPECO follows the standard OAuth2 / OpenID Connect authorization code flow:
 * the frontend obtains an access token which is forwarded to this backend and
 * exchanged for the user profile through AMPECO's userinfo endpoint.
 * </p>
 *
 * <p>
 * Because every AMPECO customer is hosted on its own tenant domain, the
 * userinfo endpoint is configurable through {@code leaf.oauth.ampeco.userInfoUri}.
 * </p>
 */
@Component
public class AmpecoTokenVerifier extends AbstractUserInfoOAuthTokenVerifier {

	@Value("${leaf.oauth.ampeco.userInfoUri:}")
	private String userInfoUri;

	@Override
	public String getProvider() {
		return "ampeco";
	}

	@Override
	protected String getUserInfoUri() {
		return this.userInfoUri;
	}
}
