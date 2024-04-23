package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandScreenPayload(int entityID) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandScreenPayload> CODEC = CustomPacketPayload.codec(
			ArmorStandScreenPayload::write,
			ArmorStandScreenPayload::new);
	public static final Type<ArmorStandScreenPayload> ID = CustomPacketPayload.createType(Reference.SCREEN_PACKET_ID.toString());

	public ArmorStandScreenPayload(final FriendlyByteBuf packetBuffer) {
		this(packetBuffer.readInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(entityID);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
