package dev.eden.client.update;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class DownloadUtility {
	private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);

	private final HttpClient client;
	private final Logger logger;

	public DownloadUtility(HttpClient client, Logger logger) {
		this.client = client;
		this.logger = logger;
	}

	public CompletableFuture<Boolean> download(GitHubRelease.Asset asset, Path destination, Executor executor) {
		HttpRequest request = HttpRequest.newBuilder(asset.downloadUri())
			.timeout(DOWNLOAD_TIMEOUT)
			.header("Accept", "application/octet-stream")
			.header("User-Agent", "eden-self-updater")
			.GET()
			.build();

		return prepareDestination(destination, executor)
			.thenComposeAsync(ignored -> client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination)), executor)
			.thenApplyAsync(response -> verifyDownload(response, destination, asset), executor)
			.exceptionally(throwable -> {
				deleteQuietly(destination);
				return false;
			});
	}

	private CompletableFuture<Void> prepareDestination(Path destination, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(destination.getParent());
				Files.deleteIfExists(destination);
			} catch (IOException exception) {
				throw new IllegalStateException("Could not prepare download destination", exception);
			}
		}, executor);
	}

	private boolean verifyDownload(HttpResponse<Path> response, Path destination, GitHubRelease.Asset asset) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			deleteQuietly(destination);
			return false;
		}

		try {
			long downloadedSize = Files.size(destination);
			if (downloadedSize <= 0L) {
				deleteQuietly(destination);
				return false;
			}

			if (asset.size() >= 0L && downloadedSize != asset.size()) {
				deleteQuietly(destination);
				return false;
			}

			return true;
		} catch (IOException exception) {
			deleteQuietly(destination);
			return false;
		}
	}

	private void deleteQuietly(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException exception) {
		}
	}
}
