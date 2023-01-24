package com.mrbysco.armorposer;


import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.ArmorStandScreenMessage;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

@Mod(Reference.MOD_ID)
public class ArmorPoser {

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Reference.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);


	public ArmorPoser() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(Type.COMMON, PoserConfig.commonSpec);
		eventBus.register(PoserConfig.class);

		CommonClass.init();

		eventBus.addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		CHANNEL.registerMessage(0, ArmorStandSyncMessage.class, ArmorStandSyncMessage::encode, ArmorStandSyncMessage::decode, ArmorStandSyncMessage::handle);
		CHANNEL.registerMessage(1, ArmorStandScreenMessage.class, ArmorStandScreenMessage::encode, ArmorStandScreenMessage::decode, ArmorStandScreenMessage::handle);
	}
}