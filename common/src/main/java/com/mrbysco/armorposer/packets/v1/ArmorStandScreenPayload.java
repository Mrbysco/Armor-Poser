package com.mrbysco.armorposer.packets.v1;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record ArmorStandScreenPayload(int entityID, List<String> disabledFeatures, double minScale,
                                      double maxScale) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandScreenPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			ArmorStandScreenPayload::entityID,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
			ArmorStandScreenPayload::disabledFeatures,
			ByteBufCodecs.DOUBLE,
			ArmorStandScreenPayload::minScale,
			ByteBufCodecs.DOUBLE,
			ArmorStandScreenPayload::maxScale,
			ArmorStandScreenPayload::new);
	public static final Type<ArmorStandScreenPayload> ID = new Type<>(Reference.SCREEN_PACKET_ID_V1);

	public ArmorStandScreenPayload(int entityID, List<String> disabledFeatures) {
		this(entityID, disabledFeatures, PoserConfig.COMMON.minScale.getAsDouble(), PoserConfig.COMMON.maxScale.getAsDouble());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
