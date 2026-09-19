package fr.iolabs.leaf.authentication.oauth;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A provider is often declared several times for the same application: one
 * client id for the web front-end, one for the iOS application, one for the
 * Android one... All of them issue valid ID tokens for our users, so every
 * declared client id has to be accepted as an audience.
 */
public final class OAuthClientIds {

	private OAuthClientIds() {
	}

	/**
	 * Splits a configuration value holding one or several comma separated client
	 * ids.
	 *
	 * @param configuredClientIds raw configuration value, may be null or blank
	 * @return the declared client ids, never null, without blank entries
	 */
	public static List<String> parse(String configuredClientIds) {
		if (configuredClientIds == null || configuredClientIds.isBlank()) {
			return List.of();
		}
		return Arrays.stream(configuredClientIds.split(","))
				.map(String::trim)
				.filter(clientId -> !clientId.isEmpty())
				.collect(Collectors.toList());
	}
}
