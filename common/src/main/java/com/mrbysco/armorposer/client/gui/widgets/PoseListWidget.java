package com.mrbysco.armorposer.client.gui.widgets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.ArmorPosesScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;

import java.util.function.Function;

public class PoseListWidget extends ObjectSelectionList<PoseListWidget.ListEntry> {
	private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);

	private final ArmorPosesScreen parent;
	private final int listWidth;
	private final Component title;

	public PoseListWidget(ArmorPosesScreen parent, Component title, boolean user, int listWidth, int top, int bottom) {
		super(parent.getScreenMinecraft(), listWidth, parent.height, top, bottom, parent.getScreenFont().lineHeight * 2 + 16);
		this.parent = parent;
		this.title = title;
		this.listWidth = listWidth;
		this.refreshList(user);
		this.setRenderBackground(false);
	}

	private int getX() {
		return this.x0;
	}

	public void setY(int y) {
		this.y0 = y;
		this.y1 = y + this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.getX() + this.listWidth - 6;
	}

	@Override
	public int getRowWidth() {
		return this.listWidth;
	}

	public void refreshList(boolean user) {
		this.clearEntries();
		if (user)
			parent.buildUserPoseList(this::addEntry, location -> new ListEntry(location, this.parent));
		else
			parent.buildPoseList(this::addEntry, location -> new ListEntry(location, this.parent));
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
		int xPos = this.getX() + (this.width - width) / 2;
		int xPos2 = this.getX() + (this.width + width) / 2;
		guiGraphics.fillGradient(xPos, top - 2, xPos2, top + height + 2, -1945083888, -1676648432);
	}

	@Override
	protected void renderBackground(GuiGraphics guiGraphics) {
		guiGraphics.fillGradient(getX(), 0, getX() + this.listWidth, parent.height, -1945104368, -1676668912);
	}

	@Override
	protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderDecorations(guiGraphics, mouseX, mouseY);
		guiGraphics.drawCenteredString(this.parent.getScreenFont(), title, getX() + this.listWidth / 2, 2, 16777215);
	}

//	@Override
//	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
//		guiGraphics.fillGradient(getX(), 0, getX() + this.listWidth, parent.height, -1945104368, -1676668912);
//		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
//		guiGraphics.drawCenteredString(this.parent.getScreenFont(), title, getX() + this.listWidth / 2, 2, 16777215);
//	}

	public class ListEntry extends Entry<ListEntry> {
		private final PoseEntry poseEntry;
		private final ArmorPosesScreen parent;
		private LivingEntity cachedEntity;

		ListEntry(PoseEntry entry, ArmorPosesScreen parent) {
			this.poseEntry = entry;
			this.parent = parent;

			Minecraft mc = parent.getScreenMinecraft();
			if (mc == null) {
				Reference.LOGGER.error("Minecraft is null, cannot create pose entry {}", entry.pose().name());
				return;
			}
			Level level = mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null ? mc.getSingleplayerServer().getAllLevels().iterator().next() : mc.level;
			if (level != null) {
				try {
					CompoundTag tag = TagParser.parseTag(entry.pose().data());

					CompoundTag nbt = new CompoundTag();
					nbt.putString("id", "minecraft:armor_stand");
					if (!tag.isEmpty()) {
						nbt.merge(tag);
					}
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
				} catch (Exception e) {
					Reference.LOGGER.error("Unable to parse nbt pose {}", e.getMessage());
				}
			}
		}

		@Override
		public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight,
		                   int mouseX, int mouseY, boolean hovered, float partialTicks) {
			Font font = this.parent.getScreenFont();
			renderScrollingString(guiGraphics, font, Component.literal(getName()), left + 36, top + 10, left + width - 18, top + 20, 0xFFFFFF);

			renderPose(guiGraphics, left + 16, top + 28, 15);
		}

		public void renderPose(GuiGraphics guiGraphics, int xPos, int yPos, int size) {
			if (cachedEntity != null) {
				InventoryScreen.renderEntityInInventory(guiGraphics, xPos, yPos, size,
						ARMOR_STAND_ANGLE, (Quaternionf) null, this.cachedEntity);
			}
		}

		@Override
		public void renderBack(GuiGraphics guiGraphics, int mouseX, int mouseY, int $$3, int $$4, int $$5, int $$6, int $$7, boolean $$8, float $$9) {
			super.renderBack(guiGraphics, mouseX, mouseY, $$3, $$4, $$5, $$6, $$7, $$8, $$9);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			parent.setSelected(this);
			if (PoseListWidget.this.getSelected() == this)
				PoseListWidget.this.setSelected(null);
			else
				PoseListWidget.this.setSelected(this);
			return false;
		}

		public CompoundTag getTag() {
			return poseEntry.getTag();
		}

		public String getName() {
			return poseEntry.getName();
		}

		public boolean userAdded() {
			return poseEntry.userAdded();
		}

		public String rawName() {
			return poseEntry.pose().name();
		}

		@Override
		public Component getNarration() {
			return Component.literal(getName());
		}

		protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
			int $$8 = font.width(text);
			int $$9 = (minY + maxY - 9) / 2 + 1;
			int $$10 = maxX - minX;
			if ($$8 > $$10) {
				int $$11 = $$8 - $$10;
				double $$12 = (double) Util.getMillis() / 1000.0D;
				double $$13 = Math.max((double) $$11 * 0.5D, 3.0D);
				double $$14 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * $$12 / $$13)) / 2.0D + 0.5D;
				double $$15 = Mth.lerp($$14, 0.0D, (double) $$11);
				guiGraphics.enableScissor(minX, minY, maxX, maxY);
				guiGraphics.drawString(font, text, minX - (int) $$15, $$9, color);
				guiGraphics.disableScissor();
			} else {
				guiGraphics.drawCenteredString(font, text, (minX + maxX) / 2, $$9, color);
			}
		}
	}
}
