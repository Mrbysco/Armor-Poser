package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.ArmorStandScreenMessage;
import com.mrbysco.armorposer.packets.ArmorStandSwapMessage;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@Mod(Reference.MOD_ID)
public class ArmorPoser {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Reference.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public ArmorPoser(IEventBus eventBus) {
		ModLoadingContext.get().registerConfig(Type.COMMON, PoserConfig.commonSpec);
		eventBus.register(PoserConfig.class);

		eventBus.addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		CHANNEL.registerMessage(0, ArmorStandSyncMessage.class, ArmorStandSyncMessage::encode, ArmorStandSyncMessage::decode, ArmorStandSyncMessage::handle);
		CHANNEL.registerMessage(1, ArmorStandSwapMessage.class, ArmorStandSwapMessage::encode, ArmorStandSwapMessage::decode, ArmorStandSwapMessage::handle);
		CHANNEL.registerMessage(2, ArmorStandScreenMessage.class, ArmorStandScreenMessage::encode, ArmorStandScreenMessage::decode, ArmorStandScreenMessage::handle);
	}
}