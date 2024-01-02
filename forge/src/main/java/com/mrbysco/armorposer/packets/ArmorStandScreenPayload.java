package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ArmorStandScreenPayload(int entityID) implements CustomPacketPayload {
	public ArmorStandScreenPayload(final FriendlyByteBuf packetBuffer) {
		this(packetBuffer.readInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entityID);
	}

	@Override
	public ResourceLocation id() {
		return Reference.SCREEN_PACKET_ID;
	}
}
