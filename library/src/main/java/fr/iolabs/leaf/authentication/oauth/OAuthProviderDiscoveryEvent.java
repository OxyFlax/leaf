package fr.iolabs.leaf.authentication.oauth;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.context.ApplicationEvent;

/**
 * Published to learn which providers can currently be signed in with. Every
 * configured verifier registers its provider name, so the front-end only ever
 * offers buttons that can work.
 */
public class OAuthProviderDiscoveryEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private final Set<String> enabledProviders = new TreeSet<>();

	public OAuthProviderDiscoveryEvent(Object source) {
		super(source);
	}

	public void register(String provider) {
		if (provider != null && !provider.isBlank()) {
			this.enabledProviders.add(provider.trim().toLowerCase(Locale.ROOT));
		}
	}

	public List<String> enabledProviders() {
		return List.copyOf(this.enabledProviders);
	}
}
