package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SwapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandSwapPayload(SwapData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandSwapPayload> CODEC = StreamCodec.composite(
			SwapData.STREAM_CODEC,
			o -> o.data,
			ArmorStandSwapPayload::new
	);
	public static final Type<ArmorStandSwapPayload> ID = new Type<>(Reference.SWAP_PACKET_ID_V1);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
