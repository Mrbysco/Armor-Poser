package com.mrbysco.armorposer.client.gui.widgets;

import com.mrbysco.armorposer.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class PoseImageButton extends Button {
	private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/poser_buttons.png");
	private final int yOffset;

	public PoseImageButton(int x, int y, Button.OnPress onPress, int buttonID) {
		super(x, y, 20, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.yOffset = buttonID == 0 ? 0 : buttonID * 20;
	}

	public PoseImageButton(int x, int y, Button.OnPress onPress, int buttonID, Tooltip tooltip) {
		this(x, y, onPress, buttonID);
		this.setTooltip(tooltip);
	}

	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int i = 0;
		if (this.isHovered()) {
			i += 20;
		}

		guiGraphics.blit(PoseImageButton.BUTTON_LOCATION, this.getX(), this.getY(), i, yOffset, 20, 20, 64, 128);
	}
}