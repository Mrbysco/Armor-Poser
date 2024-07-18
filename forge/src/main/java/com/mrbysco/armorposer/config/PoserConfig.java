package com.mrbysco.armorposer.config;

import com.mrbysco.armorposer.Reference;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;


public class PoserConfig {

	public static class Common {
		public final BooleanValue enableConfigGui;
		public final BooleanValue enableNameTags;
		public final BooleanValue allowScrolling;
		public final BooleanValue restrictResizeToOP;
		public final ModConfigSpec.ConfigValue<List<? extends String>> resizeWhitelist;

		Common(ModConfigSpec.Builder builder) {
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

			restrictResizeToOP = builder
					.comment("Restrict the ability to resize the Armor Stand to server operators")
					.translation("armorposer.config.restrictResizeToOP.tooltip")
					.define("restrictResizeToOP", false);

			resizeWhitelist = builder
					.comment("List of players that are allowed to resize the Armor Stand when restrictResizeToOP is enabled")
					.translation("armorposer.config.resizeWhitelist.tooltip")
					.defineListAllowEmpty("resizeWhitelist", List.of(), o -> o instanceof String);

			builder.pop();
		}

	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
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
