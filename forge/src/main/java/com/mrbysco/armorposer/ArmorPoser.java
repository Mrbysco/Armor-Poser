package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import com.mrbysco.armorposer.packets.handler.ClientPayloadHandler;
import com.mrbysco.armorposer.packets.handler.ServerPayloadHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod(Reference.MOD_ID)
public class ArmorPoser {

	public ArmorPoser(IEventBus eventBus) {
		ModLoadingContext.get().registerConfig(Type.COMMON, PoserConfig.commonSpec);
		eventBus.register(PoserConfig.class);

		eventBus.addListener(this::setupPackets);
	}

	private void setupPackets(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(Reference.MOD_ID);
		registrar.play(Reference.SCREEN_PACKET_ID, ArmorStandScreenPayload::new, handler -> handler
				.client(ClientPayloadHandler.getInstance()::handleScreenData));
		registrar.play(Reference.SWAP_PACKET_ID, ArmorStandSwapPayload::new, handler -> handler
				.server(ServerPayloadHandler.getInstance()::handleSwapData));
		registrar.play(Reference.SYNC_PACKET_ID, ArmorStandSyncPayload::new, handler -> handler
				.server(ServerPayloadHandler.getInstance()::handleSyncData));
	}
}