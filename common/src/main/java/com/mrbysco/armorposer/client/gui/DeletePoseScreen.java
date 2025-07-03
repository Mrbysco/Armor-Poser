package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.PoseListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class DeletePoseScreen extends Screen {
	private final ArmorStandScreen parentScreen;
	private final ArmorPosesScreen posesScreen;
	private final Component warning = Component.translatable("armorposer.gui.delete_poose.message");

	public DeletePoseScreen(ArmorPosesScreen posesScreen) {
		super(Component.translatable("armorposer.gui.delete_poose.title"));
		this.posesScreen = posesScreen;
		this.parentScreen = posesScreen.parentScreen;
	}

	private PoseListWidget.ListEntry getSelected() {
		return this.posesScreen.selected;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, (button) -> {
			Reference.removePose(this.getSelected().rawName());
			this.minecraft.setScreen(parentScreen);
		}).bounds(this.width / 2 - 66, this.height / 2 + 3, 60, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, (button) -> {
			this.minecraft.setScreen(parentScreen);
		}).bounds(this.width / 2 - 4, this.height / 2 + 3, 60, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		this.getSelected().renderPose(guiGraphics, this.width / 2 - 5, this.height / 2 - 10, 30);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, ARGB.opaque(16777215));
		guiGraphics.drawCenteredString(this.font, this.warning, this.width / 2, 40, ARGB.opaque(11141120));
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTransparentBackground(guiGraphics);
	}
}
