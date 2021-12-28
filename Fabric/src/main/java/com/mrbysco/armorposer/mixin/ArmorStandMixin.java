package com.mrbysco.armorposer.mixin;

import com.mrbysco.armorposer.handlers.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public class ArmorStandMixin {

	@Inject(at = @At(value = "HEAD"),
			method = "interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
			cancellable = true)
	public void poserInteractAt(Player player, Vec3 vec3, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult result = EventHandler.onPlayerEntityInteractSpecific(player, ((ArmorStand)(Object) this), interactionHand);
		if(result == InteractionResult.SUCCESS) {
			cir.setReturnValue(result);
		}
	}
}
