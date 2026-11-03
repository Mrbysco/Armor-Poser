package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.client.GlowHandler;
import com.mrbysco.armorposer.client.GroupHelper;
import com.mrbysco.armorposer.client.gui.widgets.ArmorGlowWidget;
import com.mrbysco.armorposer.client.gui.widgets.RangeSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArmorGlowScreen extends Screen {
	private static final int PADDING = 6;

	private boolean groupMode;
	private EditBox groupField;

	private ArmorGlowWidget[] armorListWidget = new ArmorGlowWidget[2];
	private ArmorGlowWidget.ListEntry selected = null;
	private CycleButton<String> selectedGroupButton;

	private final List<ArmorStand> armorStands;
	private final List<ArmorStand> invisiblearmorStands;
	private Button locateButton;
	private Button modifyButton, modifyButton2;
	private Button addButton, addButton2;
	private Button removeButton, removeButton2;
	private Button clearButton, clearButton2;
	private RangeSlider rangeSlider;

	public final ArmorStandScreen parentScreen;
	private double range = 30;

	public ArmorGlowScreen(ArmorStandScreen parent, boolean groupMode) {
		super(groupMode ? Component.translatable("armorposer.gui.armor_list.group_mode") : Component.translatable("armorposer.gui.armor_list.list"));
		this.groupMode = groupMode;
		this.parentScreen = parent;

		//Add the armor stands to the list
		if (minecraft.player == null || minecraft.level == null)
			this.onClose();

		assert minecraft.level != null;
		List<ArmorStand> armorStands = new ArrayList<>(minecraft.level.getEntitiesOfClass(ArmorStand.class,
				minecraft.player.getBoundingBox().inflate(30.0D), EntitySelector.LIVING_ENTITY_STILL_ALIVE));
		//Sort the list based on how far the armor stand is from the player
		armorStands.sort((armorStand, armorStand2) -> {
			double distance1 = armorStand.distanceToSqr(minecraft.player);
			double distance2 = armorStand2.distanceToSqr(minecraft.player);
			return Double.compare(distance1, distance2);
		});
		armorStands.forEach(GroupHelper::loadGroups);
		this.armorStands = armorStands.stream().filter(stand -> !stand.isInvisible()).toList();
		this.invisiblearmorStands = armorStands.stream().filter(Entity::isInvisible).toList();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		int centerWidth = this.width / 2;
		int listWidth = this.width / 4 + 20;
		int structureWidth = this.width - listWidth - (PADDING * 3);
		int closeButtonWidth = Math.min(structureWidth, 160);
		int y = this.height - 20 - PADDING;
		this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> ArmorGlowScreen.this.onClose())
				.bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

		y -= 18 + PADDING;


		int fullButtonHeight = PADDING + 20 + PADDING;
		this.armorListWidget[0] = new ArmorGlowWidget(this, Component.translatable("armorposer.gui.armor_list.list"),
				true, listWidth, fullButtonHeight, y - getScreenFont().lineHeight - PADDING);
		this.armorListWidget[0].setX(0);
		this.armorListWidget[0].setY(16);
		this.armorListWidget[0].setHeight(this.height);
		addRenderableWidget(armorListWidget[0]);
		armorListWidget[0].refreshList(true);

		this.armorListWidget[1] = new ArmorGlowWidget(this, Component.translatable("armorposer.gui.armor_list.list2"),
				false, listWidth, fullButtonHeight, y - getScreenFont().lineHeight - PADDING);
		this.armorListWidget[1].setX(width - listWidth);
		this.armorListWidget[1].setY(16);
		this.armorListWidget[1].setHeight(this.height);
		addRenderableWidget(armorListWidget[1]);
		armorListWidget[1].refreshList(false);

		if (groupMode) {
			int buttonWidth = (closeButtonWidth / 2) - 1;
			this.addRenderableWidget(this.locateButton = Button.builder(Component.translatable("armorposer.gui.armor_list.locate"), b -> {
				if (selected != null && minecraft.player != null) {
					GlowHandler.startGlowing(this.selected.getArmorStand().getUUID());
					Vec3 standPos = this.selected.getArmorStand().position();
					Vec3 adjustedPos = standPos.add(0, this.selected.getArmorStand().getScale(), 0);
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, adjustedPos);
				}
			}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y - (18 + PADDING), closeButtonWidth, 20).build());

			this.addRenderableWidget(this.modifyButton = Button.builder(Component.translatable("armorposer.gui.armor_list.pose_group"), b -> {
				ArmorStandScreen modifyScreen = new ArmorStandScreen(parentScreen.getArmorStandEntity(), parentScreen.getDisabledFeatures(), parentScreen.getMinScale(), parentScreen.getMaxScale());
				modifyScreen.setGroup(getSelectedGroup());
				minecraft.setScreenAndShow(modifyScreen);
			}).bounds(centerWidth - buttonWidth + 5, y, buttonWidth, 20).build());

			this.addRenderableWidget(this.modifyButton2 = Button.builder(Component.translatable("armorposer.gui.armor_list.pose_group_2"), b -> {
				if (!getGroup().isBlank()) {
					ArmorStandScreen modifyScreen = new ArmorStandScreen(parentScreen.getArmorStandEntity(), parentScreen.getDisabledFeatures(), parentScreen.getMinScale(), parentScreen.getMaxScale());
					modifyScreen.setGroup(getGroup());
					minecraft.setScreenAndShow(modifyScreen);
				}
			}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING + buttonWidth + 2, y, buttonWidth, 20).build());

			Component group = this.parentScreen.getGroup().isBlank() ? Component.empty() : Component.literal(this.parentScreen.getGroup());
			this.groupField = new EditBox(this.font, centerWidth - 46, 18, 94, 20, group);
			this.groupField.setValue(group.getString());
			this.groupField.setMaxLength(16);
			this.groupField.setTextColor(-1);
			this.groupField.setHint(Component.literal("  Custom Group "));
			this.groupField.addFormatter(this::formatLowercase);
			this.addRenderableWidget(this.groupField);

			this.addRenderableWidget(this.removeButton = Button.builder(Component.literal("➖"), b -> {
				if (selected != null && minecraft.player != null) {
					GroupHelper.removeFromGroup(this.getGroup(), selected.getArmorStand());
				}
			}).bounds(centerWidth - 64, 20, 16, 16).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.remove_group"))).build());

			this.addRenderableWidget(this.clearButton = Button.builder(Component.literal("\uD83E\uDDF9"), b -> {
				if (minecraft.player != null) {
					GroupHelper.clearGroup(this.getGroup(), minecraft.level != null ? minecraft.level : parentScreen.getArmorStandEntity().level());
				}
			}).bounds(centerWidth - 82, 20, 16, 16).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.clear_group"))).build());

			this.addRenderableWidget(this.addButton = Button.builder(Component.literal("➕"), b -> {
				if (selected != null && minecraft.player != null) {
					GroupHelper.addToGroup(this.getGroup(), selected.getArmorStand());
				}
			}).bounds(centerWidth + 50, 20, 16, 16).tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.add_group"))).build());

			String defaultGroup = GroupHelper.DEFAULT_GROUP_NAMES.getFirst();
			this.addRenderableWidget(
					this.selectedGroupButton = CycleButton.builder(
									Component::literal,
									defaultGroup
							).withTooltip((value) -> Tooltip.create(Component.translatable("armorposer.gui.tooltip.selected_group", value)))
							.withValues(GroupHelper.DEFAULT_GROUP_NAMES)
							.create(centerWidth - 46, 40, 94, 20, Component.literal("Group"), (button, value) -> {
								this.removeButton2.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.remove_group_2", this.getSelectedGroup())));
								this.clearButton2.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.clear_group_2", this.getSelectedGroup())));
								this.addButton2.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.add_group_2", this.getSelectedGroup())));
							})
			);

			this.addRenderableWidget(this.removeButton2 = Button.builder(Component.literal("➖"), b -> {
						if (selected != null && minecraft.player != null) {
							String selectedGroup = this.getSelectedGroup().toLowerCase(Locale.ROOT);
							GroupHelper.removeFromGroup(selectedGroup, selected.getArmorStand());
						}
					}).bounds(centerWidth - 64, 42, 16, 16)
					.tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.remove_group_2", this.getSelectedGroup()))).build());

			this.addRenderableWidget(this.clearButton2 = Button.builder(Component.literal("\uD83E\uDDF9"), b -> {
						if (minecraft.player != null) {
							String selectedGroup = this.getSelectedGroup().toLowerCase(Locale.ROOT);
							GroupHelper.clearGroup(selectedGroup, minecraft.level != null ? minecraft.level : parentScreen.getArmorStandEntity().level());
						}
					}).bounds(centerWidth - 82, 42, 16, 16)
					.tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.clear_group_2", this.getSelectedGroup()))).build());

			this.addRenderableWidget(this.addButton2 = Button.builder(Component.literal("➕"), b -> {
						if (selected != null && minecraft.player != null) {
							String selectedGroup = this.getSelectedGroup().toLowerCase(Locale.ROOT);
							GroupHelper.addToGroup(selectedGroup, selected.getArmorStand());
						}
					}).bounds(centerWidth + 50, 42, 16, 16)
					.tooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.add_group_2", this.getSelectedGroup()))).build());

		} else {
			int buttonWidth = (closeButtonWidth / 2) - 1;
			this.addRenderableWidget(this.locateButton = Button.builder(Component.translatable("armorposer.gui.armor_list.locate"), b -> {
				if (selected != null && minecraft.player != null) {
					GlowHandler.startGlowing(this.selected.getArmorStand().getUUID());
					Vec3 standPos = this.selected.getArmorStand().position();
					Vec3 adjustedPos = standPos.add(0, this.selected.getArmorStand().getScale(), 0);
					minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, adjustedPos);
				}
			}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, buttonWidth, 20).build());

			this.addRenderableWidget(this.modifyButton = Button.builder(Component.translatable("armorposer.gui.armor_list.modify"), b -> {
				if (selected != null && minecraft.player != null) {
					ArmorStandScreen modifyScreen = new ArmorStandScreen(selected.getArmorStand(), parentScreen.getDisabledFeatures(), parentScreen.getMinScale(), parentScreen.getMaxScale());
					minecraft.setScreenAndShow(modifyScreen);
				}
			}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING + buttonWidth + 2, y, buttonWidth, 20).build());

			// Range textbox
			this.rangeSlider = new RangeSlider(centerWidth - 50, 18, 100, 20,
					Component.translatable("armorposer.gui.label.range").append(" : "), Component.empty(), 1f, 30f, 30, true, (value) -> {
				this.range = value;
				this.armorListWidget[0].refreshList(true);
				this.armorListWidget[1].refreshList(false);
			}
			);
			this.rangeSlider.setTooltip(Tooltip.create(Component.translatable("armorposer.gui.tooltip.range")));
			this.addRenderableWidget(this.rangeSlider);
		}

		updateCache();
	}

	@SuppressWarnings("unused")
	private FormattedCharSequence formatLowercase(String text, int offset) {
		return FormattedCharSequence.forward(text.toLowerCase(Locale.ROOT), Style.EMPTY);
	}

	private int pruneTimer = 0;

	@Override
	public void tick() {
		if (armorListWidget[0].children().contains(selected)) {
			if (armorListWidget[0].getSelected() != selected) {
				armorListWidget[0].setSelected(selected);
				armorListWidget[1].setSelected(null);
			}
		} else if (armorListWidget[1].children().contains(selected)) {
			if (armorListWidget[1].getSelected() != selected) {
				armorListWidget[1].setSelected(selected);
				armorListWidget[0].setSelected(null);
			}
		}

		if (this.groupMode) {
			pruneTimer++;
			if (pruneTimer % 20 == 0) {
				pruneTimer = 0;
				if (minecraft != null && minecraft.level != null && minecraft.player != null) {
					var valid = minecraft.level.getEntitiesOfClass(
							ArmorStand.class,
							minecraft.player.getBoundingBox().inflate(range),
							EntitySelector.LIVING_ENTITY_STILL_ALIVE
					).stream().map(Entity::getUUID).collect(Collectors.toSet());
					GroupHelper.pruneInvalid(valid);
				}
			}

			this.addButton.active = this.selected != null && !this.groupField.getValue().isBlank();
			this.removeButton.active = this.selected != null && !this.groupField.getValue().isBlank();
			this.clearButton.active = !this.groupField.getValue().isBlank();

			this.addButton2.active = this.selected != null;
			this.removeButton2.active = this.selected != null;

			this.modifyButton.active = !GroupHelper.isEmpty(getSelectedGroup());
			this.modifyButton2.active = !getGroup().isBlank() && !GroupHelper.isEmpty(getGroup());
		}
	}

	public <T extends ObjectSelectionList.Entry<T>> void buildVisiblePositions(Consumer<T> listViewConsumer, Function<ArmorStand, T> newEntry) {
		List<ArmorStand> filteredStands = filterRange(armorStands, range);
		filteredStands.forEach(stand -> listViewConsumer.accept(newEntry.apply(stand)));
	}

	public <T extends ObjectSelectionList.Entry<T>> void buildInvisiblePositions(Consumer<T> listViewConsumer, Function<ArmorStand, T> newEntry) {
		List<ArmorStand> filteredStands = filterRange(invisiblearmorStands, range);
		filteredStands.forEach(stand -> listViewConsumer.accept(newEntry.apply(stand)));
	}

	private List<ArmorStand> filterRange(List<ArmorStand> stands, double range) {
		if (minecraft.player == null) {
			return stands;
		}
		return stands.stream().filter(stand -> stand.distanceToSqr(minecraft.player) <= range * range).toList();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

		graphics.centeredText(this.font, this.title, this.width / 2, 4, ARGB.opaque(16777215));
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.armorListWidget[0].keyPressed(event)) {
			return true;
		} else if (this.armorListWidget[1].keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		//Nope
	}

	public void setSelected(ArmorGlowWidget.ListEntry previousEntry, ArmorGlowWidget.ListEntry entry) {
		if (this.selected == previousEntry) {
			this.selected = entry;
			GlowHandler.startGlowing(entry.getArmorStand().getUUID());
		} else {
			if (this.selected == null || entry != null) {
				this.selected = entry;
			}
		}
		updateCache();
	}

	private void updateCache() {
		this.locateButton.active = selected != null;
		this.modifyButton.active = groupMode || selected != null;
	}

	/**
	 * Clear the search field when right-clicked on it
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent buttonEvent, boolean flag) {
		boolean clicked = super.mouseClicked(buttonEvent, flag);
		if (this.armorListWidget[0].mouseClicked(buttonEvent, flag)) {
			return true;
		} else if (this.armorListWidget[1].mouseClicked(buttonEvent, flag)) {
			return true;
		}
		return clicked;
	}

	@Override
	public void resize(int width, int height) {
		ArmorGlowWidget.ListEntry selected = this.selected;
		String group = "";
		String selectedGroup = "";
		if (this.groupMode) {
			group = this.groupField.getValue();
			selectedGroup = this.selectedGroupButton.getValue();
		}
		this.init(width, height);
		if (this.groupMode) {
			this.groupField.setValue(group);
			this.selectedGroupButton.setValue(selectedGroup);
		}
		this.selected = selected;
		updateCache();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(parentScreen);
	}

	public Minecraft getScreenMinecraft() {
		return this.minecraft;
	}

	public Font getScreenFont() {
		return this.font;
	}

	public boolean isGroupMode() {
		return this.groupMode;
	}

	public String getGroup() {
		return this.groupMode ? this.groupField.getValue().strip().toLowerCase(Locale.ROOT) : "";
	}

	public String getSelectedGroup() {
		return this.groupMode ? "group " + this.selectedGroupButton.getValue() : "";
	}
}
