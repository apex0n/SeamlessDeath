package com.seamlessdeath;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final SeamlessDeathPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(SeamlessDeathPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        setupDefaultConfig();
    }
    
    private void setupDefaultConfig() {
        // Main settings
        config.addDefault("enabled", true);
        config.addDefault("keep-inventory", false);
        config.addDefault("suppress-death-messages", false);
        
        // Death drops settings
        config.addDefault("death-drops.drop-inventory", true);
        config.addDefault("death-drops.drop-experience", true);
        config.addDefault("death-drops.experience-drop-mode", "vanilla");
        
        // Kill cam settings
        config.addDefault("killcam-duration", 5); // seconds
        config.addDefault("killcam-type", "auto"); // auto, first-person, third-person, cinematic
        
        // Camera settings
        config.addDefault("camera.smooth-movement", true);
        config.addDefault("camera.orbit-speed", 0.1);
        config.addDefault("camera.orbit-radius", 5.0);
        config.addDefault("camera.orbit-height", 3.0);
        config.addDefault("camera.third-person-distance", 3.0);
        config.addDefault("camera.third-person-height", 2.0);
        
        // Effects settings
        config.addDefault("effects.death-particles", true);
        config.addDefault("effects.death-sound", true);
        config.addDefault("effects.respawn-sound", true);
        
        // Messages
        config.addDefault("messages.death-title", "§c§lYOU DIED");
        config.addDefault("messages.death-subtitle", "§7Viewing kill cam...");
        config.addDefault("messages.respawn-message", "§aYou have been respawned!");
        config.addDefault("messages.no-permission", "§cYou don't have permission to use this command!");
        config.addDefault("messages.plugin-enabled", "§aSeamless death enabled!");
        config.addDefault("messages.plugin-disabled", "§cSeamless death disabled!");
        config.addDefault("messages.config-reloaded", "§aConfiguration reloaded!");
        
        // Advanced settings
        config.addDefault("advanced.respawn-delay-ticks", 1);
        config.addDefault("advanced.cleanup-delay-ticks", 5);
        config.addDefault("advanced.camera-update-interval", 2);
        config.addDefault("advanced.particle-update-interval", 2);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // Getter methods for easy access
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    public boolean shouldKeepInventory() {
        return config.getBoolean("keep-inventory", false);
    }
    
    public boolean shouldSuppressDeathMessages() {
        return config.getBoolean("suppress-death-messages", false);
    }
    
    public boolean shouldDropInventory() {
        return config.getBoolean("death-drops.drop-inventory", true);
    }
    
    public boolean shouldDropExperience() {
        return config.getBoolean("death-drops.drop-experience", true);
    }
    
    public String getExperienceDropMode() {
        return config.getString("death-drops.experience-drop-mode", "vanilla");
    }
    
    public int getKillCamDuration() {
        return config.getInt("killcam-duration", 5);
    }
    
    public String getKillCamType() {
        return config.getString("killcam-type", "auto");
    }
    
    public boolean isSmoothMovementEnabled() {
        return config.getBoolean("camera.smooth-movement", true);
    }
    
    public double getOrbitSpeed() {
        return config.getDouble("camera.orbit-speed", 0.1);
    }
    
    public double getOrbitRadius() {
        return config.getDouble("camera.orbit-radius", 5.0);
    }
    
    public double getOrbitHeight() {
        return config.getDouble("camera.orbit-height", 3.0);
    }
    
    public double getThirdPersonDistance() {
        return config.getDouble("camera.third-person-distance", 3.0);
    }
    
    public double getThirdPersonHeight() {
        return config.getDouble("camera.third-person-height", 2.0);
    }
    
    public boolean areDeathParticlesEnabled() {
        return config.getBoolean("effects.death-particles", true);
    }
    
    public boolean isDeathSoundEnabled() {
        return config.getBoolean("effects.death-sound", true);
    }
    
    public boolean isRespawnSoundEnabled() {
        return config.getBoolean("effects.respawn-sound", true);
    }
    
    public String getDeathTitle() {
        return config.getString("messages.death-title", "§c§lYOU DIED");
    }
    
    public String getDeathSubtitle() {
        return config.getString("messages.death-subtitle", "§7Viewing kill cam...");
    }
    
    public String getRespawnMessage() {
        return config.getString("messages.respawn-message", "§aYou have been respawned!");
    }
    
    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission", "§cYou don't have permission to use this command!");
    }
    
    public String getPluginEnabledMessage() {
        return config.getString("messages.plugin-enabled", "§aSeamless death enabled!");
    }
    
    public String getPluginDisabledMessage() {
        return config.getString("messages.plugin-disabled", "§cSeamless death disabled!");
    }
    
    public String getConfigReloadedMessage() {
        return config.getString("messages.config-reloaded", "§aConfiguration reloaded!");
    }
    
    public int getRespawnDelayTicks() {
        return config.getInt("advanced.respawn-delay-ticks", 1);
    }
    
    public int getCleanupDelayTicks() {
        return config.getInt("advanced.cleanup-delay-ticks", 5);
    }
    
    public int getCameraUpdateInterval() {
        return config.getInt("advanced.camera-update-interval", 2);
    }
    
    public int getParticleUpdateInterval() {
        return config.getInt("advanced.particle-update-interval", 2);
    }
}