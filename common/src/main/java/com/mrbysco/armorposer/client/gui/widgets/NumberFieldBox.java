package com.mrbysco.armorposer.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.function.Consumer;

public class NumberFieldBox extends EditBox {

	public float scrollMultiplier = 1;

	public float modValue = 360;
	public int decimalPoints = 0;
	public boolean allowDecimal = false;
	public OnTooltip onTooltip;

	public NumberFieldBox(Font font, int x, int y, int width, int height, Component defaultValue, OnTooltip tooltip) {
		super(font, x, y, width, height, defaultValue);
		this.onTooltip = tooltip;
	}

	public void setTooltip(OnTooltip tooltip) {
		this.onTooltip = tooltip;
	}

	@Override
	public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
		onTooltip.onTooltip(this, poseStack, mouseX, mouseY);
	}

	@Override
	public void updateNarration(NarrationElementOutput elementOutput) {
		this.defaultButtonNarrationText(elementOutput);
		onTooltip.narrateTooltip(add -> elementOutput.add(NarratedElementType.HINT, add));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void insertText(String textToWrite) {
		if (this.isNumeric(textToWrite)) super.insertText(textToWrite);

		float currentValue = getFloat();
		if (currentValue > 360 || currentValue < -360) {
			this.setValue("0");
		}
	}

	@Override
	public String getValue() {
		return (this.isNumeric(super.getValue()) ? super.getValue() : "0");
	}

	@Override
	public void setValue(String value) {
		if (value.isEmpty()) {
			super.setValue("0");
		} else {
			super.setValue(String.format(("%." + decimalPoints + "f"), Float.parseFloat(value)));
		}
	}

	public float getFloat() {
		return NumberUtils.toFloat(super.getValue(), 0.0F);
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setHighlightPos(this.getValue().length());
			this.moveCursorToEnd();
		}
	}

	protected boolean isNumeric(String value) {
		if (allowDecimal && value.equals("."))
			return true;
		else
			return value.equals("-") || NumberUtils.isParsable(value);
	}

	public interface OnTooltip {
		void onTooltip(NumberFieldBox numberFieldBox, PoseStack poseStack, int mouseX, int mouseY);

		default void narrateTooltip(Consumer<Component> consumer) {
		}
	}
}