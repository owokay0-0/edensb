package dev.eden.client.update;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdatePreLaunch implements PreLaunchEntrypoint {
	private static final Logger LOGGER = LoggerFactory.getLogger("eden");

	@Override
	public void onPreLaunch() {
		try {
			new SafeReplacementLogic(LOGGER).applyPendingUpdate();
		} catch (RuntimeException exception) {
		}
	}
}
