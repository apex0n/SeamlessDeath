package com.seamlessdeath;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.ExperienceOrb;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class DeathManager implements Listener {
    
    private final SeamlessDeathPlugin plugin;
    private final KillCamManager killCamManager;
    private final Set<UUID> playersInKillCam;
    
    public DeathManager(SeamlessDeathPlugin plugin, KillCamManager killCamManager) {
        this.plugin = plugin;
        this.killCamManager = killCamManager;
        this.playersInKillCam = new HashSet<>();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Skip if plugin is disabled
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }
        
        // Skip if player doesn't have permission
        if (!player.hasPermission("seamlessdeath.use")) {
            return;
        }
        
        // Skip if player is already in kill cam
        if (playersInKillCam.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Check if this damage would be fatal
        double finalDamage = event.getFinalDamage();
        double currentHealth = player.getHealth();
        
        if (currentHealth - finalDamage <= 0) {
            // This damage would kill the player - prevent it and trigger kill cam
            event.setCancelled(true);
            
            // Add to kill cam tracking
            playersInKillCam.add(player.getUniqueId());
            
            // Get damage source info
             Player killer = null;
             LivingEntity lastDamager = null;
             
             if (player.getKiller() != null) {
                 killer = player.getKiller();
             } else if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                 org.bukkit.event.entity.EntityDamageByEntityEvent damageByEntityEvent = 
                     (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
                 if (damageByEntityEvent.getDamager() instanceof Player) {
                     killer = (Player) damageByEntityEvent.getDamager();
                 } else if (damageByEntityEvent.getDamager() instanceof LivingEntity) {
                     lastDamager = (LivingEntity) damageByEntityEvent.getDamager();
                 }
             }
            
            // Store death location
            Location deathLocation = player.getLocation().clone();
            
            // Trigger seamless death immediately
            handleSeamlessDeath(player, killer, lastDamager, deathLocation);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if plugin is enabled
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }
        
        // Check if player has permission
        if (!player.hasPermission("seamlessdeath.use")) {
            return;
        }
        
        // If player is already in kill cam, this is a fallback death event
        if (playersInKillCam.contains(player.getUniqueId())) {
            // Prevent death message and drops since we're handling it seamlessly
            event.setDeathMessage(null);
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            return;
        }
        
        // This is a fallback for deaths that weren't caught by damage prediction
        playersInKillCam.add(player.getUniqueId());
        
        // Get the killer/damage source
        Player killer = player.getKiller();
        LivingEntity lastDamager = getLastDamager(player);
        
        // Store death location
        Location deathLocation = player.getLocation().clone();
        
        // Prevent death message if configured
        if (plugin.getConfigManager().shouldSuppressDeathMessages()) {
            event.setDeathMessage(null);
        }
        
        // Handle inventory and experience drops based on configuration
        handleDeathDrops(event, player, deathLocation);
        
        // Schedule seamless death handling
        new BukkitRunnable() {
            @Override
            public void run() {
                handleSeamlessDeath(player, killer, lastDamager, deathLocation);
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getRespawnDelayTicks());
    }
    
    private void handleSeamlessDeath(Player player, Player killer, LivingEntity lastDamager, Location deathLocation) {
        // Handle drops before making player invisible
        handleSeamlessDeathDrops(player, deathLocation);
        
        // Set health to very low but not zero to prevent actual death
        player.setHealth(0.1);
        
        // Make player invisible and immobile
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
        
        // Set to spectator mode temporarily
        GameMode originalGameMode = player.getGameMode();
        player.setGameMode(GameMode.SPECTATOR);
        
        // Start kill cam
        killCamManager.startKillCam(player, killer, lastDamager, deathLocation, originalGameMode);
    }
    
    public void removePlayerFromKillCam(Player player) {
        playersInKillCam.remove(player.getUniqueId());
    }
    
    public boolean isPlayerInKillCam(Player player) {
        return playersInKillCam.contains(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Clean up any remaining effects after respawn
        new BukkitRunnable() {
            @Override
            public void run() {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
            }
        }.runTaskLater(plugin, plugin.getConfigManager().getCleanupDelayTicks());
    }
    
    private void handleSeamlessDeathDrops(Player player, Location deathLocation) {
        // Handle inventory drops for seamless death
        if (plugin.getConfigManager().shouldDropInventory() && !plugin.getConfigManager().shouldKeepInventory()) {
            // Drop all items from inventory
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    deathLocation.getWorld().dropItemNaturally(deathLocation, item);
                }
            }
            // Drop armor
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != org.bukkit.Material.AIR) {
                    deathLocation.getWorld().dropItemNaturally(deathLocation, armor);
                }
            }
            // Drop off-hand item
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand != null && offHand.getType() != org.bukkit.Material.AIR) {
                deathLocation.getWorld().dropItemNaturally(deathLocation, offHand);
            }
            // Clear inventory
            player.getInventory().clear();
        }
        
        // Handle experience drops for seamless death
        if (plugin.getConfigManager().shouldDropExperience()) {
            String expMode = plugin.getConfigManager().getExperienceDropMode();
            if ("full".equalsIgnoreCase(expMode)) {
                // Drop experience equivalent to current level
                int currentLevel = player.getLevel();
                int totalExp = getTotalExperienceFromLevel(currentLevel);
                ExperienceOrb expOrb = deathLocation.getWorld().spawn(deathLocation, ExperienceOrb.class);
                expOrb.setExperience(totalExp);
            } else {
                // Use vanilla-like experience drop (7 * level, capped at 100)
                int level = player.getLevel();
                int expToDrop = Math.min(level * 7, 100);
                if (expToDrop > 0) {
                    ExperienceOrb expOrb = deathLocation.getWorld().spawn(deathLocation, ExperienceOrb.class);
                    expOrb.setExperience(expToDrop);
                }
            }
            // Reset player experience
            player.setLevel(0);
            player.setExp(0);
        }
    }
    
    private void handleDeathDrops(PlayerDeathEvent event, Player player, Location deathLocation) {
        // Handle inventory drops
        if (plugin.getConfigManager().shouldKeepInventory() || !plugin.getConfigManager().shouldDropInventory()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        } else {
            // Let vanilla handle inventory drops
            event.setKeepInventory(false);
        }
        
        // Handle experience drops
        if (!plugin.getConfigManager().shouldDropExperience()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        } else {
            String expMode = plugin.getConfigManager().getExperienceDropMode();
            if ("full".equalsIgnoreCase(expMode)) {
                // Drop experience equivalent to current level
                int currentLevel = player.getLevel();
                int totalExp = getTotalExperienceFromLevel(currentLevel);
                event.setDroppedExp(totalExp);
                event.setKeepLevel(false);
            } else {
                // Use vanilla experience drop behavior
                event.setKeepLevel(false);
                // Let vanilla calculate dropped experience
            }
        }
    }
    
    private int getTotalExperienceFromLevel(int level) {
        // Calculate total experience points for a given level
        // Based on Minecraft's experience formula
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    
    private LivingEntity getLastDamager(Player player) {
        // Try to get the last entity that damaged the player
        if (player.getLastDamageCause() != null && 
            player.getLastDamageCause().getEntity() instanceof LivingEntity) {
            return (LivingEntity) player.getLastDamageCause().getEntity();
        }
        return null;
    }
}