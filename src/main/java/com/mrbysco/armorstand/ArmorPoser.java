package com.mrbysco.armorstand;

import com.mrbysco.armorstand.config.ModConfiguration;
import com.mrbysco.armorstand.packets.ArmorStandScreenMessage;
import com.mrbysco.armorstand.packets.ArmorStandSyncMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ArmorPoser.MOD_ID)
public class ArmorPoser {
	public static final String MOD_ID = "armorposer";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	public ArmorPoser() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(Type.COMMON, ModConfiguration.commonSpec);
		eventBus.register(ModConfiguration.class);

		eventBus.addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		CHANNEL.registerMessage(0, ArmorStandSyncMessage.class, ArmorStandSyncMessage::encode, ArmorStandSyncMessage::decode, ArmorStandSyncMessage::handle);
		CHANNEL.registerMessage(1, ArmorStandScreenMessage.class, ArmorStandScreenMessage::encode, ArmorStandScreenMessage::decode, ArmorStandScreenMessage::handle);
	}

}
