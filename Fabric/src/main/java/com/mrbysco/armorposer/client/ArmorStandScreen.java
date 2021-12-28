package com.mrbysco.armorposer.client;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.gui.AbstractArmorStandScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandScreen extends AbstractArmorStandScreen {
	public ArmorStandScreen(ArmorStand entityArmorStand) {
		super(entityArmorStand);
	}

	public static void openScreen(ArmorStand armorStandEntity) {
		Minecraft.getInstance().setScreen(new ArmorStandScreen(armorStandEntity));
	}

	@Override
	protected void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		CompoundTag CompoundNBT = armorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		armorStand.load(CompoundNBT);

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(armorStand.getUUID());
		buf.writeNbt(compound);
		ClientPlayNetworking.send(Reference.SYNC_PACKET_ID, buf);
	}
}
