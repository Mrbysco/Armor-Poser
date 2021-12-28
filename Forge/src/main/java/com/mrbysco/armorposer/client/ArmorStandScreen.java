package com.mrbysco.armorposer.client;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.client.gui.AbstractArmorStandScreen;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.PacketDistributor;

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

		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSyncMessage(armorStand.getUUID(), compound));
	}
}
