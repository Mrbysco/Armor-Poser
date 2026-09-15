package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ArmorStandSyncGroupsPayload(Map<UUID, List<String>> groupData) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandSyncGroupsPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())),
			o -> o.groupData,
			ArmorStandSyncGroupsPayload::new
	);

	public static final Type<ArmorStandSyncGroupsPayload> ID = new Type<>(Reference.SYNC_GROUP_PACKET_ID_V1);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
