package dev.eden.client.update;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record GitHubRelease(
	String tagName,
	String name,
	boolean draft,
	boolean prerelease,
	List<Asset> assets
) {
	public Optional<Asset> findJarAsset() {
		return assets.stream()
			.filter(asset -> asset.name().toLowerCase().endsWith(".jar"))
			.filter(asset -> !asset.name().toLowerCase().contains("sources"))
			.filter(asset -> asset.name().toLowerCase().startsWith("eden-"))
			.findFirst();
	}

	public String displayVersion() {
		if (tagName != null && !tagName.isBlank()) {
			return tagName;
		}
		return name == null ? "" : name;
	}

	public record Asset(String name, URI downloadUri, long size) {
	}
}
