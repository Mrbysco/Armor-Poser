package com.mrbysco.armorposer.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.NumberFieldBox;
import com.mrbysco.armorposer.client.gui.widgets.SizeField;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.platform.Services;
import com.mrbysco.armorposer.util.ArmorStandData;
import com.mrbysco.armorposer.util.ArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

public class ArmorStandScreen extends Screen {
	private static final WidgetSprites MIRROR_POSE_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_pose"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_pose_highlighted")
	);
	private static final WidgetSprites MIRROR_LEGS_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_legs"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_legs_highlighted")
	);
	private static final WidgetSprites MIRROR_ARMS_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_arms"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_arms_highlighted")
	);
	private static final WidgetSprites SWAP_TO_HEAD_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/swap_to_head"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/swap_to_head_highlighted")
	);
	private static final WidgetSprites MIRROR_HANDS_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_hands"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/mirror_hands_highlighted")
	);
	private static final WidgetSprites BLOCK_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/block"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/block_highlighted")
	);
	private static final WidgetSprites ITEM_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/item"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/item_highlighted")
	);
	private static final WidgetSprites TOOL_SPRITES = new WidgetSprites(
			ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/tool"), ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "widget/tool_highlighted")
	);
	private final ArmorStand entityArmorStand;
	private final ArmorStandData armorStandData;

	private final String[] buttonLabels = new String[]{"invisible", "no_base_plate", "no_gravity", "show_arms", "small", "name_visible", "rotation", "scale"};
	private final String[] sliderLabels = new String[]{"head", "body", "left_leg", "right_leg", "left_arm", "right_arm", "position"};
	private final String version;

	private NumberFieldBox rotationTextField;
	private final ToggleButton[] toggleButtons = new ToggleButton[6];
	protected final NumberFieldBox[] poseTextFields = new NumberFieldBox[3 * 7];
	private SizeField sizeField;
	private LockIconButton lockButton;
	private final boolean allowScrolling;

	private Vec3 lastSendOffset = new Vec3(0, 0, 0);

	//Cache the tooltip, so we don't have to create a new one every tick
	private final Tooltip yPositionTooltip = Tooltip.create(Component.translatable("armorposer.gui.tooltip.y_position"));
	private final Tooltip yPositionTooltipDisabled = Tooltip.create(Component.translatable("armorposer.gui.tooltip.y_position.disabled").withStyle(ChatFormatting.RED));

	private final int whiteColor = 16777215;

	public ArmorStandScreen(ArmorStand entityArmorStand) {
		super(Component.translatable("armorposer.gui.title"));
		this.entityArmorStand = entityArmorStand;

		this.armorStandData = new ArmorStandData();
		CompoundTag tag = entityArmorStand.saveWithoutId(new CompoundTag());
		if (!tag.contains("Pose") || tag.getCompound("Pose").isEmpty()) {
			tag.put("Pose", ArmorUtil.writeAllPoses(entityArmorStand));
		}
		this.armorStandData.readFromNBT(tag);

		this.allowScrolling = Services.PLATFORM.allowScrolling();
		this.version = Services.PLATFORM.getModVersion();
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

			this.addRenderableWidget(this.toggleButtons[i] = new ToggleButton.Builder(this.armorStandData.getBooleanValue(i), (button) -> {
				ToggleButton toggleButton = ((ToggleButton) button);
				toggleButton.setValue(!toggleButton.getValue());
				this.textFieldUpdated();
			}).bounds(x, y, width, height).build());
			this.toggleButtons[i].setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip." + buttonLabels[i])));
		}

		// rotation textbox
		this.rotationTextField = new NumberFieldBox(this.font, 1 + offsetX, 1 + offsetY + (this.toggleButtons.length * 22), 38, 17, Component.translatable("armorposer.gui.label.rotation"));
		this.rotationTextField.setValue(String.valueOf((int) this.armorStandData.rotation));
		this.rotationTextField.setMaxLength(4);
		this.addWidget(this.rotationTextField);
		this.rotationTextField.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.rotation")));

		// Size slider
		this.sizeField = new SizeField(this.font, 1 + offsetX, offsetY + ((this.toggleButtons.length + 1) * 22), 38, 17, Component.translatable("armorposer.gui.label.scale"));
		this.sizeField.setValue(String.valueOf((double) this.entityArmorStand.getScale()));
		this.sizeField.setMaxLength(4);
		this.addWidget(this.sizeField);
		this.sizeField.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.scale")));
		if (minecraft != null && !Reference.canResize(minecraft.player)) {
			this.sizeField.active = false;
			this.sizeField.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.size.disabled").withStyle(ChatFormatting.RED)));
		}

		// pose textboxes
		offsetX = this.width - 20 - 100;
		for (int i = 0; i < this.poseTextFields.length; i++) {
			int x = 1 + offsetX + ((i % 3) * 35);
			int y = 1 + offsetY + ((i / 3) * 22);
			int width = 28;
			int height = 17;
			String value = String.valueOf((int) this.armorStandData.pose[i]);

			this.poseTextFields[i] = new NumberFieldBox(this.font, x, y, width, height, Component.literal(value));
			this.poseTextFields[i].setValue(value);
			this.poseTextFields[i].setMaxLength(4);

			boolean lastRow = i >= 3 * 6 && i < 3 * 7;
			if (lastRow) {
				this.poseTextFields[i].scrollMultiplier = 0.01f;
				this.poseTextFields[i].modValue = Integer.MAX_VALUE;
				this.poseTextFields[i].decimalPoints = 2;
				this.poseTextFields[i].allowDecimal = true;
				this.poseTextFields[i].setMaxLength(6);
			}

			//Set tooltip
			if (i % 3 == 0) {
				this.poseTextFields[i].setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip." + (lastRow ? "x_position" : "x_rotation"))));
			} else if (i % 3 == 1) {
				this.poseTextFields[i].setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip." + (lastRow ? "y_position" : "y_rotation"))));
			} else {
				this.poseTextFields[i].setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip." + (lastRow ? "z_position" : "z_rotation"))));
			}

			this.addWidget(this.poseTextFields[i]);
		}

		offsetY = this.height / 4 + 134;

		// copy & paste buttons
		offsetX = 20;
		this.addRenderableWidget(Button.builder(Component.translatable("armorposer.gui.label.poses"), (button) -> this.minecraft.setScreen(new ArmorPosesScreen(this)))
				.bounds(offsetX, offsetY, 130, 20)
				.tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.poses"))).build());
		this.addRenderableWidget(Button.builder(Component.translatable("armorposer.gui.label.copy"), (button) -> {
			CompoundTag compound = this.writeFieldsToNBT();
			String clipboardData = compound.toString();
			if (this.minecraft != null) {
				this.minecraft.keyboardHandler.setClipboard(clipboardData);
			}
		}).bounds(offsetX, offsetY + 22, 42, 20).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.copy"))).build());
		this.addRenderableWidget(Button.builder(Component.translatable("armorposer.gui.label.paste"), (button) -> {
			try {
				String clipboardData = null;
				if (this.minecraft != null) {
					clipboardData = this.minecraft.keyboardHandler.getClipboard();
				}
				if (clipboardData != null) {
					CompoundTag compound = TagParser.parseTag(clipboardData);
					this.readFieldsFromNBT(compound);
					this.textFieldUpdated();
				}
			} catch (Exception e) {
				//Nope
			}
		}).bounds(offsetX + 44, offsetY + 22, 42, 20).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.paste"))).build());
		this.addRenderableWidget(Button.builder(Component.translatable("armorposer.gui.label.save"), (button) -> {
			this.minecraft.setScreen(new SavePoseScreen(this));
		}).bounds(offsetX + 88, offsetY + 22, 42, 20).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.save"))).build());

		offsetX = this.width - 20;
		int buttonsLeft = 9;
		int buttonOffset = -4;
		ImageButton mirrorPose = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, MIRROR_POSE_SPRITES, (button) -> {
			//Mirror head
			float[] head = new float[]{poseTextFields[0].getFloat(), poseTextFields[1].getFloat(), poseTextFields[2].getFloat()};
			poseTextFields[0].setValue(String.valueOf(head[0]));
			poseTextFields[1].setValue(String.valueOf(head[1] != 0 ? -head[1] : 0));
			poseTextFields[2].setValue(String.valueOf(head[2] != 0 ? -head[2] : 0));

			//Mirror Body
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
			this.textFieldUpdated();
		}));
		mirrorPose.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.mirror")));
		buttonsLeft--;

		ImageButton mirrorLegs = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, MIRROR_LEGS_SPRITES, (button) -> {
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
			this.textFieldUpdated();
		}));
		mirrorLegs.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.mirror_legs")));
		buttonsLeft--;

		ImageButton mirrorArms = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, MIRROR_ARMS_SPRITES, (button) -> {
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
			this.textFieldUpdated();
		}));
		mirrorArms.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.mirror_arms")));
		buttonsLeft--;

		ImageButton swapToHead = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, SWAP_TO_HEAD_SPRITES, (button) -> {
			//Swap item in main hand with head
			Services.PLATFORM.swapSlots(this.entityArmorStand, SwapData.Action.SWAP_WITH_HEAD);

		}));
		swapToHead.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.swap_head")));

		buttonsLeft--;

		ImageButton swapHands = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, MIRROR_HANDS_SPRITES, (button) -> {
			//Swap item in main and offhand
			Services.PLATFORM.swapSlots(this.entityArmorStand, SwapData.Action.SWAP_HANDS);
		}));
		swapHands.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.swap_hands")));
		buttonsLeft--;

		ImageButton blockButton = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, BLOCK_SPRITES, (button) -> {
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

		}));
		blockButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.block")));
		buttonsLeft--;

		ImageButton itemButton = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, ITEM_SPRITES, (button) -> {
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
		}));
		itemButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.item").append("\n").append(Component.translatable("armorposer.gui.tooltip.item2").withStyle(ChatFormatting.GRAY))));
		buttonsLeft--;

		ImageButton toolButton = this.addRenderableWidget(new ImageButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, 20, 20, TOOL_SPRITES, (button) -> {
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
		}));
		toolButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.tool")));
		buttonsLeft--;

		this.addRenderableWidget(this.lockButton = new LockIconButton(offsetX - (22 * buttonsLeft) - buttonOffset, offsetY, (button) -> {
			this.lockButton.setLocked(!this.lockButton.isLocked());
			this.textFieldUpdated();
		}));
		this.lockButton.setLocked(this.armorStandData.getBooleanValue(6));
		this.lockButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.lock")));

		// done & cancel buttons
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (button) -> {
			this.textFieldUpdated();
			this.minecraft.setScreen((Screen) null);
		}).bounds(offsetX - ((2 * 96) + 2), offsetY + 22, 97, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (button) -> {
			this.poseTextFields[18].setValue("0");
			this.poseTextFields[19].setValue("0");
			this.poseTextFields[20].setValue("0");
			this.textFieldUpdated();
			this.updateEntity(this.armorStandData.writeToNBT());
			this.minecraft.setScreen((Screen) null);
		}).bounds(offsetX - 95, offsetY + 22, 97, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("💡"), (button) -> {
			this.minecraft.setScreen(new ArmorGlowScreen(this));
		}).bounds(0, 0, 16, 16).build());
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		// Draw gui title
		guiGraphics.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 10, whiteColor, true);

		// Draw textboxes
		this.rotationTextField.render(guiGraphics, mouseX, mouseY, partialTicks);
		for (EditBox textField : this.poseTextFields)
			textField.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.sizeField.render(guiGraphics, mouseX, mouseY, partialTicks);

		int offsetY = 20;

		// left column labels
		int offsetX = 20;
		for (int i = 0; i < this.buttonLabels.length; i++) {
			int x = offsetX;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			guiGraphics.drawString(this.font, I18n.get("armorposer.gui.label." + this.buttonLabels[i]), x, y, whiteColor, true);
		}

		// right column labels
		offsetX = this.width - 20 - 100;
		// x, y, z
		guiGraphics.drawString(this.font, "X", offsetX + 10, 7, whiteColor, true);
		guiGraphics.drawString(this.font, "Y", offsetX + 45, 7, whiteColor, true);
		guiGraphics.drawString(this.font, "Z", offsetX + 80, 7, whiteColor, true);
		// pose textboxes
		for (int i = 0; i < this.sliderLabels.length; i++) {
			String translatedLabel = I18n.get("armorposer.gui.label." + this.sliderLabels[i]);
			int x = offsetX - this.font.width(translatedLabel) - 10;
			int y = offsetY + (i * 22) + (10 - (this.font.lineHeight / 2));
			guiGraphics.drawString(this.font, translatedLabel, x, y, whiteColor, true);
		}

		PoseStack poseStack = guiGraphics.pose();
		if (Services.PLATFORM.allowScrolling()) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			guiGraphics.drawString(this.font, Component.translatable("armorposer.gui.label.scroll", version), 21, -width + 10, 11184810, true);
			poseStack.popPose();
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		//Nope
	}

	@Override
	public void tick() {
		super.tick();

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
	public boolean mouseScrolled(double mouseX, double mouseY, double xScroll, double yScroll) {
		var multiplier = Screen.hasShiftDown() ? 10.0f : 1.0f;
		if (allowScrolling && (xScroll > 0 || yScroll > 0)) {
			//Add 1 to the value
			if (rotationTextField.canConsumeInput()) {
				float nextValue = (rotationTextField.getFloat() + multiplier * rotationTextField.scrollMultiplier) % rotationTextField.modValue;
				rotationTextField.setValue(String.valueOf(nextValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			if (sizeField.canConsumeInput()) {
				float nextValue = (float)(sizeField.getFloat() + (double)(multiplier * sizeField.scrollMultiplier));
				nextValue = Math.clamp(nextValue, sizeField.minValue, sizeField.maxValue);
				sizeField.setValue(String.valueOf(nextValue));
				sizeField.setCursorPosition(0);
				sizeField.setHighlightPos(0);
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
		} else if (allowScrolling && (xScroll < 0 || yScroll < 0)) {
			//Remove 1 to the value
			if (rotationTextField.canConsumeInput()) {
				float previousValue = (rotationTextField.getFloat() - multiplier * rotationTextField.scrollMultiplier) % rotationTextField.modValue;
				rotationTextField.setValue(String.valueOf(previousValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			if (sizeField.canConsumeInput()) {
				float previousValue = (float)(sizeField.getFloat() - (double)(multiplier * sizeField.scrollMultiplier));
				previousValue = Math.clamp(previousValue, sizeField.minValue, sizeField.maxValue);
				sizeField.setValue(String.valueOf(previousValue));
				sizeField.setCursorPosition(0);
				sizeField.setHighlightPos(0);
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
		return super.mouseScrolled(mouseX, mouseY, xScroll, yScroll);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 15) { //Tab
			for (int i = 0; i < this.poseTextFields.length; i++) {
				if (this.poseTextFields[i].isFocused()) {
					this.textFieldUpdated();
					this.poseTextFields[i].moveCursorToEnd(false);
					this.poseTextFields[i].setFocused(false);

					int j = (!Screen.hasShiftDown() ? (i == this.poseTextFields.length - 1 ? 0 : i + 1) : (i == 0 ? this.poseTextFields.length - 1 : i - 1));
					this.poseTextFields[j].setFocused(true);
					this.poseTextFields[j].moveCursorTo(0, false);
					this.poseTextFields[j].setHighlightPos(this.poseTextFields[j].getValue().length());
				}
			}
		} else {
			if (this.rotationTextField.keyPressed(keyCode, scanCode, modifiers)) {
				this.textFieldUpdated();
				return true;
			} else if (this.sizeField.keyPressed(keyCode, scanCode, modifiers)) {
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
		compound.putDouble("Scale", this.sizeField.getFloat());

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

		// Set toggle buttons
		this.toggleButtons[0].setValue(compound.getBoolean("Invisible"));
		this.toggleButtons[1].setValue(compound.getBoolean("NoBasePlate"));
		this.toggleButtons[2].setValue(compound.getBoolean("NoGravity"));
		this.toggleButtons[3].setValue(compound.getBoolean("ShowArms"));
		this.toggleButtons[4].setValue(compound.getBoolean("Small"));
		this.toggleButtons[5].setValue(compound.getBoolean("CustomNameVisible"));

		// Set lock button
		this.lockButton.setLocked(compound.getBoolean("Invulnerable"));

		// Set size field
		this.sizeField.setValue(String.valueOf(compound.getDouble("Scale")));

		// Set rotation text field
		ListTag rotationTag = compound.getList("Rotation", 5); // 5 is the type for float
		if (!rotationTag.isEmpty()) {
			this.rotationTextField.setValue(String.valueOf(rotationTag.getFloat(0)));
		}

		// Set pose text fields
		CompoundTag poseTag = compound.getCompound("Pose");

		ListTag poseHeadTag = poseTag.getList("Head", 5);
		this.poseTextFields[0].setValue(String.valueOf(poseHeadTag.getFloat(0)));
		this.poseTextFields[1].setValue(String.valueOf(poseHeadTag.getFloat(1)));
		this.poseTextFields[2].setValue(String.valueOf(poseHeadTag.getFloat(2)));

		ListTag poseBodyTag = poseTag.getList("Body", 5);
		this.poseTextFields[3].setValue(String.valueOf(poseBodyTag.getFloat(0)));
		this.poseTextFields[4].setValue(String.valueOf(poseBodyTag.getFloat(1)));
		this.poseTextFields[5].setValue(String.valueOf(poseBodyTag.getFloat(2)));

		ListTag poseLeftLegTag = poseTag.getList("LeftLeg", 5);
		this.poseTextFields[6].setValue(String.valueOf(poseLeftLegTag.getFloat(0)));
		this.poseTextFields[7].setValue(String.valueOf(poseLeftLegTag.getFloat(1)));
		this.poseTextFields[8].setValue(String.valueOf(poseLeftLegTag.getFloat(2)));

		ListTag poseRightLegTag = poseTag.getList("RightLeg", 5);
		this.poseTextFields[9].setValue(String.valueOf(poseRightLegTag.getFloat(0)));
		this.poseTextFields[10].setValue(String.valueOf(poseRightLegTag.getFloat(1)));
		this.poseTextFields[11].setValue(String.valueOf(poseRightLegTag.getFloat(2)));

		ListTag poseLeftArmTag = poseTag.getList("LeftArm", 5);
		this.poseTextFields[12].setValue(String.valueOf(poseLeftArmTag.getFloat(0)));
		this.poseTextFields[13].setValue(String.valueOf(poseLeftArmTag.getFloat(1)));
		this.poseTextFields[14].setValue(String.valueOf(poseLeftArmTag.getFloat(2)));

		ListTag poseRightArmTag = poseTag.getList("RightArm", 5);
		this.poseTextFields[15].setValue(String.valueOf(poseRightArmTag.getFloat(0)));
		this.poseTextFields[16].setValue(String.valueOf(poseRightArmTag.getFloat(1)));
		this.poseTextFields[17].setValue(String.valueOf(poseRightArmTag.getFloat(2)));

		// Set position offsets
		ListTag positionOffset = compound.getList("Move", 6); // 6 is the type for double
		if (!positionOffset.isEmpty()) {
			this.poseTextFields[18].setValue(String.valueOf(positionOffset.getDouble(0) + this.lastSendOffset.x));
			this.poseTextFields[19].setValue(String.valueOf(positionOffset.getDouble(1) + this.lastSendOffset.y));
			this.poseTextFields[20].setValue(String.valueOf(positionOffset.getDouble(2) + this.lastSendOffset.z));
			this.lastSendOffset = new Vec3(
					positionOffset.getDouble(0) + this.lastSendOffset.x,
					positionOffset.getDouble(1) + this.lastSendOffset.y,
					positionOffset.getDouble(2) + this.lastSendOffset.z
			);
		}
	}

	public static void openScreen(ArmorStand armorStandEntity) {
		Minecraft.getInstance().setScreen(new ArmorStandScreen(armorStandEntity));
	}

	public void updateEntity(CompoundTag compound) {
		Services.PLATFORM.updateEntity(this.entityArmorStand, compound);
	}
}