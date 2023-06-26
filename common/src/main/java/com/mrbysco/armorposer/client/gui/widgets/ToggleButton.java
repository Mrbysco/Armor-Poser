package com.mrbysco.armorposer.client.gui.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends Button {

	private boolean value;

	public ToggleButton(int x, int y, int width, int height, boolean defaultValue, Button.OnPress onPress, Button.CreateNarration createNarration) {
		super(x, y, width, height, defaultValue ? Component.translatable("gui.yes") : Component.translatable("gui.no"), onPress, createNarration);
		this.value = defaultValue;
	}

	public boolean getValue() {
		return this.value;
	}

	public void setValue(boolean value) {
		this.value = value;
		this.setMessage(value ? Component.translatable("gui.yes") : Component.translatable("gui.no"));
	}

	public static class Builder {
		private final boolean defaultValue;
		private final Button.OnPress onPress;
		@Nullable
		private Tooltip tooltip;
		private int x;
		private int y;
		private int width = 150;
		private int height = 20;
		private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

		public Builder(boolean defaultValue, Button.OnPress onPress) {
			this.defaultValue = defaultValue;
			this.onPress = onPress;
		}

		public ToggleButton.Builder pos(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public ToggleButton.Builder width(int width) {
			this.width = width;
			return this;
		}

		public ToggleButton.Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public ToggleButton.Builder bounds(int p_254166_, int p_253872_, int p_254522_, int p_253985_) {
			return this.pos(p_254166_, p_253872_).size(p_254522_, p_253985_);
		}

		public ToggleButton.Builder tooltip(@Nullable Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public ToggleButton.Builder createNarration(Button.CreateNarration createNarration) {
			this.createNarration = createNarration;
			return this;
		}

		public ToggleButton build() {
			ToggleButton button = new ToggleButton(this.x, this.y, this.width, this.height, this.defaultValue, this.onPress, this.createNarration);
			button.setTooltip(this.tooltip);
			return button;
		}
	}
}