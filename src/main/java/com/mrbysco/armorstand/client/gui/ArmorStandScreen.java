package com.mrbysco.armorstand.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrbysco.armorstand.ArmorPoser;
import com.mrbysco.armorstand.client.gui.widgets.NumberFieldWidget;
import com.mrbysco.armorstand.client.gui.widgets.ToggleButton;
import com.mrbysco.armorstand.packets.ArmorStandSyncMessage;
import com.mrbysco.armorstand.util.ArmorStandData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class ArmorStandScreen extends Screen {
	private final ArmorStand entityArmorStand;
	private final ArmorStandData armorStandData;

	private final String[] buttonLabels = new String[] { "invisible", "no_base_plate", "no_gravity", "show_arms", "small", "rotation" };
	private final String[] sliderLabels = new String[] { "head", "body", "left_leg", "right_leg", "left_arm", "right_arm" };

	private NumberFieldWidget rotationTextField;
	private final ToggleButton[] toggleButtons = new ToggleButton[5];
	private final NumberFieldWidget[] poseTextFields = new NumberFieldWidget[18];

	public ArmorStandScreen(ArmorStand entityArmorStand) {
		super(NarratorChatListener.NO_TITLE);
		this.entityArmorStand = entityArmorStand;

		this.armorStandData = new ArmorStandData();
		this.armorStandData.readFromNBT(entityArmorStand.saveWithoutId(new CompoundTag()));

		for (int i = 0; i < this.buttonLabels.length; i++)
			this.buttonLabels[i] = I18n.get(String.format("%s.gui.label." + this.buttonLabels[i], ArmorPoser.MOD_ID));
		for (int i = 0; i < this.sliderLabels.length; i++)
			this.sliderLabels[i] = I18n.get(String.format("%s.gui.label." + this.sliderLabels[i], ArmorPoser.MOD_ID));
	}

	public static void openScreen(ArmorStand armorStandEntity) {
		Minecraft.getInstance().setScreen(new ArmorStandScreen(armorStandEntity));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		super.init();

		int offsetX = 110;
		int offsetY = 50;

		// toggle buttons
		for (int i = 0; i < this.toggleButtons.length; i++) {
			int x = offsetX;
			int y = offsetY + (i * 22);
			int width = 40;
			int height = 20;

			this.toggleButtons[i] = new ToggleButton(x, y, width, height, this.armorStandData.getBooleanValue(i), (button) -> {
				ToggleButton toggleButton = ((ToggleButton)button);
				toggleButton.setValue(!toggleButton.getValue());
				this.textFieldUpdated();
			});
			this.addRenderableWidget(this.toggleButtons[i]);
		}

		// rotation textbox
		this.rotationTextField = new NumberFieldWidget(this.font, 1 + offsetX, 1 + offsetY + (this.toggleButtons.length * 22), 38, 17, new TextComponent("field.rotation"));
		this.rotationTextField.setValue(String.valueOf((int)this.armorStandData.rotation));
		this.rotationTextField.setMaxLength(3);
		this.addWidget(this.rotationTextField);

		// pose textboxes
		offsetX = this.width - 20 - 100;
		for (int i = 0; i < this.poseTextFields.length; i++) {
			int id = 5 + i;
			int x = 1 + offsetX + ((i % 3) * 35);
			int y = 1 + offsetY + ((i / 3) * 22);
			int width = 28;
			int height = 17;
			String value = String.valueOf((int)this.armorStandData.pose[i]);

			this.poseTextFields[i] = new NumberFieldWidget(this.font, x, y, width, height, new TextComponent(String.format("field.%s", i)));
			this.poseTextFields[i].setValue(value);
			this.poseTextFields[i].setMaxLength(3);
			this.addWidget(this.poseTextFields[i]);
		}

		offsetY = this.height / 4 + 120 + 12;

		// copy & paste buttons
		offsetX = 20;
		this.addRenderableWidget(new Button(offsetX, offsetY, 64, 20, new TranslatableComponent(String.format("%s.gui.label.copy", ArmorPoser.MOD_ID)), (button) -> {
			CompoundTag compound = this.writeFieldsToNBT();
			String clipboardData = compound.toString();
			if (this.minecraft != null) {
				this.minecraft.keyboardHandler.setClipboard(clipboardData);
			}
		}));
		this.addRenderableWidget(new Button(offsetX + 66, offsetY, 64, 20, new TranslatableComponent(String.format("%s.gui.label.paste", ArmorPoser.MOD_ID)), (button) -> {
			try {
				String clipboardData = null;
				if (this.minecraft != null) {
					clipboardData = this.minecraft.keyboardHandler.getClipboard();
				}
				CompoundTag compound = TagParser.parseTag(clipboardData);
				this.readFieldsFromNBT(compound);
				this.updateEntity(compound);
			} catch (Exception e) {
				//Nope
			}
		}));

		// done & cancel buttons
		offsetX = this.width - 20;
		this.addRenderableWidget(new Button(offsetX - ((2 * 96) + 2), offsetY, 96, 20, new TranslatableComponent("gui.done"), (button) -> {
			this.updateEntity(this.writeFieldsToNBT());
			this.minecraft.setScreen((Screen)null);
		}));
		this.addRenderableWidget(new Button(offsetX - 96, offsetY, 96, 20, new TranslatableComponent("gui.cancel"), (button) -> {
			this.updateEntity(this.armorStandData.writeToNBT());
			this.minecraft.setScreen((Screen)null);
		}));
	}

	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);

		// gui title
		this.drawCenteredString(matrixStack, this.font, new TranslatableComponent(String.format("%s.gui.title", ArmorPoser.MOD_ID)), this.width / 2, 20, 0xFFFFFF);

		// textboxes
		this.rotationTextField.render(matrixStack, mouseX, mouseY, partialTicks);
		for (EditBox textField : this.poseTextFields)
			textField.render(matrixStack, mouseX, mouseY, partialTicks);

		int offsetY = 50;

		// left column labels
		int offsetX = 20;
		for (int i = 0; i < this.buttonLabels.length; i++) {
			int x = offsetX;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			this.drawString(matrixStack, this.font, this.buttonLabels[i], x, y, 0xA0A0A0);
		}

		// right column labels
		offsetX = this.width - 20 - 100;
		// x, y, z
		this.drawString(matrixStack, this.font, "X", offsetX, 37, 0xA0A0A0);
		this.drawString(matrixStack, this.font, "Y", offsetX + (35), 37, 0xA0A0A0);
		this.drawString(matrixStack, this.font, "Z", offsetX + (2 * 35), 37, 0xA0A0A0);
		// pose textboxes
		for (int i = 0; i < this.sliderLabels.length; i++) {
			int x = offsetX - this.font.width(this.sliderLabels[i]) - 10;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			this.drawString(matrixStack, this.font, this.sliderLabels[i], x, y, 0xA0A0A0);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		this.rotationTextField.tick();
		for (EditBox textField : this.poseTextFields)
			textField.tick();
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		boolean typed = super.charTyped(codePoint, modifiers);
		if(typed) {
			this.textFieldUpdated();
		}
		return typed;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 15) { //Tab
			for (int i = 0; i < this.poseTextFields.length; i++) {
				if (this.poseTextFields[i].isFocused()) {
					this.textFieldUpdated();
					this.poseTextFields[i].moveCursorToEnd();
					this.poseTextFields[i].setFocus(false);

					int j = (!Screen.hasShiftDown() ? (i == this.poseTextFields.length - 1 ? 0 : i + 1) : (i == 0 ? this.poseTextFields.length - 1 : i - 1));
					this.poseTextFields[j].setFocus(true);
					this.poseTextFields[j].moveCursorTo(0);
					this.poseTextFields[j].setHighlightPos(this.poseTextFields[j].getValue().length());
				}
			}
		} else {
			if (this.rotationTextField.keyPressed(keyCode, scanCode, modifiers)) {
				this.textFieldUpdated();
				return true;
			} else {
				for (NumberFieldWidget textField : this.poseTextFields) {
					if (textField.keyPressed(keyCode, scanCode, modifiers)) {
						this.textFieldUpdated();
						return true;
					}
				}
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.rotationTextField.mouseClicked(mouseX, mouseY, button);
		for (NumberFieldWidget textField : this.poseTextFields) {
			textField.mouseClicked(mouseX, mouseY, button);
			this.textFieldUpdated();
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	protected void textFieldUpdated() {
		this.updateEntity(this.writeFieldsToNBT());
	}

	private CompoundTag writeFieldsToNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putBoolean("Invisible", this.toggleButtons[0].getValue());
		compound.putBoolean("NoBasePlate", this.toggleButtons[1].getValue());
		compound.putBoolean("NoGravity", this.toggleButtons[2].getValue());
		compound.putBoolean("ShowArms", this.toggleButtons[3].getValue());
		compound.putBoolean("Small", this.toggleButtons[4].getValue());

		ListTag rotationTag = new ListTag();
		rotationTag.add(FloatTag.valueOf(Float.valueOf(this.rotationTextField.getFloat())));
		compound.put("Rotation", rotationTag);

		CompoundTag poseTag = new CompoundTag();

		ListTag poseHeadTag = new ListTag();
		poseHeadTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[0].getFloat())));
		poseHeadTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[1].getFloat())));
		poseHeadTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[2].getFloat())));
		poseTag.put("Head", poseHeadTag);

		ListTag poseBodyTag = new ListTag();
		poseBodyTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[3].getFloat())));
		poseBodyTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[4].getFloat())));
		poseBodyTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[5].getFloat())));
		poseTag.put("Body", poseBodyTag);

		ListTag poseLeftLegTag = new ListTag();
		poseLeftLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[6].getFloat())));
		poseLeftLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[7].getFloat())));
		poseLeftLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[8].getFloat())));
		poseTag.put("LeftLeg", poseLeftLegTag);

		ListTag poseRightLegTag = new ListTag();
		poseRightLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[9].getFloat())));
		poseRightLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[10].getFloat())));
		poseRightLegTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[11].getFloat())));
		poseTag.put("RightLeg", poseRightLegTag);

		ListTag poseLeftArmTag = new ListTag();
		poseLeftArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[12].getFloat())));
		poseLeftArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[13].getFloat())));
		poseLeftArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[14].getFloat())));
		poseTag.put("LeftArm", poseLeftArmTag);

		ListTag poseRightArmTag = new ListTag();
		poseRightArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[15].getFloat())));
		poseRightArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[16].getFloat())));
		poseRightArmTag.add(FloatTag.valueOf(Float.valueOf(this.poseTextFields[17].getFloat())));
		poseTag.put("RightArm", poseRightArmTag);

		compound.put("Pose", poseTag);
		return compound;
	}

	private void readFieldsFromNBT(CompoundTag compound) {
		ArmorStandData armorStandData = new ArmorStandData();
		armorStandData.readFromNBT(compound);

		for (int i = 0; i < this.toggleButtons.length; i++) {
			this.toggleButtons[i].setValue(armorStandData.getBooleanValue(i));
		}

		this.rotationTextField.setValue(String.valueOf((int)armorStandData.rotation));

		for (int i = 0; i < this.poseTextFields.length; i++) {
			this.poseTextFields[i].setValue(String.valueOf((int)armorStandData.pose[i]));
		}
	}

	private void updateEntity(CompoundTag compound) {
		CompoundTag CompoundNBT = this.entityArmorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		this.entityArmorStand.load(CompoundNBT);

		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSyncMessage(entityArmorStand.getUUID(), compound));
	}

}