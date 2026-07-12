package dev.eden.client.update;

import dev.eden.client.EdenConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateManager {
	private static final long INITIAL_CHECK_DELAY_SECONDS = 5L;

	private final Logger logger;
	private final ScheduledExecutorService scheduler;
	private final ExecutorService workerExecutor;
	private final GitHubReleaseFetcher releaseFetcher;
	private final DownloadUtility downloadUtility;
	private final SafeReplacementLogic replacementLogic;
	private final AtomicBoolean checkInProgress = new AtomicBoolean();

	private UpdateManager(
		Logger logger,
		ScheduledExecutorService scheduler,
		ExecutorService workerExecutor,
		GitHubReleaseFetcher releaseFetcher,
		DownloadUtility downloadUtility,
		SafeReplacementLogic replacementLogic
	) {
		this.logger = logger;
		this.scheduler = scheduler;
		this.workerExecutor = workerExecutor;
		this.releaseFetcher = releaseFetcher;
		this.downloadUtility = downloadUtility;
		this.replacementLogic = replacementLogic;
	}

	public static UpdateManager create(Logger logger) {
		ExecutorService workerExecutor = Executors.newFixedThreadPool(2, daemonThreadFactory("updater-worker"));
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory("updater-scheduler"));
		HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.executor(workerExecutor)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

		return new UpdateManager(
			logger,
			scheduler,
			workerExecutor,
			new GitHubReleaseFetcher(httpClient, logger),
			new DownloadUtility(httpClient, logger),
			new SafeReplacementLogic(logger)
		);
	}

	public void start() {
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "eden-updater-shutdown"));
		if (UpdaterConfig.GITHUB_REPOSITORY.isBlank()) {
			return;
		}
		scheduler.scheduleWithFixedDelay(
			this::scheduleCheck,
			INITIAL_CHECK_DELAY_SECONDS,
			UpdaterConfig.CHECK_INTERVAL_MINUTES,
			TimeUnit.MINUTES
		);
	}

	private void scheduleCheck() {
		if (!isEnabled() || UpdaterConfig.GITHUB_REPOSITORY.isBlank()) {
			return;
		}
		if (!checkInProgress.compareAndSet(false, true)) {
			return;
		}

		checkForUpdates()
			.exceptionally(throwable -> {
				return null;
			})
			.whenComplete((ignored, throwable) -> checkInProgress.set(false));
	}

	public boolean isEnabled() {
		return EdenConfig.INSTANCE.isAutoUpdateEnabled();
	}

	public void setEnabled(boolean enabled) {
		EdenConfig.INSTANCE.setAutoUpdateEnabled(enabled);
		if (enabled) {
			scheduler.execute(this::scheduleCheck);
		}
	}

	private CompletableFuture<Void> checkForUpdates() {
		if (replacementLogic.hasPendingUpdate()) {
			return CompletableFuture.completedFuture(null);
		}

		Optional<java.nio.file.Path> runningJar = replacementLogic.findRunningModJar();
		if (runningJar.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}

		String currentVersion = currentVersion();
		return releaseFetcher.fetchLatestRelease(workerExecutor)
			.thenCompose(release -> release
				.map(latestRelease -> handleRelease(latestRelease, currentVersion, runningJar.get()))
				.orElseGet(() -> CompletableFuture.completedFuture(null))
			);
	}

	private CompletableFuture<Void> handleRelease(GitHubRelease release, String currentVersion, java.nio.file.Path runningJar) {
		if (release.draft() || release.prerelease()) {
			return CompletableFuture.completedFuture(null);
		}

		String latestVersion = release.displayVersion();
		if (!VersionComparator.isNewer(latestVersion, currentVersion)) {
			return CompletableFuture.completedFuture(null);
		}

		Optional<GitHubRelease.Asset> jarAsset = release.findJarAsset();
		if (jarAsset.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		}

		GitHubRelease.Asset asset = jarAsset.get();
		java.nio.file.Path pendingJar = replacementLogic.pendingJarPath(asset.name());

		return downloadUtility.download(asset, pendingJar, workerExecutor)
			.thenAccept(downloaded -> {
				if (!downloaded) {
					return;
				}

				boolean staged = replacementLogic.writePendingUpdate(latestVersion, pendingJar, runningJar, runningJar);
				if (staged) {
				}
			});
	}

	private String currentVersion() {
		return FabricLoader.getInstance()
			.getModContainer("eden")
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.filter(version -> !version.isBlank())
			.orElse(UpdaterConfig.CURRENT_VERSION);
	}

	private void shutdown() {
		scheduler.shutdownNow();
		workerExecutor.shutdownNow();
	}

	private static ThreadFactory daemonThreadFactory(String prefix) {
		AtomicBoolean firstThread = new AtomicBoolean(true);
		return runnable -> {
			String suffix = firstThread.getAndSet(false) ? "1" : Long.toUnsignedString(System.nanoTime(), 36);
			Thread thread = new Thread(runnable, "%s-%s".formatted(prefix, suffix));
			thread.setDaemon(true);
			return thread;
		};
	}
}
