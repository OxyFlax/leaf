package fr.iolabs.leaf.authentication.model.authentication;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public class OAuthIdentity {

	private String provider;
	private String providerUserId;
	private String email;
	private LocalDateTime linkedAt;

	public OAuthIdentity() {
	}

	public OAuthIdentity(String provider, String providerUserId, String email) {
		this.provider = normalizeProvider(provider);
		this.providerUserId = providerUserId;
		this.email = normalizeEmail(email);
		this.linkedAt = LocalDateTime.now();
	}

	public OAuthIdentity(OAuthIdentity from) {
		this.provider = from.provider;
		this.providerUserId = from.providerUserId;
		this.email = from.email;
		this.linkedAt = from.linkedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OAuthIdentity that = (OAuthIdentity) o;
		return Objects.equals(provider, that.provider) && Objects.equals(providerUserId, that.providerUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(provider, providerUserId);
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = normalizeProvider(provider);
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = normalizeEmail(email);
	}

	public LocalDateTime getLinkedAt() {
		return linkedAt;
	}

	public void setLinkedAt(LocalDateTime linkedAt) {
		this.linkedAt = linkedAt;
	}

	private static String normalizeProvider(String provider) {
		return provider != null ? provider.toLowerCase(Locale.ROOT) : null;
	}

	private static String normalizeEmail(String email) {
		return email != null ? email.toLowerCase(Locale.ROOT) : null;
	}
}
