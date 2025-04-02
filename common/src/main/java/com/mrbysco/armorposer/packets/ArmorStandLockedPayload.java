package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandLockedPayload(int entityID, boolean isLocked) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandLockedPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			ArmorStandLockedPayload::entityID,
			ByteBufCodecs.BOOL,
			ArmorStandLockedPayload::isLocked,
			ArmorStandLockedPayload::new);
	public static final Type<ArmorStandLockedPayload> ID = new Type<>(Reference.LOCKED_PACKET_ID);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
