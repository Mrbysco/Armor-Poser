package com.mrbysco.armorposer.animation;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.BookCopyData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public class AnimationHandler {
	private static final Set<BlockPos> cachedFrames = new HashSet<>();

	public static void onFrameUpdate(ItemFrame frame) {
		if (frame.level() instanceof ServerLevel serverLevel && frame.getDirection() == Direction.UP) {
			BlockPos pos = frame.blockPosition();
			ItemStack frameStack = frame.getItem();
			if (isValidArmorPoserBook(frameStack)) {
				if (serverLevel.getSignal(pos, Direction.UP) >= 1) {
					if (!cachedFrames.contains(pos)) {
						cachedFrames.add(pos);
						WrittenBookContent bookContent = frameStack.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);
						String match = "";
						if (!bookContent.pages().isEmpty()) {
							var firstPage = bookContent.pages().getFirst();
							if (!firstPage.raw().getString().isEmpty())
								match = firstPage.raw().getString();
						}
						TargetingConditions conditions = TargetingConditions.forNonCombat();
						if (!match.isEmpty()) {
							String finalMatch = match;
							conditions.selector((livingEntity, level) ->
									livingEntity.getName().getString().equals(finalMatch));
						}
						var nearestStand = serverLevel.getNearestEntity(ArmorStand.class, conditions, null, pos.getX(), pos.getY(), pos.getZ(),
								AABB.ofSize(frame.position(), Reference.ANIMATION_SEARCH_RADIUS, Reference.ANIMATION_SEARCH_RADIUS, Reference.ANIMATION_SEARCH_RADIUS));
						if (nearestStand != null) {
							CompoundTag customTag = frameStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
							if (customTag.contains("SavedPose")) {
								CompoundTag poseTag = customTag.getCompound("SavedPose");
								BookCopyData bookCopyData = new BookCopyData(nearestStand.getUUID(), poseTag);
								bookCopyData.handleFrame(nearestStand);
							}
						}
					}
				} else {
					cachedFrames.remove(pos);
				}
			}
		}
	}

	private static boolean isValidArmorPoserBook(ItemStack stack) {
		return stack.is(Items.WRITTEN_BOOK)
				&& stack.getCustomName() != null
				&& stack.getCustomName().getString().equals("Armor Poser")
				&& stack.has(DataComponents.CUSTOM_DATA);
	}
}
