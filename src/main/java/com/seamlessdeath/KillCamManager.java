package com.seamlessdeath;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillCamManager implements Listener {
    
    private final SeamlessDeathPlugin plugin;
    private final Map<UUID, KillCamSession> activeSessions;
    
    public KillCamManager(SeamlessDeathPlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }
    
    public void startKillCam(Player victim, Player killer, LivingEntity lastDamager, Location deathLocation, GameMode originalGameMode) {
        // End any existing session for this player
        endKillCam(victim);
        
        // Determine kill cam type and target
        KillCamType camType = determineKillCamType(killer, lastDamager);
        LivingEntity target = (killer != null) ? killer : lastDamager;
        
        // Create kill cam session
        KillCamSession session = new KillCamSession(victim, target, deathLocation, originalGameMode, camType);
        activeSessions.put(victim.getUniqueId(), session);
        
        // Start the kill cam sequence
        startKillCamSequence(session);
    }
    
    private KillCamType determineKillCamType(Player killer, LivingEntity lastDamager) {
        String configuredType = plugin.getConfigManager().getKillCamType();
        
        switch (configuredType.toLowerCase()) {
            case "first-person":
                return KillCamType.FIRST_PERSON;
            case "third-person":
                return KillCamType.THIRD_PERSON;
            case "cinematic":
                return KillCamType.CINEMATIC;
            case "auto":
            default:
                // Auto-determine based on available targets
                if (killer != null && killer.isOnline()) {
                    return KillCamType.FIRST_PERSON;
                } else if (lastDamager != null && !lastDamager.isDead()) {
                    return KillCamType.THIRD_PERSON;
                } else {
                    return KillCamType.CINEMATIC;
                }
        }
    }
    
    private void startKillCamSequence(KillCamSession session) {
        Player victim = session.getVictim();
        
        // Send title to player
        String deathTitle = plugin.getConfigManager().getDeathTitle();
        String deathSubtitle = plugin.getConfigManager().getDeathSubtitle();
        victim.sendTitle(deathTitle, deathSubtitle, 10, 40, 10);
        
        // Play death sound if enabled
        if (plugin.getConfigManager().isDeathSoundEnabled()) {
            victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        }
        
        // Start kill cam based on type
        switch (session.getCamType()) {
            case FIRST_PERSON:
                startFirstPersonKillCam(session);
                break;
            case THIRD_PERSON:
                startThirdPersonKillCam(session);
                break;
            case CINEMATIC:
                startCinematicKillCam(session);
                break;
        }
        
        // Schedule respawn after kill cam duration
        int duration = plugin.getConfigManager().getKillCamDuration() * 20; // Convert to ticks
        session.setRespawnTask(new BukkitRunnable() {
            @Override
            public void run() {
                respawnPlayer(session);
            }
        }.runTaskLater(plugin, duration));
    }
    
    private void startFirstPersonKillCam(KillCamSession session) {
        Player victim = session.getVictim();
        LivingEntity target = session.getTarget();
        
        if (target instanceof Player && ((Player) target).isOnline()) {
            Player killerPlayer = (Player) target;
            
            // Spectate the killer
            victim.setSpectatorTarget(killerPlayer);
            
            // Add particle effects at death location
            spawnDeathParticles(session.getDeathLocation());
        } else {
            // Fallback to cinematic if killer is not available
            startCinematicKillCam(session);
        }
    }
    
    private void startThirdPersonKillCam(KillCamSession session) {
        Player victim = session.getVictim();
        LivingEntity target = session.getTarget();
        
        if (target != null && !target.isDead()) {
            // Position camera behind and above the target
            Location targetLoc = target.getLocation();
            Vector direction = targetLoc.getDirection().normalize();
            double distance = plugin.getConfigManager().getThirdPersonDistance();
            double height = plugin.getConfigManager().getThirdPersonHeight();
            Vector offset = direction.multiply(-distance).setY(height);
            
            Location camPos = targetLoc.clone().add(offset);
            camPos.setDirection(direction);
            
            victim.teleport(camPos);
            
            // Create smooth camera movement if enabled
            if (plugin.getConfigManager().isSmoothMovementEnabled()) {
                session.setCameraTask(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (target.isDead() || !target.isValid()) {
                            this.cancel();
                            return;
                        }
                        
                        Location newTargetLoc = target.getLocation();
                        Vector newDirection = newTargetLoc.getDirection().normalize();
                        Vector newOffset = newDirection.multiply(-distance).setY(height);
                        
                        Location newCamPos = newTargetLoc.clone().add(newOffset);
                        newCamPos.setDirection(newDirection);
                        
                        victim.teleport(newCamPos);
                    }
                }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getCameraUpdateInterval()));
            }
        } else {
            startCinematicKillCam(session);
        }
    }
    
    private void startCinematicKillCam(KillCamSession session) {
        Player victim = session.getVictim();
        Location deathLoc = session.getDeathLocation();
        
        // Create a cinematic camera that orbits around the death location
        session.setCameraTask(new BukkitRunnable() {
            private double angle = 0;
            private final double radius = plugin.getConfigManager().getOrbitRadius();
            private final double height = plugin.getConfigManager().getOrbitHeight();
            private final double speed = plugin.getConfigManager().getOrbitSpeed();
            
            @Override
            public void run() {
                angle += speed; // Rotation speed from config
                
                double x = deathLoc.getX() + radius * Math.cos(angle);
                double z = deathLoc.getZ() + radius * Math.sin(angle);
                double y = deathLoc.getY() + height;
                
                Location camPos = new Location(deathLoc.getWorld(), x, y, z);
                
                // Look at death location
                Vector direction = deathLoc.toVector().subtract(camPos.toVector()).normalize();
                camPos.setDirection(direction);
                
                victim.teleport(camPos);
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getCameraUpdateInterval()));
        
        // Add particle effects if enabled
        if (plugin.getConfigManager().areDeathParticlesEnabled()) {
            spawnDeathParticles(deathLoc);
        }
    }
    
    private void spawnDeathParticles(Location location) {
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = plugin.getConfigManager().getKillCamDuration() * 20; // Match kill cam duration
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    return;
                }
                
                // Create DustOptions for REDSTONE particle (red color, size 1.0)
                Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.RED, 1.0f);
                location.getWorld().spawnParticle(Particle.REDSTONE, location.clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0, dustOptions);
                location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location.clone().add(0, 0.5, 0), 5, 0.3, 0.3, 0.3, 0.02);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getParticleUpdateInterval());
    }
    
    private void respawnPlayer(KillCamSession session) {
        Player victim = session.getVictim();
        Location deathLocation = session.getDeathLocation();
        
        // Clean up session
        endKillCam(victim);
        
        // Remove from death manager tracking
        plugin.getDeathManager().removePlayerFromKillCam(victim);
        
        // Clear spectator target while still in spectator mode
        if (victim.getGameMode() == GameMode.SPECTATOR) {
            victim.setSpectatorTarget(null);
        }
        
        // Determine respawn location
        Location respawnLocation = determineRespawnLocation(victim, deathLocation);
        
        // Restore original game mode
        victim.setGameMode(session.getOriginalGameMode());
        
        // Clear all potion effects
        for (PotionEffect effect : victim.getActivePotionEffects()) {
            victim.removePotionEffect(effect.getType());
        }
        
        // Teleport to the correct respawn location
        if (respawnLocation != null) {
            victim.teleport(respawnLocation);
        } else {
            // Fallback to world spawn if no location determined
            victim.teleport(victim.getWorld().getSpawnLocation());
        }
        
        // Restore full health
        victim.setHealth(victim.getMaxHealth());
        
        // Reset food and saturation
        victim.setFoodLevel(20);
        victim.setSaturation(20.0f);
        
        // Reset experience (optional - can be configured later)
        // victim.setExp(0);
        // victim.setLevel(0);
        
        // Send respawn message
        victim.sendMessage(plugin.getConfigManager().getRespawnMessage());
        
        // Play respawn sound if enabled
        if (plugin.getConfigManager().isRespawnSoundEnabled()) {
            victim.playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }
    
    private Location determineRespawnLocation(Player player, Location deathLocation) {
        World deathWorld = deathLocation.getWorld();
        
        // Check if player died in the Nether
        if (deathWorld.getEnvironment() == World.Environment.NETHER) {
            // Look for respawn anchor near the death location
            Location anchorLocation = findNearbyRespawnAnchor(deathLocation);
            if (anchorLocation != null) {
                return anchorLocation;
            }
            // In Nether, if no anchor found, fall back to overworld spawn
            World overworld = plugin.getServer().getWorlds().get(0); // Main world
            Location bedSpawn = player.getBedSpawnLocation();
            if (bedSpawn != null && bedSpawn.getWorld().getEnvironment() == World.Environment.NORMAL) {
                return bedSpawn;
            }
            return overworld.getSpawnLocation();
        }
        
        // For overworld and end deaths, check bed spawn first
        Location bedSpawn = player.getBedSpawnLocation();
        if (bedSpawn != null) {
            // Always use bed spawn if it exists, regardless of bed block presence
            // This matches vanilla Minecraft behavior
            return bedSpawn;
        }
        
        // Fall back to world spawn
        return player.getWorld().getSpawnLocation();
    }
    
    private boolean isBedBlock(Block block) {
        String blockType = block.getType().name();
        return blockType.contains("BED") && !blockType.equals("BEDROCK");
    }
    
    private Location findNearbyRespawnAnchor(Location deathLocation) {
        World world = deathLocation.getWorld();
        int searchRadius = 16; // Search within 16 blocks
        
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Location checkLoc = deathLocation.clone().add(x, y, z);
                    Block block = world.getBlockAt(checkLoc);
                    
                    if (block.getType() == Material.RESPAWN_ANCHOR) {
                        // Check if the anchor is charged (has glowstone)
                        if (block.getBlockData() instanceof org.bukkit.block.data.type.RespawnAnchor) {
                            org.bukkit.block.data.type.RespawnAnchor anchor = (org.bukkit.block.data.type.RespawnAnchor) block.getBlockData();
                            if (anchor.getCharges() > 0) {
                                // Return a safe location near the anchor
                                Location anchorLoc = block.getLocation().add(0.5, 1, 0.5);
                                return anchorLoc;
                            }
                        }
                    }
                }
            }
        }
        
        return null; // No charged respawn anchor found
    }
    
    public void endKillCam(Player player) {
        UUID playerId = player.getUniqueId();
        KillCamSession session = activeSessions.remove(playerId);
        
        if (session != null) {
            // Cancel any running tasks
            if (session.getCameraTask() != null) {
                session.getCameraTask().cancel();
            }
            if (session.getRespawnTask() != null) {
                session.getRespawnTask().cancel();
            }
            
            // Remove from death manager tracking
             plugin.getDeathManager().removePlayerFromKillCam(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        endKillCam(event.getPlayer());
    }
    
    public void cleanup() {
        // Clean up all active sessions
        for (UUID playerId : activeSessions.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                endKillCam(player);
            }
        }
        activeSessions.clear();
    }
    
    // Inner classes
    private enum KillCamType {
        FIRST_PERSON,
        THIRD_PERSON,
        CINEMATIC
    }
    
    private static class KillCamSession {
        private final Player victim;
        private final LivingEntity target;
        private final Location deathLocation;
        private final GameMode originalGameMode;
        private final KillCamType camType;
        private BukkitTask cameraTask;
        private BukkitTask respawnTask;
        
        public KillCamSession(Player victim, LivingEntity target, Location deathLocation, GameMode originalGameMode, KillCamType camType) {
            this.victim = victim;
            this.target = target;
            this.deathLocation = deathLocation;
            this.originalGameMode = originalGameMode;
            this.camType = camType;
        }
        
        // Getters and setters
        public Player getVictim() { return victim; }
        public LivingEntity getTarget() { return target; }
        public Location getDeathLocation() { return deathLocation; }
        public GameMode getOriginalGameMode() { return originalGameMode; }
        public KillCamType getCamType() { return camType; }
        public BukkitTask getCameraTask() { return cameraTask; }
        public void setCameraTask(BukkitTask cameraTask) { this.cameraTask = cameraTask; }
        public BukkitTask getRespawnTask() { return respawnTask; }
        public void setRespawnTask(BukkitTask respawnTask) { this.respawnTask = respawnTask; }
    }
}