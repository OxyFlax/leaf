package fr.iolabs.leaf.authentication.oauth;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Component
public class GoogleTokenVerifier extends OAuthTokenVerifier {

	private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);

	public static final String PROVIDER = "google";

	/**
	 * One or several comma separated Google client ids (web, iOS, Android...).
	 */
	@Value("${leaf.oauth.google.clientId:}")
	private String clientId;

	private GoogleIdTokenVerifier verifier;

	@Override
	public String getProvider() {
		return PROVIDER;
	}

	@Override
	public boolean isConfigured() {
		return !OAuthClientIds.parse(this.clientId).isEmpty();
	}

	@Override
	protected OAuthUserInfo verify(String idToken) {
		List<String> audiences = OAuthClientIds.parse(this.clientId);
		if (audiences.isEmpty()) {
			throw new UnauthorizedException("Google OAuth is not configured");
		}
		if (idToken == null || idToken.isBlank()) {
			throw new UnauthorizedException("Missing Google ID token");
		}
		try {
			// Checks the signature against Google public keys, the issuer, the audience
			// and the expiration date.
			GoogleIdToken googleIdToken = this.getVerifier(audiences).verify(idToken);
			if (googleIdToken == null) {
				throw new UnauthorizedException("Invalid Google ID token");
			}

			GoogleIdToken.Payload payload = googleIdToken.getPayload();

			OAuthUserInfo info = new OAuthUserInfo();
			info.setProvider(PROVIDER);
			// Warning from Google documentation: Don't use email address as an identifier
			// because a Google Account can have multiple email addresses at different
			// points in time. Always use the "sub" field as the identifier for the user.
			info.setProviderUserId(payload.getSubject());
			info.setEmail(payload.getEmail());
			info.setEmailVerified(Boolean.TRUE.equals(payload.getEmailVerified()));
			info.setFirstname((String) payload.get("given_name"));
			info.setLastname((String) payload.get("family_name"));
			info.setAvatarUrl((String) payload.get("picture"));
			return info;
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Google token verification failed", e);
			throw new UnauthorizedException("Google token verification failed");
		}
	}

	private synchronized GoogleIdTokenVerifier getVerifier(List<String> audiences) {
		if (this.verifier == null) {
			this.verifier = new GoogleIdTokenVerifier.Builder(
					new NetHttpTransport(), GsonFactory.getDefaultInstance())
					.setAudience(audiences)
					.build();
		}
		return this.verifier;
	}
}
