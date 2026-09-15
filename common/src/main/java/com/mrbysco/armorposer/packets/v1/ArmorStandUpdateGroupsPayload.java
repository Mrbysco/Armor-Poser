package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.GroupData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandUpdateGroupsPayload(GroupData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandUpdateGroupsPayload> CODEC = StreamCodec.composite(
			GroupData.STREAM_CODEC,
			o -> o.data,
			ArmorStandUpdateGroupsPayload::new
	);
	public static final Type<ArmorStandUpdateGroupsPayload> ID = new Type<>(Reference.UPDATE_GROUP_PACKET_ID_V1);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
