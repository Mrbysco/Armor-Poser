package com.mrbysco.armorstand.config;

import com.mrbysco.armorstand.ArmorPoser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;


public class ModConfiguration {

	public static class Common {
		public final BooleanValue enableConfigGui;
		public final BooleanValue enableNameTags;

		Common(ForgeConfigSpec.Builder builder) {
			builder.comment("General settings")
					.push("General");

			enableConfigGui = builder
					.comment("Show the Armor Stand configuration GUI on shift right click")
					.translation("armorposer.config.enableConfigGui.tooltip")
					.define("enableConfigGui", true);

			enableNameTags = builder
					.comment("Allow Armor Stand to be renamed using name tags")
					.translation("armorposer.config.enableNameTags.tooltip")
					.define("enableNameTags", true);

			builder.pop();
		}

	}

	public static final ForgeConfigSpec commonSpec;
	public static final ModConfiguration.Common COMMON;
	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ModConfiguration.Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		ArmorPoser.LOGGER.debug("Loaded %s's config file {}", ArmorPoser.MOD_ID,  configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		ArmorPoser.LOGGER.debug("%s's config just got changed on the file system!", ArmorPoser.MOD_ID);
	}
}
