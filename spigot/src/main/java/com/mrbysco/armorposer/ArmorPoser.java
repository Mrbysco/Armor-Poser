package com.mrbysco.armorposer;

import com.mrbysco.armorposer.handlers.SpigotEventHandler;
import com.mrbysco.armorposer.handlers.SpigotSwapHandler;
import com.mrbysco.armorposer.handlers.SpigotSyncHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArmorPoser extends JavaPlugin {
    public static String CHANNEL_ID_SYNC = Reference.SYNC_PACKET_ID.toString();
    public static String CHANNEL_ID_SWAP = Reference.SWAP_PACKET_ID.toString();
    public static String CHANNEL_ID_SCREEN = Reference.SCREEN_PACKET_ID.toString();

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.addDefault("enableConfigGui", true);
        config.addDefault("enableNameTags", true);
        config.options().copyDefaults(true);
        saveConfig();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL_ID_SCREEN);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL_ID_SYNC, new SpigotSyncHandler());
        this.getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL_ID_SWAP, new SpigotSwapHandler());
        this.getServer().getPluginManager().registerEvents(new SpigotEventHandler(this), this);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
}
