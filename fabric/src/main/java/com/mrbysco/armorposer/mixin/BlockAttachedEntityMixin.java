package com.mrbysco.armorposer.mixin;

import com.mrbysco.armorposer.handlers.EventHandler;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public class BlockAttachedEntityMixin {

	@Inject(method = "tick()V", at = @At(
			value = "TAIL")
	)
	public void armorposer$tick(CallbackInfo ci) {
		EventHandler.onFrameUpdate(((BlockAttachedEntity) (Object) this));
	}
}
