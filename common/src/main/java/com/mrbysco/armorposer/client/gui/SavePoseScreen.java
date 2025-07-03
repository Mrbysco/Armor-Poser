package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class SavePoseScreen extends Screen {
	private final ArmorStandScreen parentScreen;
	private Button saveButton;
	private EditBox nameField;

	public SavePoseScreen(ArmorStandScreen armorStandScreen) {
		super(Component.translatable("armorposer.gui.save_pose.title"));
		this.parentScreen = armorStandScreen;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(this.saveButton = Button.builder(Component.translatable("armorposer.gui.label.save"), (button) -> {
			CompoundTag compound = this.parentScreen.writeFieldsToNBT();
			Reference.savePose(this.nameField.getValue(), compound);
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 - 66, this.height / 2 + 3, 60, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 - 4, this.height / 2 + 3, 60, 20).build());

		this.nameField = new EditBox(this.minecraft.font, this.width / 2 - 90, this.height / 2 - 24, 180, 20, Component.literal("Name"));
		this.nameField.setMaxLength(31);
		this.nameField.setTextColor(-1);
		this.addWidget(this.nameField);
		setInitialFocus(nameField);
	}

	@Override
	public void tick() {
		super.tick();

		this.saveButton.active = !this.nameField.getValue().isEmpty();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, ARGB.opaque(16777215));

		this.nameField.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTransparentBackground(guiGraphics);
	}
}
