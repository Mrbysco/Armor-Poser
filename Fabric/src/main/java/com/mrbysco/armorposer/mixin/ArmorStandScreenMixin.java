package com.mrbysco.armorposer.mixin;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ArmorStandScreen.class)
public class ArmorStandScreenMixin {

	/**
	 * @author Mrbysco
	 * @reason How else am I gonna abstract networking
	 */
	@Overwrite(remap = false)
	private void updateEntity(ArmorStand stand, CompoundTag compound) {
		CompoundTag CompoundNBT = stand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		stand.load(CompoundNBT);

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(stand.getUUID());
		buf.writeNbt(compound);
		ClientPlayNetworking.send(Reference.SYNC_PACKET_ID, buf);
	}
}
