package com.mrbysco.armorposer.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * A copy of LocKIconButton but allowing the tooltip to be provided
 */
public class TooltipLockIconButton extends Button {
	private boolean locked;

	public TooltipLockIconButton(int $$0, int $$1, Button.OnPress $$2, Button.OnTooltip $$3) {
		super($$0, $$1, 20, 20, new TranslatableComponent("narrator.button.difficulty_lock"), $$2, $$3);
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return CommonComponents.joinForNarration(
				super.createNarrationMessage(),
				this.isLocked()
						? new TranslatableComponent("narrator.button.difficulty_lock.locked")
						: new TranslatableComponent("narrator.button.difficulty_lock.unlocked")
		);
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean $$0) {
		this.locked = $$0;
	}

	@Override
	public void renderButton(PoseStack $$0, int $$1, int $$2, float $$3) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Button.WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		TooltipLockIconButton.Icon $$4;
		if (!this.active) {
			$$4 = this.locked ? TooltipLockIconButton.Icon.LOCKED_DISABLED : TooltipLockIconButton.Icon.UNLOCKED_DISABLED;
		} else if (this.isHoveredOrFocused()) {
			$$4 = this.locked ? TooltipLockIconButton.Icon.LOCKED_HOVER : TooltipLockIconButton.Icon.UNLOCKED_HOVER;
		} else {
			$$4 = this.locked ? TooltipLockIconButton.Icon.LOCKED : TooltipLockIconButton.Icon.UNLOCKED;
		}

		this.blit($$0, this.x, this.y, $$4.getX(), $$4.getY(), this.width, this.height);
	}

	static enum Icon {
		LOCKED(0, 146),
		LOCKED_HOVER(0, 166),
		LOCKED_DISABLED(0, 186),
		UNLOCKED(20, 146),
		UNLOCKED_HOVER(20, 166),
		UNLOCKED_DISABLED(20, 186);

		private final int x;
		private final int y;

		private Icon(int $$0, int $$1) {
			this.x = $$0;
			this.y = $$1;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
}
