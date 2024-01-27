package com.mrbysco.armorposer.config;

import com.mrbysco.armorposer.Reference;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Reference.MOD_ID)
public class PoserConfig implements ConfigData {
	@CollapsibleObject
	public General general = new General();

	public static class General {
		@Comment("Show the Armor Stand configuration GUI on shift right click")
		public boolean enableConfigGui = true;
		@Comment("Allow Armor Stand to be renamed using name tags")
		public boolean enableNameTags = true;
		@Comment("Allow scrolling to add / decrease an angle value in the posing screen")
		public boolean allowScrolling = true;
		@Comment("Open the configuration GUI when sprinting instead pressing the shift key")
		public  boolean useSprint = false;
	}
}
