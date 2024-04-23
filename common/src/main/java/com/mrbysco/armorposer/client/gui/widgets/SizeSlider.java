package com.mrbysco.armorposer.client.gui.widgets;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;

public class SizeSlider extends AbstractSliderButton {
	private final ArmorStandScreen screen;
	private final double minValue;
	private final double maxValue;
	protected double stepSize;
	private final DecimalFormat format = new DecimalFormat("0.00");

	public SizeSlider(int x, int y, int width, double value, double minValue, double maxValue, ArmorStandScreen screen) {
		super(x, y, width, 16, CommonComponents.EMPTY, 0.0);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.screen = screen;
		this.stepSize = 0.01D;
		this.value = (Mth.clamp((float) value, minValue, maxValue) - minValue) / (maxValue - minValue);

		this.updateMessage();
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		super.onDrag(mouseX, mouseY, dragX, dragY);
		this.setValueFromMouse(mouseX);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xScroll, double yScroll) {
		if (isHovered()) {
			double increment = Screen.hasShiftDown() ? 0.01D : 0.001D;
			this.setSliderValue(this.value + (yScroll < 0 ? -increment : increment));
		}
		return super.mouseScrolled(mouseX, mouseY, xScroll, yScroll);
	}

	private void setValueFromMouse(double mouseX) {
		this.setSliderValue((mouseX - (this.getX() + 4)) / (this.width - 8));
	}

	private void setSliderValue(double value) {
		double oldValue = this.value;
		this.value = this.snapToNearest(value);
		if (!Mth.equal(oldValue, this.value))
			this.applyValue();

		this.updateMessage();
	}

	private double snapToNearest(double value) {
		if (stepSize <= 0D)
			return Mth.clamp(value, 0D, 1D);

		value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);

		value = (stepSize * Math.round(value / stepSize));

		if (this.minValue > this.maxValue) {
			value = Mth.clamp(value, this.maxValue, this.minValue);
		} else {
			value = Mth.clamp(value, this.minValue, this.maxValue);
		}

		return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
	}

	@Override
	protected void applyValue() {
		this.screen.updateScale();
	}

	@Override
	public void onRelease(double d, double e) {
		super.onRelease(d, e);
	}

	@Override
	protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
		super.renderScrollingString(guiGraphics, font, i, j);
	}

	public double getValue() {
		return this.value * (maxValue - minValue) + minValue;
	}

	public String getValueString() {
		return this.format.format(this.getValue());
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Component.literal("").append(this.getValueString()));
//		this.setMessage(
//				CommonComponents.optionNameValue(
//						RealmsSlotOptionsScreen.SPAWN_PROTECTION_TEXT,
//						(Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0
//								? CommonComponents.OPTION_OFF
//								: Component.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))
//				)
//		);
	}

}
