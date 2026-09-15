package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.PoseEntry;
import com.mrbysco.armorposer.client.gui.widgets.PoseListWidget;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.platform.Services;
import com.mrbysco.armorposer.poses.UserPoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArmorPosesScreen extends Screen {
	private enum SortType {
		NORMAL,
		A_TO_Z,
		Z_TO_A;

		Button button;

		Component getButtonText() {
			return Component.translatable("armorposer.gui.poses.search." + name().toLowerCase(Locale.ROOT));
		}
	}

	private static final int PADDING = 6;

	private final PoseListWidget[] poseListWidget = new PoseListWidget[2];
	protected PoseListWidget.ListEntry selected = null;
	private List<PoseEntry> poses;
	private final List<PoseEntry> unsortedPoses;
	private List<PoseEntry> userPoses;
	private final List<PoseEntry> unsortedUserPoses;
	private Button applyButton;

	private final int buttonMargin = 1;
	private final int numButtons = SortType.values().length;
	private String lastFilterText = "";

	private EditBox search;
	private boolean sorted = false;
	private SortType sortType = SortType.NORMAL;

	public final ArmorStandScreen parentScreen;
	private final DeletePoseScreen deletePoseScreen;

	public ArmorPosesScreen(ArmorStandScreen parent) {
		super(Component.translatable("armorposer.gui.poses.title"));
		this.parentScreen = parent;
		this.deletePoseScreen = new DeletePoseScreen(this);
		//Add default poses
		List<PoseEntry> rawPoses = Reference.defaultPoseMap.entrySet().stream()
				.map(entry -> new PoseEntry(entry.getKey(), entry.getValue(), false)).collect(Collectors.toList());
		this.unsortedPoses = Collections.unmodifiableList(rawPoses);
		Collections.sort(rawPoses);
		this.poses = Collections.unmodifiableList(rawPoses);

		//Add user added poses
		UserPoseHandler.loadUserPoses();
		List<PoseEntry> rawUserPoses = Reference.userPoses.stream().map(entry -> new PoseEntry(entry, true)).collect(Collectors.toList());
		this.unsortedUserPoses = Collections.unmodifiableList(rawUserPoses);
		Collections.sort(rawUserPoses);
		this.userPoses = Collections.unmodifiableList(rawUserPoses);
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
		this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> ArmorPosesScreen.this.onClose())
				.bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

		y -= 18 + PADDING;
		this.addRenderableWidget(this.applyButton = Button.builder(Component.translatable("armorposer.gui.poses.selection.apply"), b -> {
			if (selected != null) {
				if (!selected.userAdded() && selected.rawName().equals("random")) {
					//Randomize all fields but the last 3 (as those are position) but don't make the rotations too crazy
					for (int i = 0; i < this.parentScreen.poseTextFields.length - 3; i++) {
						//generate a random number between -35 and 35
						float randomRotation = (float) (Math.random() * 70 - 35);
						this.parentScreen.poseTextFields[i].setValue(String.valueOf((int) randomRotation));
					}
					this.parentScreen.textFieldUpdated();
				} else {
					net.minecraft.nbt.CompoundTag saved = selected.getTag();
					if (saved != null) {
						CompoundTag toApply = saved.copy();

						if (toApply.contains("Actions")) {
							ListTag actions = toApply.getListOrEmpty("Actions");
							for (int i = 0; i < actions.size(); i++) {
								CompoundTag action = actions.getCompoundOrEmpty(i);
								String type = action.getStringOr("Type", "");
								if ("swap".equals(type)) {
									try {
										SwapData.Action action1 = SwapData.Action.valueOf(action.getStringOr("Action", ""));
										Services.PLATFORM.swapSlots(
												this.parentScreen.getArmorStandEntity(), action1);
									} catch (IllegalArgumentException ignored) {
										// Invalid action. Ignore!
									}
								}
							}
						}

						this.parentScreen.readFieldsFromNBT(toApply);
						this.parentScreen.updateEntity(toApply);
						this.parentScreen.setLastSendOffset(this.parentScreen.getOffset());
					}
				}
			}
			this.onClose();
		}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

		y -= 14 + PADDING;
		search = new EditBox(getScreenFont(), centerWidth - listWidth / 2 + PADDING + 1, y, listWidth - 2, 14,
				Component.translatable("armorposer.gui.poses.search"));
		search.setFocused(false);
		search.setCanLoseFocus(true);
		addRenderableWidget(search);

		int fullButtonHeight = PADDING + 20 + PADDING;
		this.poseListWidget[0] = new PoseListWidget(this, Component.translatable("armorposer.gui.poses.default"),
				false, listWidth, fullButtonHeight, search.getY() - getScreenFont().lineHeight - PADDING);
		this.poseListWidget[0].setX(0);
		this.poseListWidget[0].setY(16);
		this.poseListWidget[0].setHeight(this.height);
		addRenderableWidget(poseListWidget[0]);

		this.poseListWidget[1] = new PoseListWidget(this, Component.translatable("armorposer.gui.poses.user"),
				true, listWidth, fullButtonHeight, search.getY() - getScreenFont().lineHeight - PADDING);
		this.poseListWidget[1].setX(width - listWidth);
		this.poseListWidget[1].setY(16);
		this.poseListWidget[1].setHeight(this.height);
		addRenderableWidget(poseListWidget[1]);

		final int width = listWidth / numButtons;
		int x = centerWidth + PADDING - width;
		addRenderableWidget(SortType.A_TO_Z.button = Button.builder(SortType.A_TO_Z.getButtonText(), b ->
						resortPoses(SortType.A_TO_Z))
				.bounds(x, PADDING, width - buttonMargin, 20).build());
		x += width + buttonMargin;
		addRenderableWidget(SortType.Z_TO_A.button = Button.builder(SortType.Z_TO_A.getButtonText(), b ->
						resortPoses(SortType.Z_TO_A))
				.bounds(x, PADDING, width - buttonMargin, 20).build());

		resortPoses(SortType.A_TO_Z);
		updateCache();
	}

	@Override
	public void tick() {
		if (poseListWidget[0].children().contains(selected)) {
			poseListWidget[0].setSelected(selected);
			poseListWidget[1].setSelected(null);
		} else if (poseListWidget[1].children().contains(selected)) {
			poseListWidget[0].setSelected(null);
			poseListWidget[1].setSelected(selected);
		}

		if (!search.getValue().equals(lastFilterText)) {
			reloadPoses();
			sorted = false;
		}

		if (!sorted) {
			reloadPoses();
			if (sortType == SortType.A_TO_Z) {
				Collections.sort(poses);
				Collections.sort(userPoses);
			} else if (sortType == SortType.Z_TO_A) {
				poses.sort(Collections.reverseOrder());
				userPoses.sort(Collections.reverseOrder());
			}
			poseListWidget[0].refreshList(false);
			poseListWidget[1].refreshList(true);
			if (selected != null) {
				selected = poseListWidget[0].children().stream().filter(e -> e == selected).findFirst()
						.orElse(poseListWidget[1].children().stream().filter(e -> e == selected).findFirst().orElse(null));
			}
			sorted = true;
		}
	}

	private void reloadPoses() {
		this.poses = this.unsortedPoses.stream().
				filter(entry -> entry.getName().getString().toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)))
				.collect(Collectors.toList());

		this.userPoses = this.unsortedUserPoses.stream().
				filter(entry -> entry.getName().getString().toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)))
				.collect(Collectors.toList());

		lastFilterText = search.getValue();
	}

	public <T extends ObjectSelectionList.Entry<T>> void buildPoseList(Consumer<T> ListViewConsumer, Function<PoseEntry, T> newEntry) {
		poses.forEach(mod -> ListViewConsumer.accept(newEntry.apply(mod)));
	}

	public <T extends ObjectSelectionList.Entry<T>> void buildUserPoseList(Consumer<T> ListViewConsumer, Function<PoseEntry, T> newEntry) {
		userPoses.forEach(mod -> ListViewConsumer.accept(newEntry.apply(mod)));
	}

	private void resortPoses(SortType newSort) {
		this.sortType = newSort;

		for (SortType sort : SortType.values()) {
			if (sort.button != null)
				sort.button.active = sortType != sort;
		}
		sorted = false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

		Component text = Component.translatable("armorposer.gui.poses.search");
		guiGraphics.centeredText(getScreenFont(), text, this.width / 2 + PADDING,
				search.getY() - getScreenFont().lineHeight - 2, -1);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 261) {
			if (selected != null && selected.userAdded()) {
				this.minecraft.setScreenAndShow(this.deletePoseScreen);
			}
		}
		if (this.poseListWidget[0].keyPressed(event)) {
			return true;
		} else if (this.poseListWidget[1].keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		//Nope
	}

	public void setSelected(PoseListWidget.ListEntry previousEntry, PoseListWidget.ListEntry entry, boolean visible) {
		if (this.selected == previousEntry) {
			this.selected = entry;
		} else {
			if (this.selected == null || entry != null) {
				this.selected = entry;
			}
		}
		updateCache();
	}

	private void updateCache() {
		this.applyButton.active = selected != null;
	}

	/**
	 * Clear the search field when right-clicked on it
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent buttonEvent, boolean flag) {
		boolean clicked = super.mouseClicked(buttonEvent, flag);
		if (buttonEvent.button() == 1 && search.isMouseOver(buttonEvent.x(), buttonEvent.y())) {
			search.setValue("");
		}
		return clicked;
	}

	@Override
	public void resize(int width, int height) {
		String s = this.search.getValue();
		SortType sort = this.sortType;
		PoseListWidget.ListEntry selected = this.selected;
		this.init(width, height);
		this.search.setValue(s);
		this.selected = selected;
		if (!this.search.getValue().isEmpty())
			reloadPoses();
		if (sort != SortType.NORMAL)
			resortPoses(sort);
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
}
