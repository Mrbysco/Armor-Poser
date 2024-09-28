package com.mrbysco.armorposer.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorposer.Reference;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class PoseImageButton extends Button {
	private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation(Reference.MOD_ID, "textures/gui/poser_buttons.png");
	private final int yOffset;

	public PoseImageButton(int x, int y, Button.OnPress onPress, int buttonID, Button.OnTooltip onTooltip) {
		super(x, y, 20, 20, TextComponent.EMPTY, onPress, onTooltip);
		this.yOffset = buttonID == 0 ? 0 : buttonID * 20;
	}


	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		super.renderButton(poseStack, mouseX, mouseY, partialTick);
		int i = 0;
		if (this.isHovered) {
			i += 20;
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, BUTTON_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		blit(poseStack, this.x, this.y, i, yOffset, 20, 20, 128, 256);
	}
}