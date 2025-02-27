package com.mrbysco.armorposer.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrbysco.armorposer.client.KeybindHandler;
import com.mrbysco.armorposer.mixin.KeyMappingAccessor;
import com.mrbysco.armorposer.mixin.MouseHandleAccessor;
import com.mrbysco.armorposer.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class MoveableScreen extends Screen {
    private double mouseX;
    private double mouseY;
    private boolean isPressDown = false;

    private boolean wasDeferred = false;
    protected MoveableScreen(Component title) {
        super(title);
        Services.PLATFORM.onMoveableScreen(false);
    }

    @Override
    public void afterKeyboardAction() {
        super.afterKeyboardAction();
        this.isPressDown = true;
    }

    @Override
    public void removed() {
        this.tickRelease();
        super.removed();
    }

    @Override
    public void onClose() {
        Services.PLATFORM.onMoveableScreen(true);
        super.onClose();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        boolean isPressDown = this.isPressDown;
        this.isPressDown = false;

        //If a child is selected, just escape the text box when pressing escape
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            for (var child : this.children()) {
                if (child.isFocused()) {
                    child.setFocused(false);
                    return true;
                }
            }
        }

        boolean consumed = super.keyPressed(key, scanCode, modifiers);
        if (consumed)
            return true;

        this.updateKeybind();
        if (KeybindHandler.DEFER_CONTROL_KEY.isDown()) {
            //Center the mouse so it doesn't cause button tooltips to show
            //Else consume it ourselves
            if (!this.wasDeferred) {
                var mouseHandle = this.minecraft.mouseHandler;
                this.wasDeferred = true;
                this.mouseX = mouseHandle.xpos();
                this.mouseY = mouseHandle.ypos();
                mouseHandle.setIgnoreFirstMove();
                ((MouseHandleAccessor)mouseHandle).armorposer$setMouseGrabbed(true);
                InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, ((double) this.minecraft.getWindow().getScreenWidth() / 2), ((double) this.minecraft.getWindow().getScreenHeight() / 2));
            }

            var keyEntry = InputConstants.getKey(key, scanCode);
            if (isPressDown) {
                KeyMapping.set(keyEntry, true);
                KeyMapping.click(keyEntry);
            } else {
                KeyMapping.set(keyEntry, false);
            }

            return true;
        }
        return false;
    }

    private void tickRelease() {
        if (this.wasDeferred) {
            this.wasDeferred = false;
            this.minecraft.mouseHandler.setIgnoreFirstMove();
            ((MouseHandleAccessor)this.minecraft.mouseHandler).armorposer$setMouseGrabbed(false);
            InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212993, this.mouseX, this.mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //Deselect focused entry if clicked and nothing hit
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (var child : this.children()) {
            if (child.isFocused()) {
                child.setFocused(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        this.updateKeybind();
        if (!KeybindHandler.DEFER_CONTROL_KEY.isDown()) {
            this.tickRelease();
        }
        return super.keyReleased(key, scanCode, modifiers);
    }

    private void updateKeybind() {
        var key = ((KeyMappingAccessor)KeybindHandler.DEFER_CONTROL_KEY).armorposer$getKey();
        if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue()) {
            KeybindHandler.DEFER_CONTROL_KEY.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue()));
        }
    }
}
