package fr.iolabs.leaf.authentication.oauth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import fr.iolabs.leaf.authentication.model.authentication.OAuthIdentity;

/**
 * What the account owner is allowed to see about one of their linked social
 * identities. The provider user id is deliberately left out: it is an internal
 * correlation key and never has to reach a browser.
 */
public class OAuthIdentitySummary {

	private String provider;
	private String email;
	private LocalDateTime linkedAt;

	public OAuthIdentitySummary() {
	}

	public OAuthIdentitySummary(OAuthIdentity identity) {
		this.provider = identity.getProvider();
		this.email = identity.getEmail();
		this.linkedAt = identity.getLinkedAt();
	}

	public static List<OAuthIdentitySummary> of(List<OAuthIdentity> identities) {
		if (identities == null) {
			return List.of();
		}
		return identities.stream().map(OAuthIdentitySummary::new).collect(Collectors.toList());
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getLinkedAt() {
		return linkedAt;
	}

	public void setLinkedAt(LocalDateTime linkedAt) {
		this.linkedAt = linkedAt;
	}
}
