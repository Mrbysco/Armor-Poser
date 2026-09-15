package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.RenameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandRenamePayload(RenameData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandRenamePayload> CODEC = StreamCodec.composite(
			RenameData.STREAM_CODEC,
			o -> o.data,
			ArmorStandRenamePayload::new
	);
	public static final Type<ArmorStandRenamePayload> ID = new Type<>(Reference.RENAME_PACKET_ID_V1);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
