package com.mrbysco.armorposer.config;

import com.mrbysco.armorposer.Reference;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;


public class PoserConfig {

	public static class Common {
		public final BooleanValue enableConfigGui;
		public final BooleanValue enableNameTags;
		public final BooleanValue allowScrolling;
		public final BooleanValue useSprint;

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

			allowScrolling = builder
					.comment("Allow scrolling to increase / decrease an angle value in the posing screen")
					.translation("armorposer.config.allowScrolling.tooltip")
					.define("allowScrolling", true);

			useSprint = builder
					.comment("Open the configuration GUI when sprint instead shift")
					.translation("armorposer.config.useCtrlKey")
					.define("useSprint", false);

			builder.pop();
		}

	}

	public static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		Reference.LOGGER.debug("Loaded {}'s config file {}", Reference.MOD_ID, configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		Reference.LOGGER.debug("{}'s config just got changed on the file system!", Reference.MOD_ID);
	}
}
