package com.mrbysco.armorposer.client.gui;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.widgets.PoseEntry;
import com.mrbysco.armorposer.client.gui.widgets.PoseListWidget;
import com.mrbysco.armorposer.poses.UserPoseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

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

	private PoseListWidget[] poseListWidget = new PoseListWidget[2];
	private PoseListWidget.ListEntry selected = null;
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

	public ArmorStandScreen parentScreen;

	public ArmorPosesScreen(ArmorStandScreen parent) {
		super(Component.translatable("armorposer.gui.poses.title"));
		this.parentScreen = parent;

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
				} else {
					this.parentScreen.readFieldsFromNBT(selected.getTag());
				}
				this.parentScreen.textFieldUpdated();
				this.parentScreen.updateEntity(selected.getTag());
			}
			this.onClose();
		}).bounds(centerWidth - (closeButtonWidth / 2) + PADDING, y, closeButtonWidth, 20).build());

		y -= 14 + PADDING;
		search = new EditBox(getScreenFont(), centerWidth - listWidth / 2 + PADDING + 1, y, listWidth - 2, 14,
				Component.translatable("armorposer.gui.poses.search"));

		int fullButtonHeight = PADDING + 20 + PADDING;
		this.poseListWidget[0] = new PoseListWidget(this, Component.translatable("armorposer.gui.poses.default"), false, listWidth, fullButtonHeight, search.getY() - getScreenFont().lineHeight - PADDING);
		this.poseListWidget[0].setX(0);
		this.poseListWidget[0].setY(10);
		this.poseListWidget[0].setHeight(this.height);

		this.poseListWidget[1] = new PoseListWidget(this, Component.translatable("armorposer.gui.poses.user"), true, listWidth, fullButtonHeight, search.getY() - getScreenFont().lineHeight - PADDING);
		this.poseListWidget[1].setX(width - listWidth);
		this.poseListWidget[1].setY(10);
		this.poseListWidget[1].setHeight(this.height);

		addWidget(search);
		addWidget(poseListWidget[0]);
		addWidget(poseListWidget[1]);
		search.setFocused(false);
		search.setCanLoseFocus(true);

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
				filter(entry -> entry.getName().toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)))
				.collect(Collectors.toList());

		this.userPoses = this.unsortedUserPoses.stream().
				filter(entry -> entry.getName().toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)))
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.poseListWidget[0].render(guiGraphics, mouseX, mouseY, partialTicks);
		this.poseListWidget[1].render(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		Component text = Component.translatable("armorposer.gui.poses.search");
		guiGraphics.drawCenteredString(getScreenFont(), text, this.width / 2 + PADDING,
				search.getY() - getScreenFont().lineHeight - 2, 0xFFFFFF);

		this.search.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			if(selected != null && selected.userAdded()) {
				this.minecraft.setScreen(new DeletePoseScreen(this.parentScreen, selected));
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		//Nope
	}

	public void setSelected(PoseListWidget.ListEntry entry) {
		this.selected = entry == this.selected ? null : entry;
		updateCache();
	}

	private void updateCache() {
		this.applyButton.active = selected != null;
	}

	/**
	 * Clear the search field when right-clicked on it
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean flag = super.mouseClicked(mouseX, mouseY, button);
		if (button == 1 && search.isMouseOver(mouseX, mouseY)) {
			search.setValue("");
		}
		return flag;
	}

	@Override
	public void resize(Minecraft mc, int width, int height) {
		String s = this.search.getValue();
		SortType sort = this.sortType;
		PoseListWidget.ListEntry selected = this.selected;
		this.init(mc, width, height);
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
		this.minecraft.setScreen(parentScreen);
	}

	public Minecraft getScreenMinecraft() {
		return this.minecraft;
	}

	public Font getScreenFont() {
		return this.font;
	}
}
