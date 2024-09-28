package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
	public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

	public static <T> T load(Class<T> clazz) {

		final T loadedService = ServiceLoader.load(clazz)
				.findFirst()
				.orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
		Reference.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
		return loadedService;
	}
}
