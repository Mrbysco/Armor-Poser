package com.mrbysco.armorposer.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandleAccessor {
    @Accessor
    void setMouseGrabbed(boolean value);
}
