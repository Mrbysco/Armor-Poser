package com.mrbysco.armorposer.client.gui.widgets;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrbysco.armorposer.util.PoseData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.jetbrains.annotations.NotNull;

public record PoseEntry(PoseData pose, boolean userAdded) implements Comparable<PoseEntry> {

	public PoseEntry(String name, String data, boolean userAdded) {
		this(new PoseData(name, data), userAdded);
	}

	public String getName() {
		return userAdded() ? pose().name() : I18n.get("armorposer.gui.pose." + pose().name());
	}

	public CompoundTag getTag() {
		try {
			return TagParser.parseTag(pose().data());
		} catch (CommandSyntaxException e) {
			return null;
		}
	}

	@Override
	public int compareTo(@NotNull PoseEntry o) {
		return getName().compareTo(o.getName());
	}
}
