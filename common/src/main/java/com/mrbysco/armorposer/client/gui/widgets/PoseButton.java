package com.mrbysco.armorposer.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Function;

public class PoseButton extends Button {
	private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
	private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);

	private final String poseID;
	private final CompoundTag tag;
	private LivingEntity cachedEntity;

	public PoseButton(int x, int y, int width, int height, String poseID, Component pose, CompoundTag tag, OnPress onPress, CreateNarration createNarration) {
		super(x, y, width, height, pose, onPress, createNarration);
		this.poseID = poseID;
		this.tag = tag;

		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", "minecraft:armor_stand");
		if (!tag.isEmpty()) {
			nbt.merge(tag);
		}

		Minecraft mc = Minecraft.getInstance();
		Level level = mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null ? mc.getSingleplayerServer().getAllLevels().iterator().next() : mc.level;
		if (level != null) {
			ArmorStand armorStand = (ArmorStand) EntityType.loadEntityRecursive(nbt, level, Function.identity());
			if (armorStand != null) {
				armorStand.setNoBasePlate(true);
				armorStand.setShowArms(true);
				armorStand.yBodyRot = 210.0F;
				armorStand.setXRot(25.0F);
				armorStand.yHeadRot = armorStand.getYRot();
				armorStand.yHeadRotO = armorStand.getYRot();
				this.cachedEntity = armorStand;
			}
		}
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		Minecraft minecraft = Minecraft.getInstance();

		if (cachedEntity != null) {
			InventoryScreen.renderEntityInInventory(guiGraphics, getX() + 20, getY() + 38, 15,
					ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE, (Quaternionf) null, this.cachedEntity);
		}

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, -14F, 100F);
		int color = this.active ? 16777215 : 10526880;
		this.renderScrollingString(guiGraphics, minecraft.font, 2, color | Mth.ceil(this.alpha * 255.0F) << 24);
		poseStack.popPose();
	}

	@Override
	public void renderString(GuiGraphics guiGraphics, Font font, int color) {
		//
	}

	public CompoundTag getTag() {
		return tag;
	}

	public String getPoseID() {
		return poseID;
	}

	public static class Builder {
		private final String poseID;
		private final Component pose;
		private CompoundTag tag = new CompoundTag();
		private final OnPress onPress;
		@Nullable
		private Tooltip tooltip;
		private int x;
		private int y;
		private int width = 40;
		private int height = 40;
		private CreateNarration createNarration = Button.DEFAULT_NARRATION;

		public Builder(String poseID, String tag, OnPress onPress) {
			this.poseID = poseID;
			this.pose = Component.translatable(("armorposer.gui.pose." + poseID));
			try {
				this.tag = TagParser.parseTag(tag);
			} catch (Exception e) {
				//Nope
			}
			this.onPress = onPress;
		}

		public PoseButton.Builder pos(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public PoseButton.Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public PoseButton.Builder bounds(int x, int y, int width, int height) {
			return this.pos(x, y).size(width, height);
		}

		public PoseButton.Builder tooltip(@Nullable Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public PoseButton.Builder createNarration(CreateNarration createNarration) {
			this.createNarration = createNarration;
			return this;
		}

		public PoseButton build() {
			PoseButton button = new PoseButton(this.x, this.y, this.width, this.height, this.poseID, this.pose, this.tag, this.onPress, this.createNarration);
			button.setTooltip(this.tooltip);
			return button;
		}
	}
}