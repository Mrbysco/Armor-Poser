package com.mrbysco.armorposer.client.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

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
	private final SavePoseScreen savePoseScreen;

	private final String[] buttonLabels = new String[]{"invisible", "base_plate", "gravity", "show_arms", "small", "name_visible", "rotation", "scale"};
	private final String[] sliderLabels = new String[]{"head", "body", "left_leg", "right_leg", "left_arm", "right_arm", "position"};
	private final String version;

	private EditBox nameField;
	private String oldName;
	private String changedName;
	private Button renameButton;

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

	private final int whiteColor = ARGB.opaque(16777215);

	public ArmorStandScreen(ArmorStand entityArmorStand) {
		super(Component.translatable("armorposer.gui.title"));
		this.entityArmorStand = entityArmorStand;
		this.oldName = entityArmorStand.hasCustomName() ? entityArmorStand.getName().getString() : this.getTitle().getString();

		this.armorStandData = new ArmorStandData();
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(Reference.LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, entityArmorStand.registryAccess());
			entityArmorStand.saveWithoutId(output);
			CompoundTag tag = output.buildResult();

			if (tag.getCompoundOrEmpty("Pose").isEmpty()) {
				CompoundTag poseTag = ArmorUtil.writeAllPoses(entityArmorStand);
				tag.put("Pose", poseTag);
			}
			this.armorStandData.readFromNBT(tag);
		}

		this.allowScrolling = Services.PLATFORM.allowScrolling();
		this.version = Services.PLATFORM.getModVersion();
		this.savePoseScreen = new SavePoseScreen(this);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		super.init();

		this.nameField = new EditBox(this.font,
				this.width / 2 - this.font.width(this.oldName) / 2, 10,
				100, 20,
				Component.translatable("armorposer.gui.label.name")
		);
		this.nameField.setValue(this.oldName);
		this.nameField.setTextColor(whiteColor);
		this.nameField.setTextColorUneditable(whiteColor);
		this.nameField.setBordered(false);
		this.nameField.setMaxLength(50);
		this.nameField.setTextShadow(true);
		this.nameField.setFocused(false);
		this.nameField.setResponder((text) -> {
			this.changedName = text;
			this.updateRenameButton();
		});
		this.addWidget(this.nameField);
		this.addRenderableWidget(this.renameButton = Button.builder(Component.translatable("armorposer.gui.label.rename"), (button) -> {
					if (this.hasLevels() && !this.oldName.equals(this.changedName)) {
						this.entityArmorStand.setCustomName(Component.literal(this.changedName));
						Services.PLATFORM.renameArmorStand(this.entityArmorStand, this.changedName);
						this.oldName = this.changedName;
						this.updateRenameButton();
					}
				})
				.bounds(this.width / 2, 24, 40, 20)
				.tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.rename"))).build());
		this.renameButton.visible = false;
		this.renameButton.active = false;

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
			this.sizeField.setEditable(false);
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
					CompoundTag compound = TagParser.parseCompoundFully(clipboardData);
					compound.putBoolean("NoBasePlate", !compound.getBooleanOr("NoBasePlate", false));
					compound.putBoolean("NoGravity", !compound.getBooleanOr("NoGravity", false));
					this.readFieldsFromNBT(compound);
					this.textFieldUpdated();
				}
			} catch (Exception e) {
				//Nope
			}
		}).bounds(offsetX + 44, offsetY + 22, 42, 20).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.paste"))).build());
		this.addRenderableWidget(Button.builder(Component.translatable("armorposer.gui.label.save"), (button) -> {
			this.minecraft.setScreen(this.savePoseScreen);
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

				CompoundTag tag = TagParser.parseCompoundFully(Reference.alignedBlockPose);
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

					CompoundTag tag = TagParser.parseCompoundFully(Reference.alignedUprightItemPose);
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

					CompoundTag tag = TagParser.parseCompoundFully(Reference.alignedFlatItemPose);
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

				CompoundTag tag = TagParser.parseCompoundFully(Reference.alignedToolPose);
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

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String s = this.nameField.getValue();
		this.init(minecraft, width, height);
		this.nameField.setValue(s);
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

	private boolean hasLevels() {
		if (this.minecraft == null || this.minecraft.player == null) return false;
		if (this.minecraft.player.getAbilities().instabuild) return true;
		return this.minecraft.player.experienceLevel >= 1;
	}

	private void updateRenameButton() {
		if (!this.oldName.equals(this.changedName)) {
			this.renameButton.visible = true;
			if (this.minecraft != null && this.minecraft.player != null) {
				if (!this.hasLevels()) {
					this.renameButton.active = false;
					this.renameButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.rename.disabled").withStyle(ChatFormatting.RED)));
				} else {
					this.renameButton.active = true;
				}
			}
		} else {
			this.renameButton.visible = false;
			this.renameButton.active = false;
			this.renameButton.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.rename")));
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		// Draw textboxes
		// Name
		this.nameField.render(guiGraphics, mouseX, mouseY, partialTicks);

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

		Matrix3x2fStack pose = guiGraphics.pose();
		if (Services.PLATFORM.allowScrolling()) {
			pose.pushMatrix();
			pose.rotate(1.5708F);
			guiGraphics.drawString(this.font, Component.translatable("armorposer.gui.label.scroll", version), 21, -width + 10, ARGB.opaque(11184810), true);
			pose.popMatrix();
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
		boolean gravityEnabled = this.toggleButtons[2].getValue();
		NumberFieldBox yPositionField = this.poseTextFields[19];

		yPositionField.setEditable(!gravityEnabled);
		if (!gravityEnabled) {
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
		if (allowScrolling && yScroll > 0) {
			//Add 1 to the value
			if (rotationTextField.canConsumeInput()) {
				int nextValue = (int) (rotationTextField.getFloat() + (1 * multiplier));
				rotationTextField.setValue(String.valueOf(nextValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			if (sizeField.canConsumeInput()) {
				float nextValue = (float) (sizeField.getFloat() + (double) (multiplier * sizeField.scrollMultiplier));
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
		} else if (allowScrolling && yScroll < 0) {
			//Remove 1 to the value
			if (rotationTextField.canConsumeInput()) {
				int previousValue = (int) (rotationTextField.getFloat() - (1 * multiplier));
				rotationTextField.setValue(String.valueOf(previousValue));
				rotationTextField.setCursorPosition(0);
				rotationTextField.setHighlightPos(0);
				this.textFieldUpdated();
				return true;
			}
			if (sizeField.canConsumeInput()) {
				float previousValue = (float) (sizeField.getFloat() - (double) (multiplier * sizeField.scrollMultiplier));
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
			if (this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
				this.textFieldUpdated();
				return true;
			} else if (this.rotationTextField.keyPressed(keyCode, scanCode, modifiers)) {
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
		compound.putBoolean("NoBasePlate", !this.toggleButtons[1].getValue());
		compound.putBoolean("NoGravity", !this.toggleButtons[2].getValue());
		compound.putBoolean("ShowArms", this.toggleButtons[3].getValue());
		compound.putBoolean("Small", this.toggleButtons[4].getValue());
		compound.putBoolean("CustomNameVisible", this.toggleButtons[5].getValue());
		compound.putBoolean("Invulnerable", this.lockButton.isLocked());
		compound.putInt("DisabledSlots", this.lockButton.isLocked() ? 4144959 : 0);
		compound.putDouble("Scale", this.sizeField.getFloat());

		compound.store("Rotation", Vec2.CODEC, new Vec2(this.rotationTextField.getFloat(), 0.0F)); //Yrot and XRot

		CompoundTag poseTag = new CompoundTag();

		poseTag.store("Head", Rotations.CODEC, new Rotations(
				this.poseTextFields[0].getFloat(), this.poseTextFields[1].getFloat(), this.poseTextFields[2].getFloat()));

		poseTag.store("Body", Rotations.CODEC, new Rotations(
				this.poseTextFields[3].getFloat(), this.poseTextFields[4].getFloat(), this.poseTextFields[5].getFloat()));

		poseTag.store("LeftLeg", Rotations.CODEC, new Rotations(
				this.poseTextFields[6].getFloat(), this.poseTextFields[7].getFloat(), this.poseTextFields[8].getFloat()));

		poseTag.store("RightLeg", Rotations.CODEC, new Rotations(
				this.poseTextFields[9].getFloat(), this.poseTextFields[10].getFloat(), this.poseTextFields[11].getFloat()));

		poseTag.store("LeftArm", Rotations.CODEC, new Rotations(
				this.poseTextFields[12].getFloat(), this.poseTextFields[13].getFloat(), this.poseTextFields[14].getFloat()));

		poseTag.store("RightArm", Rotations.CODEC, new Rotations(
				this.poseTextFields[15].getFloat(), this.poseTextFields[16].getFloat(), this.poseTextFields[17].getFloat()));

		float offsetX = this.poseTextFields[18].getFloat();
		float offsetY = this.poseTextFields[19].getFloat();
		float offsetZ = this.poseTextFields[20].getFloat();
		double offsetXDiff = offsetX - this.lastSendOffset.x;
		double offsetYDiff = offsetY - this.lastSendOffset.y;
		double offsetZDiff = offsetZ - this.lastSendOffset.z;
		compound.store("Move", Vec3.CODEC, new Vec3(offsetXDiff, offsetYDiff, offsetZDiff));
		this.lastSendOffset = new Vec3(offsetX, offsetY, offsetZ);

		compound.put("Pose", poseTag);
		return compound;
	}

	protected void readFieldsFromNBT(CompoundTag compound) {
		CompoundTag armorStandTag = this.armorStandData.writeToNBT();
		armorStandTag.merge(compound);
		this.armorStandData.readFromNBT(armorStandTag);

		// Set toggle buttons
		this.toggleButtons[0].setValue(compound.getBooleanOr("Invisible", false));
		this.toggleButtons[1].setValue(compound.getBooleanOr("NoBasePlate", false));
		this.toggleButtons[2].setValue(compound.getBooleanOr("NoGravity", false));
		this.toggleButtons[3].setValue(compound.getBooleanOr("ShowArms", false));
		this.toggleButtons[4].setValue(compound.getBooleanOr("Small", false));
		this.toggleButtons[5].setValue(compound.getBooleanOr("CustomNameVisible", false));

		// Set lock button
		this.lockButton.setLocked(compound.getBooleanOr("Invulnerable", false));

		// Set size field
		this.sizeField.setValue(String.valueOf(compound.getDoubleOr("Scale", 1.0F)));

		// Set rotation text field
		Optional<Vec2> rotation = compound.read("Rotation", Vec2.CODEC);
		if (rotation.isPresent()) {
			this.rotationTextField.setValue(String.valueOf(rotation.get().x));
		}

		// Set pose text fields
		CompoundTag poseTag = compound.getCompoundOrEmpty("Pose");

		Rotations poseHeadTag = poseTag.read("Head", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[0].setValue(String.valueOf(poseHeadTag.x()));
		this.poseTextFields[1].setValue(String.valueOf(poseHeadTag.y()));
		this.poseTextFields[2].setValue(String.valueOf(poseHeadTag.z()));

		Rotations poseBodyTag = poseTag.read("Body", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[3].setValue(String.valueOf(poseBodyTag.x()));
		this.poseTextFields[4].setValue(String.valueOf(poseBodyTag.y()));
		this.poseTextFields[5].setValue(String.valueOf(poseBodyTag.z()));

		Rotations poseLeftLegTag = poseTag.read("LeftLeg", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[6].setValue(String.valueOf(poseLeftLegTag.x()));
		this.poseTextFields[7].setValue(String.valueOf(poseLeftLegTag.y()));
		this.poseTextFields[8].setValue(String.valueOf(poseLeftLegTag.z()));

		Rotations poseRightLegTag = poseTag.read("RightLeg", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[9].setValue(String.valueOf(poseRightLegTag.x()));
		this.poseTextFields[10].setValue(String.valueOf(poseRightLegTag.y()));
		this.poseTextFields[11].setValue(String.valueOf(poseRightLegTag.z()));

		Rotations poseLeftArmTag = poseTag.read("LeftArm", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[12].setValue(String.valueOf(poseLeftArmTag.x()));
		this.poseTextFields[13].setValue(String.valueOf(poseLeftArmTag.y()));
		this.poseTextFields[14].setValue(String.valueOf(poseLeftArmTag.z()));

		Rotations poseRightArmTag = poseTag.read("RightArm", Rotations.CODEC).orElse(new Rotations(0f, 0f, 0f)); // 5 is the type for float
		this.poseTextFields[15].setValue(String.valueOf(poseRightArmTag.x()));
		this.poseTextFields[16].setValue(String.valueOf(poseRightArmTag.y()));
		this.poseTextFields[17].setValue(String.valueOf(poseRightArmTag.z()));

		// Set position offsets
		Optional<Vec3> optionalOffset = compound.read("Move", Vec3.CODEC);
		if (optionalOffset.isPresent()) {
			Vec3 offset = optionalOffset.get();
			this.poseTextFields[18].setValue(String.valueOf(offset.x() + this.lastSendOffset.x));
			this.poseTextFields[19].setValue(String.valueOf(offset.y() + this.lastSendOffset.y));
			this.poseTextFields[20].setValue(String.valueOf(offset.z() + this.lastSendOffset.z));
			this.lastSendOffset = new Vec3(
					offset.x() + this.lastSendOffset.x,
					offset.y() + this.lastSendOffset.y,
					offset.z() + this.lastSendOffset.z
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