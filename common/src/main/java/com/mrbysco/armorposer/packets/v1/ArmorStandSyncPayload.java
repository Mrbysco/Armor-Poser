package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandSyncPayload(SyncData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandSyncPayload> CODEC = StreamCodec.composite(
			SyncData.STREAM_CODEC,
			o -> o.data,
			ArmorStandSyncPayload::new
	);
	public static final Type<ArmorStandSyncPayload> ID = new Type<>(Reference.SYNC_PACKET_ID_V1);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
