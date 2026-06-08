package fr.iolabs.leaf.authentication.oauth;

import org.apache.logging.log4j.util.Strings;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

/**
 * Reusable base class for any OAuth2 provider that exposes an OpenID Connect
 * style "userinfo" endpoint.
 *
 * <p>
 * The frontend sends the provider access token (carried by the
 * {@code idToken} field of the login action). This verifier calls the
 * configured userinfo endpoint, sending that token in the Authorization
 * header, then maps the returned JSON claims onto a {@link OAuthUserInfo}.
 * </p>
 *
 * <p>
 * To add a new userinfo-based provider, subclass this class (or declare it
 * through configuration, see {@link OAuthProviderProperties}) and provide the
 * provider name and userinfo endpoint. Claim names can be overridden when a
 * provider does not follow the standard OIDC claim naming.
 * </p>
 */
public abstract class AbstractUserInfoOAuthTokenVerifier implements OAuthTokenVerifier {

	private static final Logger logger = LoggerFactory.getLogger(AbstractUserInfoOAuthTokenVerifier.class);

	/**
	 * @return the userinfo endpoint URL to call with the access token, or
	 *         {@code null}/blank when the provider is not configured.
	 */
	protected abstract String getUserInfoUri();

	protected String getIdClaim() {
		return "sub";
	}

	protected String getEmailClaim() {
		return "email";
	}

	protected String getFirstnameClaim() {
		return "given_name";
	}

	protected String getLastnameClaim() {
		return "family_name";
	}

	protected String getNameClaim() {
		return "name";
	}

	protected String getAvatarClaim() {
		return "picture";
	}

	@Override
	public OAuthUserInfo verify(String accessToken) {
		String userInfoUri = this.getUserInfoUri();
		if (Strings.isBlank(userInfoUri)) {
			throw new UnauthorizedException(this.getProvider() + " OAuth is not configured");
		}
		if (Strings.isBlank(accessToken)) {
			throw new UnauthorizedException("Missing " + this.getProvider() + " access token");
		}

		JSONObject claims = this.fetchUserInfo(userInfoUri, accessToken);

		String providerUserId = this.readClaim(claims, this.getIdClaim());
		if (Strings.isBlank(providerUserId)) {
			throw new UnauthorizedException("Invalid " + this.getProvider() + " access token");
		}

		OAuthUserInfo info = new OAuthUserInfo();
		info.setProvider(this.getProvider());
		info.setProviderUserId(providerUserId);
		info.setEmail(this.readClaim(claims, this.getEmailClaim()));
		info.setAvatarUrl(this.readClaim(claims, this.getAvatarClaim()));

		String firstname = this.readClaim(claims, this.getFirstnameClaim());
		String lastname = this.readClaim(claims, this.getLastnameClaim());
		if (Strings.isBlank(firstname) && Strings.isBlank(lastname)) {
			String fullName = this.readClaim(claims, this.getNameClaim());
			if (!Strings.isBlank(fullName)) {
				int separatorIndex = fullName.indexOf(' ');
				if (separatorIndex > 0) {
					firstname = fullName.substring(0, separatorIndex).trim();
					lastname = fullName.substring(separatorIndex + 1).trim();
				} else {
					firstname = fullName.trim();
				}
			}
		}
		info.setFirstname(firstname);
		info.setLastname(lastname);

		return info;
	}

	private JSONObject fetchUserInfo(String userInfoUri, String accessToken) {
		try {
			HttpResponse<JsonNode> response = Unirest.get(userInfoUri)
					.header("Authorization", "Bearer " + accessToken)
					.header("Accept", "application/json")
					.asJson();

			if (response.getStatus() < 200 || response.getStatus() >= 300) {
				logger.warn("{} userinfo endpoint returned status {}", this.getProvider(), response.getStatus());
				throw new UnauthorizedException(this.getProvider() + " token verification failed");
			}

			JsonNode body = response.getBody();
			if (body == null) {
				throw new UnauthorizedException(this.getProvider() + " token verification failed");
			}

			JSONObject object = body.getObject();
			// Some providers wrap the claims inside a top level "data" object.
			if (object != null && !object.has(this.getIdClaim()) && object.optJSONObject("data") != null) {
				object = object.getJSONObject("data");
			}
			if (object == null) {
				throw new UnauthorizedException(this.getProvider() + " token verification failed");
			}
			return object;
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("{} token verification failed", this.getProvider(), e);
			throw new UnauthorizedException(this.getProvider() + " token verification failed");
		}
	}

	private String readClaim(JSONObject claims, String claimName) {
		if (claims == null || Strings.isBlank(claimName) || claims.isNull(claimName)) {
			return null;
		}
		String value = claims.optString(claimName, null);
		return Strings.isBlank(value) ? null : value;
	}
}
