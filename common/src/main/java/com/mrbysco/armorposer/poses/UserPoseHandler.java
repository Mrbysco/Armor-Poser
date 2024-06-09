package com.mrbysco.armorposer.poses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.platform.Services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UserPoseHandler {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final File PRESET_FOLDER = new File(Services.PLATFORM.getUserPresetFolder().toFile() + "/armorposer");
	public static final File PRESET_FILE = new File(PRESET_FOLDER, "User_poses.json");

	public static void initializePresets() {
		if (!PRESET_FOLDER.exists() || !PRESET_FILE.exists()) {
			PRESET_FOLDER.mkdirs();

			UserPoses userPresets = new UserPoses(Reference.userPoses);
			try (FileWriter writer = new FileWriter(PRESET_FILE)) {
				GSON.toJson(userPresets, writer);
				writer.flush();
			} catch (IOException e) {
				Reference.LOGGER.error("Failed to user presets {}", e.getMessage());
			}
		}
	}

	public static void saveUserPoses() {
		if (!PRESET_FOLDER.exists()) {
			PRESET_FOLDER.mkdirs();
		}

		UserPoses userPresets = new UserPoses(Reference.userPoses);
		try (FileWriter writer = new FileWriter(PRESET_FILE)) {
			GSON.toJson(userPresets, writer);
			writer.flush();
		} catch (IOException e) {
			Reference.LOGGER.error("Failed to user presets {}", e.getMessage());
		}
	}

	public static void loadUserPoses() {
		if (!PRESET_FOLDER.exists() || !PRESET_FILE.exists()) {
			initializePresets();
		}

		Reference.userPoses.clear();
		String fileName = PRESET_FILE.getName();
		try (FileReader json = new FileReader(PRESET_FILE)) {
			final UserPoses userPoses = GSON.fromJson(json, UserPoses.class);
			if (userPoses != null) {
				Reference.userPoses.addAll(userPoses.userPoses());
			} else {
				Reference.LOGGER.error("Could not load user poses from {}.", fileName);
			}
		} catch (final Exception e) {
			Reference.LOGGER.error("Unable to load file {}. Please make sure it's a valid json.", fileName);
			Reference.LOGGER.trace("Exception: ", e);
		}
	}
}
