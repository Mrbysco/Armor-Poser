package com.mrbysco.armorposer.util;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.mixin.ArmorStandAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.Locale;

/**
 * Utility class for handling default settings for armor stands.
 */
public class PoseDefaults {
	/**
	 * Adjust the armor stand based on the item stack's NBT data.
	 *
	 * @param stack      the item stack containing the pose data
	 * @param armorStand the armor stand to adjust
	 */
	public static void adjustArmorStand(ItemStack stack, ArmorStand armorStand) {
		if (PoserConfig.COMMON.nameBasedFeatures.get() && stack.getCustomName() != null) {
			final String name = stack.getCustomName().getString().toLowerCase(Locale.ROOT);
			switch (name) {
				case "armstrong", "arms":
					armorStand.setShowArms(true);
					break;
				case "based", "baseless":
					armorStand.setNoBasePlate(true);
					break;
				case "bdoubleo100", "bdubz", "small":
					((ArmorStandAccessor) armorStand).armorposer$setSmall(true);
					break;
				case "levitation":
					armorStand.setNoGravity(true);
					break;
				case "invincible", "invulnerable", "locked":
					armorStand.setPermanentlyInvulnerable(true);
					// no good way to disable slots
					try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(Reference.LOGGER)) {
						TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, armorStand.registryAccess());
						armorStand.saveWithoutId(output);
						CompoundTag outputCompound = output.buildResult();
						outputCompound.putInt("DisabledSlots", 4144959);
						armorStand.load(TagValueInput.create(ProblemReporter.DISCARDING, armorStand.registryAccess(), outputCompound));
					}
					break;
				default:
			}
		}
	}
}
