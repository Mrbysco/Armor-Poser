package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SwapData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record ArmorStandSwapPayload(SwapData data) implements CustomPacketPayload {

	public ArmorStandSwapPayload(final FriendlyByteBuf packetBuffer) {
		this(SwapData.decode(packetBuffer));
	}

	public void write(FriendlyByteBuf buf) {
		data.encode(buf);
	}

	@Override
	public ResourceLocation id() {
		return Reference.SWAP_PACKET_ID;
	}
}
