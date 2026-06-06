package fr.iolabs.leaf.authentication.oauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.authentication.oauth.OAuthProviderProperties.ProviderConfig;
import fr.iolabs.leaf.common.errors.BadRequestException;

@Component
public class OAuthProviderRegistry {

private static final Logger logger = LoggerFactory.getLogger(OAuthProviderRegistry.class);

@Autowired
private List<OAuthTokenVerifier> verifiers;

@Autowired
private OAuthProviderProperties providerProperties;

private Map<String, OAuthTokenVerifier> verifierMap;

@PostConstruct
public void init() {
this.verifierMap = new HashMap<>();

// Register built-in / code-defined verifiers (e.g. google, apple, ampeco).
for (OAuthTokenVerifier verifier : this.verifiers) {
this.verifierMap.put(verifier.getProvider().toLowerCase(), verifier);
}

// Register configuration-declared providers, without overriding code-defined ones.
if (this.providerProperties.getProviders() != null) {
for (Map.Entry<String, ProviderConfig> entry : this.providerProperties.getProviders().entrySet()) {
String provider = entry.getKey().toLowerCase();
if (Strings.isBlank(entry.getValue().getUserInfoUri())) {
logger.warn("Skipping OAuth provider '{}': missing userInfoUri", provider);
continue;
}
if (this.verifierMap.containsKey(provider)) {
logger.warn("Ignoring configuration for OAuth provider '{}': a built-in verifier already exists", provider);
continue;
}
this.verifierMap.put(provider, new ConfigurableUserInfoTokenVerifier(provider, entry.getValue()));
}
}
}

public OAuthTokenVerifier getVerifier(String provider) {
if (Strings.isBlank(provider)) {
throw new BadRequestException("Missing OAuth provider");
}
OAuthTokenVerifier verifier = verifierMap.get(provider.toLowerCase());
if (verifier == null) {
throw new BadRequestException("Unsupported OAuth provider: " + provider);
}
return verifier;
}
}
