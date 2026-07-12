package dev.eden.client.update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class SafeReplacementLogic {
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
	private static final String PENDING_DIRECTORY_NAME = ".updater-pending";
	private static final String PENDING_METADATA_NAME = "pending-update.json";

	private final Logger logger;

	public SafeReplacementLogic(Logger logger) {
		this.logger = logger;
	}

	public Optional<Path> findRunningModJar() {
		return FabricLoader.getInstance()
			.getModContainer("eden")
			.map(ModContainer::getOrigin)
			.flatMap(origin -> origin.getPaths().stream().findFirst())
			.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar"))
			.map(Path::toAbsolutePath)
			.map(Path::normalize);
	}

	public Path modsDirectory() {
		return FabricLoader.getInstance().getGameDir().resolve("mods").toAbsolutePath().normalize();
	}

	public Path pendingDirectory() {
		return modsDirectory().resolve(PENDING_DIRECTORY_NAME);
	}

	public Path metadataPath() {
		return pendingDirectory().resolve(PENDING_METADATA_NAME);
	}

	public Path pendingJarPath(String assetName) {
		return pendingDirectory().resolve(sanitizeJarName(assetName));
	}

	public boolean hasPendingUpdate() {
		return Files.isRegularFile(metadataPath());
	}

	public boolean writePendingUpdate(String version, Path pendingJar, Path targetJar, Path installedJar) {
		PendingUpdate pendingUpdate = new PendingUpdate(
			version,
			pendingJar.toAbsolutePath().normalize().toString(),
			targetJar.toAbsolutePath().normalize().toString(),
			installedJar.toAbsolutePath().normalize().toString()
		);

		try {
			Files.createDirectories(pendingDirectory());
			try (Writer writer = Files.newBufferedWriter(metadataPath())) {
				GSON.toJson(pendingUpdate, writer);
			}
			return true;
		} catch (IOException exception) {
			deleteQuietly(pendingJar);
			cleanupPendingDirectoryIfEmpty();
			return false;
		}
	}

	public boolean applyPendingUpdate() {
		Optional<PendingUpdate> pendingUpdate = readPendingUpdate();
		if (pendingUpdate.isEmpty()) {
			return false;
		}

		PendingUpdate update = pendingUpdate.get();
		Path pendingJar = Path.of(update.pendingJar()).toAbsolutePath().normalize();
		Path targetJar = Path.of(update.targetJar()).toAbsolutePath().normalize();
		Path installedJar = Path.of(update.installedJar()).toAbsolutePath().normalize();

		if (!isInsideModsDirectory(pendingJar) || !isInsideModsDirectory(targetJar) || !isInsideModsDirectory(installedJar)) {
			cleanupPendingUpdate(pendingJar);
			return false;
		}

		if (!Files.isRegularFile(pendingJar)) {
			cleanupPendingUpdate(pendingJar);
			return false;
		}

		try {
			Files.createDirectories(targetJar.getParent());
			moveReplacing(pendingJar, targetJar);
			deleteIfDifferent(installedJar, targetJar);
			deleteQuietly(metadataPath());
			cleanupPendingDirectoryIfEmpty();
			return true;
		} catch (IOException exception) {
			cleanupPendingUpdate(pendingJar);
			return false;
		}
	}

	private Optional<PendingUpdate> readPendingUpdate() {
		Path metadata = metadataPath();
		if (!Files.isRegularFile(metadata)) {
			return Optional.empty();
		}

		try (Reader reader = Files.newBufferedReader(metadata)) {
			return Optional.ofNullable(GSON.fromJson(reader, PendingUpdate.class));
		} catch (IOException | JsonParseException exception) {
			deleteQuietly(metadata);
			cleanupPendingDirectoryIfEmpty();
			return Optional.empty();
		}
	}

	private void moveReplacing(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void deleteIfDifferent(Path installedJar, Path targetJar) throws IOException {
		if (!installedJar.equals(targetJar)) {
			Files.deleteIfExists(installedJar);
		}
	}

	private boolean isInsideModsDirectory(Path path) {
		return path.toAbsolutePath().normalize().startsWith(modsDirectory());
	}

	private String sanitizeJarName(String assetName) {
		String cleaned = assetName.replace('\\', '-').replace('/', '-').trim();
		if (cleaned.isBlank() || !cleaned.toLowerCase().endsWith(".jar")) {
			return "eden-%s.jar".formatted(UpdaterConfig.CURRENT_VERSION);
		}
		return cleaned;
	}

	private void cleanupPendingUpdate(Path pendingJar) {
		deleteQuietly(pendingJar);
		deleteQuietly(metadataPath());
		cleanupPendingDirectoryIfEmpty();
	}

	private void cleanupPendingDirectoryIfEmpty() {
		Path directory = pendingDirectory();
		if (!Files.isDirectory(directory)) {
			return;
		}

		try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
			if (paths.iterator().hasNext()) {
				return;
			}
		} catch (IOException exception) {
			return;
		}

		try {
			Files.deleteIfExists(directory);
		} catch (IOException exception) {
		}
	}

	private void deleteQuietly(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException exception) {
		}
	}
}
