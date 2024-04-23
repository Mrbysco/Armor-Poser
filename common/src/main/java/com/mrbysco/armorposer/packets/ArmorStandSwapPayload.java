package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SwapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


public record ArmorStandSwapPayload(SwapData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandSwapPayload> CODEC = CustomPacketPayload.codec(
			ArmorStandSwapPayload::write,
			ArmorStandSwapPayload::new);
	public static final Type<ArmorStandSwapPayload> ID = CustomPacketPayload.createType(Reference.SWAP_PACKET_ID.toString());

	public ArmorStandSwapPayload(final FriendlyByteBuf packetBuffer) {
		this(SwapData.read(packetBuffer));
	}

	public void write(FriendlyByteBuf buf) {
		data.write(buf);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
