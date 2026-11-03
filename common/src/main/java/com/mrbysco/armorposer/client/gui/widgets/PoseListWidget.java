package com.mrbysco.armorposer.client.gui.widgets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.ArmorPosesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class PoseListWidget extends ObjectSelectionList<PoseListWidget.ListEntry> {
	private final ArmorPosesScreen parent;
	private final int listWidth;
	private final Component title;

	public PoseListWidget(ArmorPosesScreen parent, Component title, boolean user, int listWidth, int top, int bottom) {
		super(parent.getScreenMinecraft(), listWidth, bottom - top, top, parent.getScreenFont().lineHeight * 2 + 16);
		this.parent = parent;
		this.title = title;
		this.listWidth = listWidth;
		this.refreshList(user);
	}

	@Override
	protected int scrollBarX() {
		return this.getX() + this.listWidth - 6;
	}

	@Override
	public int getRowWidth() {
		return this.listWidth;
	}

	public void refreshList(boolean user) {
		this.clearEntries();
		if (user)
			parent.buildUserPoseList(this::addEntry, location -> new ListEntry(location, this.parent));
		else
			parent.buildPoseList(this::addEntry, location -> new ListEntry(location, this.parent));
		this.setScrollAmount(this.scrollAmount());
	}

	@Override
	protected void extractSelection(GuiGraphicsExtractor guiGraphics, ListEntry listEntry, int outlineColor) {
		int x1 = listEntry.getX();
		int y1 = listEntry.getY();
		int x2 = x1 + listEntry.getWidth();
		int y2 = y1 + listEntry.getHeight();
		guiGraphics.fillGradient(x1, y1, x2, y2, -1945083888, -1676648432);
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
		guiGraphics.fillGradient(getX(), 0, getX() + this.listWidth, parent.height, -1945104368, -1676668912);
		super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, a);
		guiGraphics.centeredText(this.parent.getScreenFont(), title, getX() + this.listWidth / 2, 2, ARGB.opaque(16777215));
	}

	@Override
	public void setSelected(@Nullable PoseListWidget.ListEntry selected) {
		this.parent.setSelected(getSelected(), selected);
		super.setSelected(selected);
	}

	public class ListEntry extends Entry<ListEntry> {
		private final PoseEntry poseEntry;
		private final ArmorPosesScreen parent;
		private final ArmorStandRenderState armorStandPreview = new ArmorStandRenderState();

		ListEntry(PoseEntry entry, ArmorPosesScreen parent) {
			this.poseEntry = entry;
			this.parent = parent;

			this.armorStandPreview.entityType = EntityTypes.ARMOR_STAND;
			this.armorStandPreview.xRot = 25.0F;
			this.armorStandPreview.bodyRot = 210.0F;

			Minecraft mc = parent.getScreenMinecraft();
			if (mc == null) {
				Reference.LOGGER.error("Minecraft is null, cannot create pose entry {}", entry.pose().name());
				return;
			}
			Level level = mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null ? mc.getSingleplayerServer().getAllLevels().iterator().next() : mc.level;
			if (level != null) {
				try {
					CompoundTag tag = TagParser.parseCompoundFully(entry.pose().data());

					CompoundTag nbt = new CompoundTag();
					nbt.putString("id", "minecraft:armor_stand");
					if (!tag.isEmpty()) {
						nbt.merge(tag);
					}
					ArmorStand armorStand = (ArmorStand) EntityType.loadEntityRecursive(EntityTypes.ARMOR_STAND, nbt, level, EntitySpawnReason.LOAD, entity -> {
						if (entity instanceof ArmorStand stand) {
							stand.setNoBasePlate(true);
							stand.setShowArms(true);
							stand.yBodyRot = 210.0F;
							stand.setXRot(25.0F);
							stand.yHeadRot = stand.getYRot();
							stand.yHeadRotO = stand.getYRot();
						}
						return entity;
					});
					if (armorStand != null) {
						this.updateState(armorStand);
					}
				} catch (Exception e) {
					Reference.LOGGER.error("Unable to parse nbt pose {}", e.getMessage());
				}
			}
		}

		public void updateState(ArmorStand stand) {
			this.armorStandPreview.showBasePlate = stand.showBasePlate();
			this.armorStandPreview.isSmall = stand.isSmall();
			this.armorStandPreview.showArms = stand.showArms();
			this.armorStandPreview.isInvisible = stand.isInvisible();
			this.armorStandPreview.nameTag = stand.getCustomName();

			this.armorStandPreview.headPose = stand.getHeadPose();
			this.armorStandPreview.bodyPose = stand.getBodyPose();
			this.armorStandPreview.leftArmPose = stand.getLeftArmPose();
			this.armorStandPreview.rightArmPose = stand.getRightArmPose();
			this.armorStandPreview.leftLegPose = stand.getLeftLegPose();
			this.armorStandPreview.rightLegPose = stand.getRightLegPose();
		}

		@Override
		public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
			int left = getContentX();
			int top = getContentY();
			Matrix3x2fStack pose = guiGraphics.pose();
			pose.pushMatrix();
			pose.translate(0, top - ((float) height / 2));
			extractScrollingStringOverContents(guiGraphics.textRenderer(), getName(), 18);

			if (getSelected() == this)
				extractPose(guiGraphics, left + 16, top + 28, partialTick);
			pose.popMatrix();
		}

		public void extractPose(GuiGraphicsExtractor guiGraphics, int xPos, int yPos, float partialTick) {
			if (armorStandPreview != null) {
				int startX = xPos - 40;
				int startY = yPos - 60;
				int endX = xPos + 40;
				int endY = yPos + 60;

				guiGraphics.entity(this.armorStandPreview, 20.0F,
						Reference.ARMOR_STAND_TRANSLATION, Reference.ARMOR_STAND_ANGLE, null, startX, startY, endX, endY);
			}
		}

		public CompoundTag getTag() {
			return poseEntry.getTag();
		}

		public Component getName() {
			return poseEntry.getName();
		}

		public boolean userAdded() {
			return poseEntry.userAdded();
		}

		public String rawName() {
			return poseEntry.pose().name();
		}

		@Override
		public Component getNarration() {
			return getName();
		}
	}
}
