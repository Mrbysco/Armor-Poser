package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record ArmorStandSyncPayload(SyncData data) implements CustomPacketPayload {

	public ArmorStandSyncPayload(final FriendlyByteBuf packetBuffer) {
		this(SyncData.decode(packetBuffer));
	}

	public void write(FriendlyByteBuf buf) {
		data.encode(buf);
	}

	@Override
	public ResourceLocation id() {
		return Reference.SYNC_PACKET_ID;
	}
}
