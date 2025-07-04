# SeamlessDeath Plugin

A Minecraft server plugin that provides a seamless death experience with kill cam functionality. Instead of showing the traditional death screen, players will see a cinematic kill cam showing how they died, followed by automatic respawn.

## Features

- **Seamless Death Experience**: No death screen interruption
- **Multiple Kill Cam Types**:
  - First-person: View from killer's perspective
  - Third-person: Camera behind the killer/attacker
  - Cinematic: Orbiting camera around death location
  - Auto: Automatically chooses the best available option
- **Configurable Duration**: Set how long the kill cam lasts
- **Keep Inventory**: Option to preserve player inventory on death
- **Visual Effects**: Death particles and sound effects
- **Customizable Messages**: All messages can be customized
- **Permission System**: Control who can use the features

## Installation

1. Download the latest release of SeamlessDeath.jar
2. Place the jar file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the generated `config.yml` file

## Building from Source

1. Clone this repository
2. Make sure you have Maven installed
3. Run `mvn clean package` in the project directory
4. The compiled jar will be in the `target` folder

## Configuration

The plugin generates a `config.yml` file with the following options:

```yaml
# Main plugin settings
enabled: true
keep-inventory: true
suppress-death-messages: false

# Kill cam duration in seconds
killcam-duration: 5

# Kill cam type: auto, first-person, third-person, cinematic
killcam-type: auto

# Camera settings
camera:
  smooth-movement: true
  orbit-speed: 0.1
  orbit-radius: 5.0
  orbit-height: 3.0
  third-person-distance: 3.0
  third-person-height: 2.0

# Visual and audio effects
effects:
  death-particles: true
  death-sound: true
  respawn-sound: true
```

## Commands

- `/seamlessdeath` - Show plugin information
- `/seamlessdeath reload` - Reload the configuration
- `/seamlessdeath toggle` - Enable/disable the plugin

## Permissions

- `seamlessdeath.admin` - Access to admin commands (default: op)
- `seamlessdeath.use` - Allows players to use seamless death features (default: true)

## Kill Cam Types

### Auto (Recommended)
Automatically selects the best available kill cam type based on the situation:
- If killed by a player: First-person view
- If killed by a mob: Third-person view
- If no clear attacker: Cinematic view

### First-Person
Shows the death from the killer's perspective. Only works when killed by another player.

### Third-Person
Shows a camera positioned behind and above the killer/attacker, following their movements.

### Cinematic
Creates an orbiting camera around the death location with particle effects.

## How It Works

1. When a player dies, the plugin intercepts the death event
2. The player is made invisible and immobile
3. Their game mode is temporarily set to spectator
4. A kill cam is started based on the configured type
5. After the configured duration, the player is respawned
6. All effects are cleaned up and the player returns to normal

## Compatibility

- **Minecraft Version**: 1.19.4+ (may work with earlier versions)
- **Server Software**: Spigot, Paper, and other Bukkit-based servers
- **Java Version**: Java 8 or higher

## Troubleshooting

### Kill Cam Not Working
- Check that the plugin is enabled in the config
- Verify the player has the `seamlessdeath.use` permission
- Make sure the server is running a compatible version

### Performance Issues
- Reduce the `camera-update-interval` in advanced settings
- Disable particle effects if needed
- Lower the kill cam duration

### Players Getting Stuck
- The plugin automatically cleans up after the configured duration
- Use `/seamlessdeath reload` to reset any stuck sessions
- Check server logs for any error messages

## Advanced Configuration

The `advanced` section in the config allows fine-tuning:

```yaml
advanced:
  respawn-delay-ticks: 1
  cleanup-delay-ticks: 5
  camera-update-interval: 2
  particle-update-interval: 2
```

- `respawn-delay-ticks`: Delay before handling death (in ticks)
- `cleanup-delay-ticks`: Delay before cleaning up effects (in ticks)
- `camera-update-interval`: How often to update camera position (in ticks)
- `particle-update-interval`: How often to spawn particles (in ticks)

## Support

If you encounter any issues or have suggestions for improvements, please create an issue on the GitHub repository.

## License

This project is open source. Feel free to modify and distribute according to your needs.