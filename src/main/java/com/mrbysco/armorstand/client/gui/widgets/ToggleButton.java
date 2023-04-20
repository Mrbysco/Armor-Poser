package com.mrbysco.armorstand.client.gui.widgets;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class ToggleButton extends Button {

	private boolean value;

	public ToggleButton(int x, int y, int width, int height, boolean defaultValue, IPressable pressedAction) {
		super(x, y, width, height, defaultValue ? new TranslationTextComponent("gui.yes") : new TranslationTextComponent("gui.no"), pressedAction);
		this.value = defaultValue;
	}

	public boolean getValue() {
		return this.value;
	}

	public void setValue(boolean value) {
		this.value = value;
		this.setMessage(value ? new TranslationTextComponent("gui.yes") : new TranslationTextComponent("gui.no"));
	}
}