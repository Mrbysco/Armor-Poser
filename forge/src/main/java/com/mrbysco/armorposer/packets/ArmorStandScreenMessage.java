package com.mrbysco.armorposer.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.NetworkEvent;

public class ArmorStandScreenMessage {
	private final int entityID;

	public ArmorStandScreenMessage(int playerUUID) {
		this.entityID = playerUUID;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(entityID);
	}

	public static ArmorStandScreenMessage decode(final FriendlyByteBuf packetBuffer) {
		return new ArmorStandScreenMessage(packetBuffer.readInt());
	}

	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient()) {
				Minecraft mc = Minecraft.getInstance();
				Entity entity = null;
				if (mc.level != null) {
					entity = mc.level.getEntity(entityID);
				}
				if (entity instanceof ArmorStand armorStandEntity) {
					com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
