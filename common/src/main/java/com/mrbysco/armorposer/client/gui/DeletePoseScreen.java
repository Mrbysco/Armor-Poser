package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.PoseListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class DeletePoseScreen extends Screen {
	private final ArmorStandScreen parentScreen;
	private Button deleteButton;
	private final PoseListWidget.ListEntry entry;
	private final Component warning = Component.translatable("armorposer.gui.delete_poose.message");

	public DeletePoseScreen(ArmorStandScreen armorStandScreen, PoseListWidget.ListEntry entry) {
		super(Component.translatable("armorposer.gui.delete_poose.title"));
		this.parentScreen = armorStandScreen;
		this.entry = entry;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(this.deleteButton = Button.builder(CommonComponents.GUI_YES, (button) -> {
			Reference.removePose(entry.rawName());
			this.minecraft.setScreen(parentScreen);
		}).bounds(this.width / 2 - 66, this.height / 2 + 3, 60, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, (button) -> {
			this.minecraft.setScreen(parentScreen);
		}).bounds(this.width / 2 - 4, this.height / 2 + 3, 60, 20).build());
	}

	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		this.entry.renderPose(guiGraphics, this.width / 2, this.height / 2 - 50, 50);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
		guiGraphics.drawCenteredString(this.font, this.warning, this.width / 2, 50, 11141120);
	}
}
