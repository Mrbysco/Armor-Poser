package com.mrbysco.armorstand.packets;

import com.mrbysco.armorstand.client.gui.ArmorStandScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class ArmorStandScreenMessage {
	private final int entityID;

	public ArmorStandScreenMessage(int playerUUID) {
		this.entityID = playerUUID;
	}

	public void encode(PacketBuffer buf) {
		buf.writeInt(entityID);
	}

	public static ArmorStandScreenMessage decode(final PacketBuffer packetBuffer) {
		return new ArmorStandScreenMessage(packetBuffer.readInt());
	}

	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient()) {
				Minecraft mc = Minecraft.getInstance();
				Entity entity = null;
				if (mc.level != null) {
					entity = mc.level.getEntity(entityID);
				}
				if (entity instanceof ArmorStandEntity) {
					ArmorStandEntity armorStandEntity = (ArmorStandEntity) entity;
					ArmorStandScreen.openScreen(armorStandEntity);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
