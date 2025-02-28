package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.BookCopyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmorStandCopyToBookPayload(BookCopyData data) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ArmorStandCopyToBookPayload> CODEC = CustomPacketPayload.codec(
			ArmorStandCopyToBookPayload::write,
			ArmorStandCopyToBookPayload::new);
	public static final Type<ArmorStandCopyToBookPayload> ID = new Type<>(Reference.COPY_TO_BOOK_PACKET_ID);

	public ArmorStandCopyToBookPayload(final FriendlyByteBuf packetBuffer) {
		this(BookCopyData.STREAM_CODEC.decode(packetBuffer));
	}

	public void write(FriendlyByteBuf buf) {
		BookCopyData.STREAM_CODEC.encode(buf, data());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
