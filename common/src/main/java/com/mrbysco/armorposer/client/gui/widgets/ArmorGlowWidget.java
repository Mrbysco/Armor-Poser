package com.mrbysco.armorposer.client.gui.widgets;

import com.mrbysco.armorposer.client.gui.ArmorGlowScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ArmorGlowWidget extends ObjectSelectionList<ArmorGlowWidget.ListEntry> {
	private static final Vector3f ARMOR_STAND_TRANSLATION = new Vector3f();
	private static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);

	private final ArmorGlowScreen parent;
	private final int listWidth;
	private final Component title;

	public ArmorGlowWidget(ArmorGlowScreen parent, Component title, int listWidth, int top, int bottom) {
		super(parent.getScreenMinecraft(), listWidth, bottom - top, top, parent.getScreenFont().lineHeight * 2 + 16);
		this.parent = parent;
		this.title = title;
		this.listWidth = listWidth;
		this.refreshList();
		this.setRenderBackground(false);
	}

	@Override
	protected int getScrollbarPosition() {
		return this.getX() + this.listWidth - 6;
	}

	@Override
	public int getRowWidth() {
		return this.listWidth;
	}

	public void refreshList() {
		this.clearEntries();
		parent.buildPositionList(this::addEntry, location -> new ListEntry(location, this.parent));
	}

	@Override
	protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
		int xPos = this.getX() + (this.width - width) / 2;
		int xPos2 = this.getX() + (this.width + width) / 2;
		guiGraphics.fillGradient(xPos, top - 2, xPos2, top + height + 2, -1945083888, -1676648432);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.fillGradient(getX(), 0, getX() + this.listWidth, parent.height, -1945104368, -1676668912);
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(this.parent.getScreenFont(), title, getX() + this.listWidth / 2, 2, 16777215);
	}

	public class ListEntry extends Entry<ListEntry> {
		private final ArmorGlowScreen parent;
		private ArmorStand armorStand;

		ListEntry(ArmorStand armorStand, ArmorGlowScreen parent) {
			this.armorStand = armorStand;
			this.parent = parent;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight,
		                   int mouseX, int mouseY, boolean hovered, float partialTicks) {
			Font font = this.parent.getScreenFont();
			renderScrollingString(guiGraphics, font, getPositionComponent(), left + 36, top + 10, left + width - 18, top + 20, 0xFFFFFF);

			renderPose(guiGraphics, left + 16, top + 28, 15);
		}

		public ArmorStand getArmorStand() {
			return armorStand;
		}

		public void renderPose(GuiGraphics guiGraphics, int xPos, int yPos, int size) {
			if (armorStand != null) {
				InventoryScreen.renderEntityInInventory(guiGraphics, xPos, yPos, size,
						ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE, (Quaternionf) null, this.armorStand);
			}
		}

		@Override
		public void renderBack(GuiGraphics guiGraphics, int mouseX, int mouseY, int $$3, int $$4, int $$5, int $$6, int $$7, boolean $$8, float $$9) {
			super.renderBack(guiGraphics, mouseX, mouseY, $$3, $$4, $$5, $$6, $$7, $$8, $$9);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			parent.setSelected(this);
			ArmorGlowWidget.this.setSelected(this);
			return false;
		}

		public Component getPositionComponent() {
			return Component.literal(getArmorStand().blockPosition().toShortString());
		}

		@Override
		public Component getNarration() {
			return getPositionComponent();
		}
	}
}
