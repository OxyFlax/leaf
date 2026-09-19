package fr.iolabs.leaf.authentication.oauth;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import fr.iolabs.leaf.common.errors.UnauthorizedException;

@Component
public class AppleTokenVerifier extends OAuthTokenVerifier {

	private static final Logger logger = LoggerFactory.getLogger(AppleTokenVerifier.class);

	public static final String PROVIDER = "apple";

	private static final String APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys";
	private static final String APPLE_ISSUER = "https://appleid.apple.com";

	/**
	 * One or several comma separated Apple client ids: the Services ID used by the
	 * web front-end and/or the bundle ids of the native applications.
	 */
	@Value("${leaf.oauth.apple.clientId:}")
	private String clientId;

	private JwkProvider jwkProvider;

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
			throw new UnauthorizedException("Apple OAuth is not configured");
		}
		if (idToken == null || idToken.isBlank()) {
			throw new UnauthorizedException("Missing Apple ID token");
		}
		try {
			DecodedJWT decodedJWT = JWT.decode(idToken);
			String kid = decodedJWT.getKeyId();

			Jwk jwk = this.getJwkProvider().get(kid);
			RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

			Algorithm algorithm = Algorithm.RSA256(publicKey, null);
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer(APPLE_ISSUER)
					.withAnyOfAudience(audiences.toArray(new String[0]))
					.build();

			DecodedJWT verifiedJWT = verifier.verify(idToken);

			OAuthUserInfo info = new OAuthUserInfo();
			info.setProvider(PROVIDER);
			info.setProviderUserId(verifiedJWT.getSubject());
			info.setEmail(verifiedJWT.getClaim("email").asString());
			info.setEmailVerified(this.readBooleanClaim(verifiedJWT.getClaim("email_verified")));
			// Apple does not include the name in the ID token: it is only given once, by
			// the provider SDK, on the very first sign-in, and forwarded by the front-end
			// through the login action.
			return info;
		} catch (UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Apple token verification failed", e);
			throw new UnauthorizedException("Apple token verification failed");
		}
	}

	/**
	 * Apple serializes the booleans of the ID token either as real booleans or as
	 * strings, depending on the claim and on the platform the token was issued for.
	 */
	private boolean readBooleanClaim(Claim claim) {
		if (claim == null || claim.isNull()) {
			return false;
		}
		Boolean asBoolean = claim.asBoolean();
		if (asBoolean != null) {
			return asBoolean;
		}
		return "true".equalsIgnoreCase(claim.asString());
	}

	private synchronized JwkProvider getJwkProvider() {
		if (this.jwkProvider == null) {
			try {
				// Apple keys rotate slowly: caching them avoids hitting the JWKS endpoint on
				// every single sign-in, and the rate limit protects us from a burst of
				// requests carrying unknown key ids.
				this.jwkProvider = new JwkProviderBuilder(APPLE_JWKS_URL)
						.cached(10, 24, TimeUnit.HOURS)
						.rateLimited(10, 1, TimeUnit.MINUTES)
						.build();
			} catch (Exception e) {
				logger.error("Failed to initialize Apple JWKS provider", e);
				throw new UnauthorizedException("Failed to initialize Apple JWKS provider");
			}
		}
		return this.jwkProvider;
	}
}
