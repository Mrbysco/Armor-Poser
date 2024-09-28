package com.mrbysco.armorposer.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.NumberFieldBox;
import com.mrbysco.armorposer.client.gui.widgets.PoseImageButton;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import com.mrbysco.armorposer.client.gui.widgets.TooltipLockIconButton;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.platform.Services;
import com.mrbysco.armorposer.util.ArmorStandData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class ArmorStandScreen extends Screen {
	private final ArmorStand entityArmorStand;
	private final ArmorStandData armorStandData;

	private final String[] buttonLabels = new String[]{"invisible", "no_base_plate", "no_gravity", "show_arms", "small", "name_visible", "rotation"};
	private final String[] sliderLabels = new String[]{"head", "body", "left_leg", "right_leg", "left_arm", "right_arm", "position"};

	private NumberFieldBox rotationTextField;
	private final ToggleButton[] toggleButtons = new ToggleButton[6];
	protected final NumberFieldBox[] poseTextFields = new NumberFieldBox[3 * 7];
	private TooltipLockIconButton lockButton;
	private final boolean allowScrolling;

	private Vec3 lastSendOffset = new Vec3(0, 0, 0);

	//Cache the tooltip, so we don't have to create a new one every tick
	private final NumberFieldBox.OnTooltip yPositionTooltip = createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip.y_position"));
	private final NumberFieldBox.OnTooltip yPositionTooltipDisabled = createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip.y_position.disabled").withStyle(ChatFormatting.RED));

	private final int whiteColor = 16777215;

	public ArmorStandScreen(ArmorStand entityArmorStand) {
		super(NarratorChatListener.NO_TITLE);
		this.entityArmorStand = entityArmorStand;

		this.armorStandData = new ArmorStandData();
		this.armorStandData.readFromNBT(entityArmorStand.saveWithoutId(new CompoundTag()));

		this.allowScrolling = Services.PLATFORM.allowScrolling();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		super.init();

		int offsetX = 110;
		int offsetY = 20;

		// toggle buttons
		for (int i = 0; i < this.toggleButtons.length; i++) {
			int x = offsetX;
			int y = offsetY + (i * 22);
			int width = 40;
			int height = 20;

			this.addRenderableWidget(this.toggleButtons[1] = new ToggleButton(x, y, width, height, this.armorStandData.getBooleanValue(i), (button) -> {
				ToggleButton toggleButton = ((ToggleButton) button);
				toggleButton.setValue(!toggleButton.getValue());
				this.textFieldUpdated();
			}, createTooltip(new TranslatableComponent("armorposer.gui.tooltip." + buttonLabels[i]))));
		}

		// rotation textbox
		this.rotationTextField = new NumberFieldBox(this.font, 1 + offsetX, 1 + offsetY + (this.toggleButtons.length * 22),
				38, 17, new TranslatableComponent("armorposer.gui.label.rotation"),
				createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip.rotation")));
		this.rotationTextField.setValue(String.valueOf((int) this.armorStandData.rotation));
		this.rotationTextField.setMaxLength(4);
		this.addWidget(this.rotationTextField);

		// pose textboxes
		offsetX = this.width - 20 - 100;
		for (int i = 0; i < this.poseTextFields.length; i++) {
			int x = 1 + offsetX + ((i % 3) * 35);
			int y = 1 + offsetY + ((i / 3) * 22);
			int width = 28;
			int height = 17;
			String value = String.valueOf((int) this.armorStandData.pose[i]);
			boolean lastRow = i >= 3 * 6 && i < 3 * 7;

			//Create tooltip
			NumberFieldBox.OnTooltip tooltip;
			if (i % 3 == 0) {
				tooltip = createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip." + (lastRow ? "x_position" : "x_rotation")));
			} else if (i % 3 == 1) {
				tooltip = createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip." + (lastRow ? "y_position" : "y_rotation")));
			} else {
				tooltip = createFieldTooltip(new TranslatableComponent("armorposer.gui.tooltip." + (lastRow ? "z_position" : "z_rotation")));
			}
			this.poseTextFields[i] = new NumberFieldBox(this.font, x, y, width, height, new TextComponent(value), tooltip);
			this.poseTextFields[i].setValue(value);
			this.poseTextFields[i].setMaxLength(4);
			if (lastRow) {
				this.poseTextFields[i].scrollMultiplier = 0.01f;
				this.poseTextFields[i].modValue = Integer.MAX_VALUE;
				this.poseTextFields[i].decimalPoints = 2;
				this.poseTextFields[i].allowDecimal = true;
				this.poseTextFields[i].setMaxLength(6);
			}

			this.addWidget(this.poseTextFields[i]);
		}

		offsetY = this.height / 4 + 134;

		// copy & paste buttons
		offsetX = 20;
		this.addRenderableWidget(new Button(offsetX, offsetY, 130, 20, new TranslatableComponent("armorposer.gui.label.poses"), (button) ->
						this.minecraft.setScreen(new ArmorPosesScreen(this)),
				createTooltip(new TranslatableComponent("armorposer.gui.tooltip.poses"))));
		this.addRenderableWidget(new Button(offsetX, offsetY + 22, 42, 20, new TranslatableComponent("armorposer.gui.label.copy"), (button) -> {
			CompoundTag compound = this.writeFieldsToNBT();
			String clipboardData = compound.toString();
			if (this.minecraft != null) {
				this.minecraft.keyboardHandler.setClipboard(clipboardData);
			}
		}, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.copy"))));

		this.addRenderableWidget(new Button(offsetX + 44, offsetY + 22, 42, 20, new TranslatableComponent("armorposer.gui.label.paste"), (button) -> {
			try {
				String clipboardData = null;
				if (this.minecraft != null) {
					clipboardData = this.minecraft.keyboardHandler.getClipboard();
				}
				if (clipboardData != null) {
					CompoundTag compound = TagParser.parseTag(clipboardData);
					this.readFieldsFromNBT(compound);
					this.updateEntity(compound);
				}
			} catch (Exception e) {
				//Nope
			}
		}, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.paste"))));

		this.addRenderableWidget(new Button(offsetX + 88, offsetY + 22, 42, 20, new TranslatableComponent("armorposer.gui.label.save"), (button) -> {
			this.minecraft.setScreen(new SavePoseScreen(this));
		}, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.save"))));

		offsetX = this.width - 20;
		int buttonsLeft = 9;
		int buttonOffset = -4;
		PoseImageButton mirrorPose = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			//Mirror head
			float[] head = new float[]{poseTextFields[0].getFloat(), poseTextFields[1].getFloat(), poseTextFields[2].getFloat()};
			poseTextFields[0].setValue(String.valueOf(head[0]));
			poseTextFields[1].setValue(String.valueOf(head[1] != 0 ? -head[1] : 0));
			poseTextFields[2].setValue(String.valueOf(head[2] != 0 ? -head[2] : 0));

			//Mirror head
			float[] body = new float[]{poseTextFields[3].getFloat(), poseTextFields[4].getFloat(), poseTextFields[5].getFloat()};
			poseTextFields[3].setValue(String.valueOf(body[0]));
			poseTextFields[4].setValue(String.valueOf(body[1] != 0 ? -body[1] : 0));
			poseTextFields[5].setValue(String.valueOf(body[2] != 0 ? -body[2] : 0));

			//Mirror Legs
			float[] leftLeg = new float[]{poseTextFields[6].getFloat(), poseTextFields[7].getFloat(), poseTextFields[8].getFloat()};
			float[] rightLeg = new float[]{poseTextFields[9].getFloat(), poseTextFields[10].getFloat(), poseTextFields[11].getFloat()};

			//Swap angles and mirror the angles
			poseTextFields[6].setValue(String.valueOf(rightLeg[0]));
			poseTextFields[7].setValue(String.valueOf(rightLeg[1] != 0 ? -rightLeg[1] : 0));
			poseTextFields[8].setValue(String.valueOf(rightLeg[2] != 0 ? -rightLeg[2] : 0));
			poseTextFields[9].setValue(String.valueOf(leftLeg[0]));
			poseTextFields[10].setValue(String.valueOf(leftLeg[1] != 0 ? -leftLeg[1] : 0));
			poseTextFields[11].setValue(String.valueOf(leftLeg[2] != 0 ? -leftLeg[2] : 0));

			//Mirror Arms
			float[] leftArm = new float[]{poseTextFields[12].getFloat(), poseTextFields[13].getFloat(), poseTextFields[14].getFloat()};
			float[] rightArm = new float[]{poseTextFields[15].getFloat(), poseTextFields[16].getFloat(), poseTextFields[17].getFloat()};

			//Swap angles and mirror the angles
			poseTextFields[12].setValue(String.valueOf(rightArm[0]));
			poseTextFields[13].setValue(String.valueOf(rightArm[1] != 0 ? -rightArm[1] : 0));
			poseTextFields[14].setValue(String.valueOf(rightArm[2] != 0 ? -rightArm[2] : 0));
			poseTextFields[15].setValue(String.valueOf(leftArm[0]));
			poseTextFields[16].setValue(String.valueOf(leftArm[1] != 0 ? -leftArm[1] : 0));
			poseTextFields[17].setValue(String.valueOf(leftArm[2] != 0 ? -leftArm[2] : 0));

		}, 0, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.mirror"))));
		buttonsLeft--;

		PoseImageButton mirrorLegs = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			//Mirror Legs
			float[] leftLeg = new float[]{poseTextFields[6].getFloat(), poseTextFields[7].getFloat(), poseTextFields[8].getFloat()};
			float[] rightLeg = new float[]{poseTextFields[9].getFloat(), poseTextFields[10].getFloat(), poseTextFields[11].getFloat()};

			//Swap angles and mirror the angles
			poseTextFields[6].setValue(String.valueOf(rightLeg[0]));
			poseTextFields[7].setValue(String.valueOf(rightLeg[1] != 0 ? -rightLeg[1] : 0));
			poseTextFields[8].setValue(String.valueOf(rightLeg[2] != 0 ? -rightLeg[2] : 0));
			poseTextFields[9].setValue(String.valueOf(leftLeg[0]));
			poseTextFields[10].setValue(String.valueOf(leftLeg[1] != 0 ? -leftLeg[1] : 0));
			poseTextFields[11].setValue(String.valueOf(leftLeg[2] != 0 ? -leftLeg[2] : 0));
		}, 1, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.mirror_legs"))));
		buttonsLeft--;

		PoseImageButton mirrorArms = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			//Mirror Arms
			float[] leftArm = new float[]{poseTextFields[12].getFloat(), poseTextFields[13].getFloat(), poseTextFields[14].getFloat()};
			float[] rightArm = new float[]{poseTextFields[15].getFloat(), poseTextFields[16].getFloat(), poseTextFields[17].getFloat()};

			//Swap angles and mirror the angles
			poseTextFields[12].setValue(String.valueOf(rightArm[0]));
			poseTextFields[13].setValue(String.valueOf(rightArm[1] != 0 ? -rightArm[1] : 0));
			poseTextFields[14].setValue(String.valueOf(rightArm[2] != 0 ? -rightArm[2] : 0));
			poseTextFields[15].setValue(String.valueOf(leftArm[0]));
			poseTextFields[16].setValue(String.valueOf(leftArm[1] != 0 ? -leftArm[1] : 0));
			poseTextFields[17].setValue(String.valueOf(leftArm[2] != 0 ? -leftArm[2] : 0));
		}, 2, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.mirror_arms"))));
		buttonsLeft--;

		PoseImageButton swapToHead = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			//Swap item in main hand with head
			Services.PLATFORM.swapSlots(this.entityArmorStand, SwapData.Action.SWAP_WITH_HEAD);

		}, 3, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.swap_head"))));
		buttonsLeft--;

		PoseImageButton swapHands = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			//Swap item in main and offhand
			Services.PLATFORM.swapSlots(this.entityArmorStand, SwapData.Action.SWAP_HANDS);

		}, 4, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.swap_hands"))));
		buttonsLeft--;

		PoseImageButton blockButton = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			try {
				Vec3 pos = this.entityArmorStand.position();

				//Get the amount subtracted of x to get .0725
				double xDiff = getDesiredOffset(pos.x, 1.0725D);
				//Get the amount subtracted of y to get .345
				double yDiff = getDesiredOffset(pos.y, -0.655D);
				//Get the amount subtracted of z to get .852
				double zDiff = getDesiredOffset(pos.z, 0.852D);

				Vec3 offset = new Vec3(xDiff, yDiff, zDiff);
				int closestDegree = Mth.roundToward((int) this.rotationTextField.getFloat(), 90);
				switch (closestDegree) {
					case 90: {
						//Rotate the desired position to have the correct values
						double newX = offset.z - 0.7D;
						double newZ = -offset.x + 1.18D;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
					case -180: {
						//Rotate the desired position to have the correct values
						double newX = -offset.x;
						double newZ = -offset.z;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
					case -90: {
						//Rotate the desired position to have the correct values
						double newX = -offset.z + 0.7D;
						double newZ = offset.x - 1.18D;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
				}

				CompoundTag tag = TagParser.parseTag(Reference.alignedBlockPose);
				this.readFieldsFromNBT(tag);
				this.toggleButtons[0].setValue(true); //Set invisible
				this.toggleButtons[2].setValue(true); //Set no gravity
				this.toggleButtons[3].setValue(true); //Set show arms
				this.rotationTextField.setValue(String.valueOf(closestDegree)); //Set rotation
				this.poseTextFields[18].setValue(String.valueOf(offset.x)); //Set X
				this.poseTextFields[19].setValue(String.valueOf(offset.y)); //Set Y
				this.poseTextFields[20].setValue(String.valueOf(offset.z)); //Set Z
				this.textFieldUpdated();
			} catch (CommandSyntaxException e) {
				//Nope
			}

		}, 5, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.block"))));
		buttonsLeft--;

		PoseImageButton itemButton = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			if (hasShiftDown()) { //If shift is held the item will be upright
				try {
					Vec3 pos = this.entityArmorStand.position();

					//Get the amount subtracted of x to get .86
					double xDiff = getDesiredOffset(pos.x, 0.86D);
					//Get the amount subtracted of y to get .59
					double yDiff = getDesiredOffset(pos.y, -1.41D);
					//Get the amount subtracted of z to get .9375
					double zDiff = getDesiredOffset(pos.z, -0.0625D);

					Vec3 offset = new Vec3(xDiff, yDiff, zDiff);
					int closestDegree = Mth.roundToward((int) this.rotationTextField.getFloat(), 90);
					switch (closestDegree) {
						case 90: {
							//Rotate the desired position to have the correct values
							double newX = offset.z + 1.12D;
							double newZ = -offset.x + 0.74D;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
						case -180: {
							//Rotate the desired position to have the correct values
							double newX = -offset.x;
							double newZ = -offset.z;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
						case -90: {
							//Rotate the desired position to have the correct values
							double newX = -offset.z - 1.12D;
							double newZ = offset.x - 0.74D;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
					}

					CompoundTag tag = TagParser.parseTag(Reference.alignedUprightItemPose);
					this.readFieldsFromNBT(tag);
					this.toggleButtons[0].setValue(true); //Set invisible
					this.toggleButtons[2].setValue(true); //Set no gravity
					this.toggleButtons[3].setValue(true); //Set show arms
					this.rotationTextField.setValue(String.valueOf(closestDegree)); //Set rotation
					this.poseTextFields[18].setValue(String.valueOf(offset.x)); //Set X
					this.poseTextFields[19].setValue(String.valueOf(offset.y)); //Set Y
					this.poseTextFields[20].setValue(String.valueOf(offset.z)); //Set Z
					this.textFieldUpdated();
				} catch (CommandSyntaxException e) {
					//Nope
				}
			} else {
				try {
					Vec3 pos = this.entityArmorStand.position();

					//Get the amount subtracted of x to get .886
					double xDiff = getDesiredOffset(pos.x, 0.886D);
					//Get the amount subtracted of y to get .22
					double yDiff = getDesiredOffset(pos.y, -0.78D);
					//Get the amount subtracted of z to get .205
					double zDiff = getDesiredOffset(pos.z, 0.205D);

					Vec3 offset = new Vec3(xDiff, yDiff, zDiff);
					int closestDegree = Mth.roundToward((int) this.rotationTextField.getFloat(), 90);
					switch (closestDegree) {
						case 90: {
							//Rotate the desired position to have the correct values
							double newX = offset.z + 0.59D;
							double newZ = -offset.x + 0.78D;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
						case -180: {
							//Rotate the desired position to have the correct values
							double newX = -offset.x;
							double newZ = -offset.z;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
						case -90: {
							//Rotate the desired position to have the correct values
							double newX = -offset.z - 0.59D;
							double newZ = offset.x - 0.78D;
							offset = new Vec3(newX, offset.y, newZ);
							break;
						}
					}

					CompoundTag tag = TagParser.parseTag(Reference.alignedFlatItemPose);
					this.readFieldsFromNBT(tag);
					this.toggleButtons[0].setValue(true); //Set invisible
					this.toggleButtons[2].setValue(true); //Set no gravity
					this.toggleButtons[3].setValue(true); //Set show arms
					this.rotationTextField.setValue(String.valueOf(closestDegree)); //Set rotation
					this.poseTextFields[18].setValue(String.valueOf(offset.x)); //Set X
					this.poseTextFields[19].setValue(String.valueOf(offset.y)); //Set Y
					this.poseTextFields[20].setValue(String.valueOf(offset.z)); //Set Z
					this.textFieldUpdated();
				} catch (CommandSyntaxException e) {
					//Nope
				}
			}

		}, 6, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.item").append("\n").append(new TranslatableComponent("armorposer.gui.tooltip.item2").withStyle(ChatFormatting.GRAY)))));
		buttonsLeft--;
		PoseImageButton toolButton = this.addRenderableWidget(new PoseImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			try {
				Vec3 pos = this.entityArmorStand.position();

				//Get the amount subtracted of x to get .33
				double xDiff = getDesiredOffset(pos.x, 0.33D);
				//Get the amount subtracted of y to get .22
				double yDiff = getDesiredOffset(pos.y, -1.285D);
				//Get the amount subtracted of z to get .059999D
				double zDiff = getDesiredOffset(pos.z, 0.059999D);

				Vec3 offset = new Vec3(xDiff, yDiff, zDiff);
				int closestDegree = Mth.roundToward((int) this.rotationTextField.getFloat(), 90);
				switch (closestDegree) {
					case 90: {
						//Rotate the desired position to have the correct values
						double newX = offset.z + 0.88D;
						double newZ = -offset.x - 0.34D;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
					case -180: {
						//Rotate the desired position to have the correct values
						double newX = -offset.x;
						double newZ = -offset.z;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
					case -90: {
						//Rotate the desired position to have the correct values
						double newX = -offset.z - 0.88D;
						double newZ = offset.x + 0.34D;
						offset = new Vec3(newX, offset.y, newZ);
						break;
					}
				}

				CompoundTag tag = TagParser.parseTag(Reference.alignedToolPose);
				this.readFieldsFromNBT(tag);
				this.toggleButtons[0].setValue(true); //Set invisible
				this.toggleButtons[2].setValue(true); //Set no gravity
				this.toggleButtons[3].setValue(true); //Set show arms
				this.rotationTextField.setValue(String.valueOf(closestDegree)); //Set rotation
				this.poseTextFields[18].setValue(String.valueOf(offset.x)); //Set X
				this.poseTextFields[19].setValue(String.valueOf(offset.y)); //Set Y
				this.poseTextFields[20].setValue(String.valueOf(offset.z)); //Set Z
				this.textFieldUpdated();
			} catch (CommandSyntaxException e) {
				//Nope
			}

		}, 7, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.tool"))));
		buttonsLeft--;

		this.addRenderableWidget(this.lockButton = new TooltipLockIconButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			this.lockButton.setLocked(!this.lockButton.isLocked());
		}, createTooltip(new TranslatableComponent("armorposer.gui.tooltip.lock"))));
		this.lockButton.setLocked(this.armorStandData.getBooleanValue(6));

		// done & cancel buttons
		this.addRenderableWidget(new Button(offsetX - ((2 * 96) + 2), offsetY + 22, 97, 20, new TranslatableComponent("gui.done"), (button) -> {
			this.textFieldUpdated();
			this.minecraft.setScreen((Screen) null);
		}));
		this.addRenderableWidget(new Button(offsetX - 95, offsetY + 22, 97, 20, new TranslatableComponent("gui.cancel"), (button) -> {
			this.poseTextFields[18].setValue("0");
			this.poseTextFields[19].setValue("0");
			this.poseTextFields[20].setValue("0");
			this.textFieldUpdated();
			this.updateEntity(this.armorStandData.writeToNBT());
			this.minecraft.setScreen((Screen) null);
		}));
		this.addRenderableWidget(new Button(0, 0, 16, 16, new TextComponent("💡"), (button) -> {
			this.minecraft.setScreen(new ArmorGlowScreen(this));
		}));
	}

	/**
	 * Get the desired offset to get the armor stand in the correct position
	 *
	 * @param posValue     The current position value
	 * @param desiredValue The desired position value
	 * @return The amount subtracted from or added to the current position to get the desired position
	 */
	private double getDesiredOffset(double posValue, double desiredValue) {
		double value = posValue - (int) posValue; //Get the decimal value
		if (value < 0) { //Make it positive if it's a negative position
			value = -value;
		}
		return desiredValue - value;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		super.render(poseStack, mouseX, mouseY, partialTicks);

		// Draw gui title
		drawCenteredString(poseStack, this.font, new TranslatableComponent("armorposer.gui.title"), this.width / 2, 10, 0xFFFFFF);

		// Draw textboxes
		this.rotationTextField.render(poseStack, mouseX, mouseY, partialTicks);
		for (EditBox textField : this.poseTextFields)
			textField.render(poseStack, mouseX, mouseY, partialTicks);

		int offsetY = 20;

		// left column labels
		int offsetX = 20;
		for (int i = 0; i < this.buttonLabels.length; i++) {
			int x = offsetX;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			drawString(poseStack, this.font, I18n.get("armorposer.gui.label." + this.buttonLabels[i]), x, y, whiteColor);
		}

		// right column labels
		offsetX = this.width - 20 - 100;
		// x, y, z
		drawString(poseStack, this.font, "X", offsetX + 10, 7, whiteColor);
		drawString(poseStack, this.font, "Y", offsetX + 45, 7, whiteColor);
		drawString(poseStack, this.font, "Z", offsetX + 80, 7, whiteColor);
		// pose textboxes
		for (int i = 0; i < this.sliderLabels.length; i++) {
			String translatedLabel = I18n.get("armorposer.gui.label." + this.sliderLabels[i]);
			int x = offsetX - this.font.width(translatedLabel) - 10;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			drawString(poseStack, this.font, translatedLabel, x, y, whiteColor);
		}

		if (Services.PLATFORM.allowScrolling()) {
			poseStack.pushPose();
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
			drawString(poseStack, this.font, new TranslatableComponent("armorposer.gui.label.scroll"), 21, -width + 10, 11184810);
			poseStack.popPose();
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.rotationTextField.tick();
		for (EditBox textField : this.poseTextFields)
			if (textField != null) textField.tick();

		//Disable the Y position field when gravity is enabled (So you can't get it stuck in the ground)
		boolean disabledGravity = this.toggleButtons[2].getValue();
		NumberFieldBox yPositionField = this.poseTextFields[19];

		yPositionField.setEditable(disabledGravity);
		if (disabledGravity) {
			yPositionField.setTooltip(yPositionTooltip);
		} else {
			yPositionField.setFocused(false);
			//Adjust tooltip to show it's disabled
			yPositionField.setTooltip(yPositionTooltipDisabled);
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		boolean typed = super.charTyped(codePoint, modifiers);
		if (typed) {
			this.textFieldUpdated();
		}
		return typed;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		var multiplier = Screen.hasShiftDown() ? 10.0f : 1.0f;
		if (allowScrolling && delta > 0) {
			//Add 1 to the value
			if (rotationTextField.canConsumeInput()) {
				float nextValue = (rotationTextField.getFloat() + multiplier * rotationTextField.scrollMultiplier) % rotationTextField.modValue;
				rotationTextField.setValue(String.valueOf(nextValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			for (NumberFieldBox textField : this.poseTextFields) {
				if (textField.canConsumeInput()) {
					float nextValue = (textField.getFloat() + multiplier * textField.scrollMultiplier) % textField.modValue;
					textField.setValue(String.valueOf(nextValue));
					textField.setCursorPosition(0);
					textField.setHighlightPos(0);
					this.textFieldUpdated();
					return true;
				}
			}
		} else if (allowScrolling && delta < 0) {
			//Remove 1 to the value
			if (rotationTextField.canConsumeInput()) {
				float previousValue = (rotationTextField.getFloat() - multiplier * rotationTextField.scrollMultiplier) % rotationTextField.modValue;
				rotationTextField.setValue(String.valueOf(previousValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			for (NumberFieldBox textField : this.poseTextFields) {
				if (textField.canConsumeInput()) {
					float previousValue = (textField.getFloat() - multiplier * textField.scrollMultiplier) % textField.modValue;
					textField.setValue(String.valueOf(previousValue));
					textField.setCursorPosition(0);
					textField.setHighlightPos(0);
					this.textFieldUpdated();
					return true;
				}
			}
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 15) { //Tab
			for (int i = 0; i < this.poseTextFields.length; i++) {
				if (this.poseTextFields[i].isFocused()) {
					this.textFieldUpdated();
					this.poseTextFields[i].moveCursorToEnd();
					this.poseTextFields[i].setFocused(false);

					int j = (!Screen.hasShiftDown() ? (i == this.poseTextFields.length - 1 ? 0 : i + 1) : (i == 0 ? this.poseTextFields.length - 1 : i - 1));
					this.poseTextFields[j].setFocused(true);
					this.poseTextFields[j].moveCursorTo(0);
					this.poseTextFields[j].setHighlightPos(this.poseTextFields[j].getValue().length());
				}
			}
		} else {
			if (this.rotationTextField.keyPressed(keyCode, scanCode, modifiers)) {
				this.textFieldUpdated();
				return true;
			} else {
				for (NumberFieldBox textField : this.poseTextFields) {
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
		return super.mouseClicked(mouseX, mouseY, button);
	}

	protected void textFieldUpdated() {
		this.updateEntity(this.writeFieldsToNBT());
	}

	protected CompoundTag writeFieldsToNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putBoolean("Invisible", this.toggleButtons[0].getValue());
		compound.putBoolean("NoBasePlate", this.toggleButtons[1].getValue());
		compound.putBoolean("NoGravity", this.toggleButtons[2].getValue());
		compound.putBoolean("ShowArms", this.toggleButtons[3].getValue());
		compound.putBoolean("Small", this.toggleButtons[4].getValue());
		compound.putBoolean("CustomNameVisible", this.toggleButtons[5].getValue());
		compound.putBoolean("Invulnerable", this.lockButton.isLocked());
		compound.putInt("DisabledSlots", this.lockButton.isLocked() ? 4144959 : 0);

		ListTag rotationTag = new ListTag();
		rotationTag.add(FloatTag.valueOf(this.rotationTextField.getFloat()));
		compound.put("Rotation", rotationTag);

		CompoundTag poseTag = new CompoundTag();

		ListTag poseHeadTag = new ListTag();
		poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[0].getFloat()));
		poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[1].getFloat()));
		poseHeadTag.add(FloatTag.valueOf(this.poseTextFields[2].getFloat()));
		poseTag.put("Head", poseHeadTag);

		ListTag poseBodyTag = new ListTag();
		poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[3].getFloat()));
		poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[4].getFloat()));
		poseBodyTag.add(FloatTag.valueOf(this.poseTextFields[5].getFloat()));
		poseTag.put("Body", poseBodyTag);

		ListTag poseLeftLegTag = new ListTag();
		poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[6].getFloat()));
		poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[7].getFloat()));
		poseLeftLegTag.add(FloatTag.valueOf(this.poseTextFields[8].getFloat()));
		poseTag.put("LeftLeg", poseLeftLegTag);

		ListTag poseRightLegTag = new ListTag();
		poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[9].getFloat()));
		poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[10].getFloat()));
		poseRightLegTag.add(FloatTag.valueOf(this.poseTextFields[11].getFloat()));
		poseTag.put("RightLeg", poseRightLegTag);

		ListTag poseLeftArmTag = new ListTag();
		poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[12].getFloat()));
		poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[13].getFloat()));
		poseLeftArmTag.add(FloatTag.valueOf(this.poseTextFields[14].getFloat()));
		poseTag.put("LeftArm", poseLeftArmTag);

		ListTag poseRightArmTag = new ListTag();
		poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[15].getFloat()));
		poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[16].getFloat()));
		poseRightArmTag.add(FloatTag.valueOf(this.poseTextFields[17].getFloat()));
		poseTag.put("RightArm", poseRightArmTag);


		var offsetX = this.poseTextFields[18].getFloat();
		var offsetY = this.poseTextFields[19].getFloat();
		var offsetZ = this.poseTextFields[20].getFloat();
		var offsetXDiff = offsetX - this.lastSendOffset.x;
		var offsetYDiff = offsetY - this.lastSendOffset.y;
		var offsetZDiff = offsetZ - this.lastSendOffset.z;
		ListTag positionOffset = new ListTag();
		positionOffset.add(DoubleTag.valueOf(offsetXDiff));
		positionOffset.add(DoubleTag.valueOf(offsetYDiff));
		positionOffset.add(DoubleTag.valueOf(offsetZDiff));
		compound.put("Move", positionOffset);
		this.lastSendOffset = new Vec3(offsetX, offsetY, offsetZ);

		compound.put("Pose", poseTag);
		return compound;
	}

	protected void readFieldsFromNBT(CompoundTag compound) {
		CompoundTag armorStandTag = this.armorStandData.writeToNBT();
		armorStandTag.merge(compound);
		this.armorStandData.readFromNBT(armorStandTag);

		this.rotationTextField.setValue(String.valueOf((int) armorStandData.rotation));
		for (int i = 0; i < this.poseTextFields.length; i++) {
			this.poseTextFields[i].setValue(String.valueOf((int) armorStandData.pose[i]));
		}
	}


	public static void openScreen(ArmorStand armorStandEntity) {
		Minecraft.getInstance().setScreen(new ArmorStandScreen(armorStandEntity));
	}

	public void updateEntity(CompoundTag compound) {
		Services.PLATFORM.updateEntity(this.entityArmorStand, compound);
	}

	public Button.OnTooltip createTooltip(Component component) {
		return new Button.OnTooltip() {
			@Override
			public void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
				if (!button.active) {
					ArmorStandScreen.this.renderTooltip(poseStack, component, mouseX, mouseY);
				}
			}

			@Override
			public void narrateTooltip(Consumer<Component> componentConsumer) {
				componentConsumer.accept(component);
			}
		};
	}

	public NumberFieldBox.OnTooltip createFieldTooltip(Component component) {
		return new NumberFieldBox.OnTooltip() {
			@Override
			public void onTooltip(NumberFieldBox button, PoseStack poseStack, int mouseX, int mouseY) {
				if (!button.active) {
					ArmorStandScreen.this.renderTooltip(poseStack, component, mouseX, mouseY);
				}
			}

			@Override
			public void narrateTooltip(Consumer<Component> componentConsumer) {
				componentConsumer.accept(component);
			}
		};
	}
}