package com.mrbysco.armorposer.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record SyncData(UUID entityUUID, CompoundTag tag){
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(tag);
	}

	public static SyncData decode(final FriendlyByteBuf packetBuffer) {
		return new SyncData(packetBuffer.readUUID(), packetBuffer.readNbt());
	}
}
