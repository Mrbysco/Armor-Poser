package com.mrbysco.armorposer.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.PoseListWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DeletePoseScreen extends Screen {
	private final ArmorStandScreen parentScreen;
	private Button deleteButton;
	private final PoseListWidget.ListEntry entry;
	private final Component warning = new TranslatableComponent("armorposer.gui.delete_poose.message");

	public DeletePoseScreen(ArmorStandScreen armorStandScreen, PoseListWidget.ListEntry entry) {
		super(new TranslatableComponent("armorposer.gui.delete_poose.title"));
		this.parentScreen = armorStandScreen;
		this.entry = entry;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(this.deleteButton = new Button(this.width / 2 - 66, this.height / 2 + 3, 60, 20, CommonComponents.GUI_YES, (button) -> {
			Reference.removePose(entry.rawName());
			this.minecraft.setScreen(parentScreen);
		}));

		this.addRenderableWidget(new Button(this.width / 2 - 4, this.height / 2 + 3, 60, 20, CommonComponents.GUI_NO, (button) -> {
			this.minecraft.setScreen(parentScreen);
		}));
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);

		this.entry.renderPose(poseStack, this.width / 2 - 5, this.height / 2 - 10, 30);

		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		drawCenteredString(poseStack, this.font, this.warning, this.width / 2, 40, 11141120);
	}
}
