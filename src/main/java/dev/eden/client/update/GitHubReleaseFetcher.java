package dev.eden.client.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class GitHubReleaseFetcher {
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

	private final HttpClient client;
	private final Logger logger;

	public GitHubReleaseFetcher(HttpClient client, Logger logger) {
		this.client = client;
		this.logger = logger;
	}

	public CompletableFuture<Optional<GitHubRelease>> fetchLatestRelease(Executor executor) {
		URI uri = URI.create("https://api.github.com/repos/%s/releases/latest".formatted(UpdaterConfig.GITHUB_REPOSITORY));

		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(REQUEST_TIMEOUT)
			.header("Accept", "application/vnd.github+json")
			.header("X-GitHub-Api-Version", "2022-11-28")
			.header("User-Agent", "eden-self-updater")
			.GET()
			.build();

		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApplyAsync(this::parseResponse, executor)
			.exceptionally(throwable -> {
				return Optional.empty();
			});
	}

	private Optional<GitHubRelease> parseResponse(HttpResponse<String> response) {
		int statusCode = response.statusCode();
		if (statusCode == 403 || statusCode == 429) {
			return Optional.empty();
		}

		if (statusCode < 200 || statusCode >= 300) {
			return Optional.empty();
		}

		try {
			JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
			boolean draft = getBoolean(root, "draft");
			boolean prerelease = getBoolean(root, "prerelease");
			String tagName = getString(root, "tag_name");
			String name = getString(root, "name");
			List<GitHubRelease.Asset> assets = parseAssets(root.getAsJsonArray("assets"));

			return Optional.of(new GitHubRelease(tagName, name, draft, prerelease, assets));
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
	}

	private static List<GitHubRelease.Asset> parseAssets(JsonArray assetsJson) {
		List<GitHubRelease.Asset> assets = new ArrayList<>();
		if (assetsJson == null) {
			return assets;
		}

		for (JsonElement element : assetsJson) {
			if (!element.isJsonObject()) {
				continue;
			}

			JsonObject assetJson = element.getAsJsonObject();
			String name = getString(assetJson, "name");
			String downloadUrl = getString(assetJson, "browser_download_url");
			long size = getLong(assetJson, "size");

			if (name.isBlank() || downloadUrl.isBlank()) {
				continue;
			}

			assets.add(new GitHubRelease.Asset(name, URI.create(downloadUrl), size));
		}

		return assets;
	}

	private static String getString(JsonObject object, String name) {
		JsonElement element = object.get(name);
		return element == null || element.isJsonNull() ? "" : element.getAsString();
	}

	private static boolean getBoolean(JsonObject object, String name) {
		JsonElement element = object.get(name);
		return element != null && !element.isJsonNull() && element.getAsBoolean();
	}

	private static long getLong(JsonObject object, String name) {
		JsonElement element = object.get(name);
		return element == null || element.isJsonNull() ? -1L : element.getAsLong();
	}
}
