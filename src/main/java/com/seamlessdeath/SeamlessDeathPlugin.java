package com.seamlessdeath;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SeamlessDeathPlugin extends JavaPlugin implements Listener {
    
    private DeathManager deathManager;
    private KillCamManager killCamManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        // Initialize configuration
        configManager = new ConfigManager(this);
        
        // Initialize managers
        killCamManager = new KillCamManager(this);
        deathManager = new DeathManager(this, killCamManager);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(deathManager, this);
        getServer().getPluginManager().registerEvents(killCamManager, this);
        
        getLogger().info("SeamlessDeath plugin has been enabled!");
        getLogger().info("Note: Death screen will be minimized but may still briefly appear due to client-side mechanics.");
    }
    
    @Override
    public void onDisable() {
        // Clean up any active kill cam sessions
        if (killCamManager != null) {
            killCamManager.cleanup();
        }
        
        getLogger().info("SeamlessDeath plugin has been disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("seamlessdeath")) {
            return false;
        }
        
        if (!sender.hasPermission("seamlessdeath.admin")) {
            sender.sendMessage(configManager.getNoPermissionMessage());
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§eSeamlessDeath v" + getDescription().getVersion());
            sender.sendMessage("§eUsage: /seamlessdeath <reload|toggle>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                configManager.reloadConfig();
                sender.sendMessage(configManager.getConfigReloadedMessage());
                break;
                
            case "toggle":
                boolean enabled = !configManager.isEnabled();
                getConfig().set("enabled", enabled);
                saveConfig();
                configManager.reloadConfig();
                String message = enabled ? configManager.getPluginEnabledMessage() : configManager.getPluginDisabledMessage();
                sender.sendMessage(message);
                break;
                
            default:
                sender.sendMessage("§cUnknown subcommand. Use: reload or toggle");
                break;
        }
        
        return true;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public KillCamManager getKillCamManager() {
        return killCamManager;
    }
    
    public DeathManager getDeathManager() {
        return deathManager;
    }
}