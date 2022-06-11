package com.mrbysco.armorposer.client.gui.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ToggleButton extends Button {

	private boolean value;

	public ToggleButton(int x, int y, int width, int height, boolean defaultValue, OnPress pressedAction) {
		super(x, y, width, height, defaultValue ? Component.translatable("gui.yes") : Component.translatable("gui.no"), pressedAction);
		this.value = defaultValue;
	}

	public boolean getValue() {
		return this.value;
	}

	public void setValue(boolean value) {
		this.value = value;
		this.setMessage(value ? Component.translatable("gui.yes") : Component.translatable("gui.no"));
	}
}