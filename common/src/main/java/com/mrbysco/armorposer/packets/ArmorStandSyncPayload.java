package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record ArmorStandSyncPayload(SyncData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandSyncPayload> CODEC = CustomPacketPayload.codec(
			ArmorStandSyncPayload::write,
			ArmorStandSyncPayload::new);
	public static final Type<ArmorStandSyncPayload> ID = new Type<>(Reference.SYNC_PACKET_ID);

	public ArmorStandSyncPayload(final FriendlyByteBuf packetBuffer) {
		this(SyncData.STREAM_CODEC.decode(packetBuffer));
	}

	public void write(FriendlyByteBuf buf) {
		SyncData.STREAM_CODEC.encode(buf, data());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
