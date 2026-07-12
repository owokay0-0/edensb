package dev.eden.client.update;

public record PendingUpdate(
	String version,
	String pendingJar,
	String targetJar,
	String installedJar
) {
}
