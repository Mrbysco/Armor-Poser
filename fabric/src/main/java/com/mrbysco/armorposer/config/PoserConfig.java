package com.mrbysco.armorposer.config;

import com.mrbysco.armorposer.Reference;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = Reference.MOD_ID)
public class PoserConfig implements ConfigData {
	@CollapsibleObject
	public General general = new General();

	public static class General {
		@ConfigEntry.Gui.Tooltip
		@Comment("Show the Armor Stand configuration GUI on shift right click")
		public boolean enableConfigGui = true;
		@ConfigEntry.Gui.Tooltip
		@Comment("Allow Armor Stand to be renamed using name tags")
		public boolean enableNameTags = true;
		@ConfigEntry.Gui.Tooltip
		@Comment("Allow scrolling to add / decrease an angle value in the posing screen")
		public boolean allowScrolling = true;
		@ConfigEntry.Gui.Tooltip
		@Comment("Restrict the ability to resize the Armor Stand to server operators")
		public boolean restrictResizeToOP = false;
		@ConfigEntry.Gui.Tooltip
		@Comment("List of players that are allowed to resize the Armor Stand when restrictResizeToOP is enabled")
		public List<String> resizeWhitelist = List.of();
	}
}
