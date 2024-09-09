package com.mrbysco.armorposer.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.NumberUtils;

public class SizeField extends EditBox {

	public final float scrollMultiplier = 0.1F;
	public final float minValue = 0.01F;
	public final float maxValue = 10.0F;

	public SizeField(Font font, int x, int y, int width, int height, Component defaultValue) {
		super(font, x, y, width, height, defaultValue);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void insertText(String textToWrite) {
		if (this.isNumeric(textToWrite))
			super.insertText(textToWrite);

		float currentValue = getFloat();
		if (currentValue > maxValue)
			this.setValue(String.valueOf(maxValue));
		else if (currentValue < minValue)
			this.setValue(String.valueOf(minValue));
	}

	@Override
	public String getValue() {
		return (this.isNumeric(super.getValue()) ? super.getValue() : "1.0");
	}

	@Override
	public void setValue(String value) {
		if (value.isEmpty()) {
			super.setValue("1.0");
		} else {
			super.setValue(String.format(("%.2f"), Float.parseFloat(value)));
		}
	}

	public float getFloat() {
		return NumberUtils.toFloat(super.getValue(), 1.0F);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setHighlightPos(this.getValue().length());
			this.moveCursorToEnd(false);
		}
	}

	protected boolean isNumeric(String value) {
		if (value.equals("."))
			return true;
		else
			return NumberUtils.isParsable(value);
	}
}