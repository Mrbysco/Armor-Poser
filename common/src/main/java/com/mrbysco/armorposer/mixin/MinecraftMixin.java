package com.mrbysco.armorposer.mixin;

import com.mrbysco.armorposer.client.GlowHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z",
			at = @At("HEAD"), cancellable = true)
	public void armorposer$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof ArmorStand && GlowHandler.isGlowing(entity.getUUID()))
			cir.setReturnValue(true);
	}
}
