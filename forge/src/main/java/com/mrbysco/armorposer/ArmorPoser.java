package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import com.mrbysco.armorposer.packets.handler.ClientPayloadHandler;
import com.mrbysco.armorposer.packets.handler.ServerPayloadHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Reference.MOD_ID)
public class ArmorPoser {

	public ArmorPoser(IEventBus eventBus, ModContainer container, Dist dist) {
		container.registerConfig(Type.COMMON, PoserConfig.commonSpec);
		eventBus.register(PoserConfig.class);

		eventBus.addListener(this::setupPackets);

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}

	private void setupPackets(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(Reference.MOD_ID).optional();
		registrar.playToClient(ArmorStandScreenPayload.ID, ArmorStandScreenPayload.CODEC, ClientPayloadHandler.getInstance()::handleScreenData);
		registrar.playToServer(ArmorStandSwapPayload.ID, ArmorStandSwapPayload.CODEC, ServerPayloadHandler.getInstance()::handleSwapData);
		registrar.playToServer(ArmorStandSyncPayload.ID, ArmorStandSyncPayload.CODEC, ServerPayloadHandler.getInstance()::handleSyncData);
	}
}