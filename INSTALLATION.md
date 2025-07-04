# SeamlessDeath Plugin - Installation Guide

## Quick Start

### For Server Owners (Pre-built JAR)

1. **Download** the latest `seamless-death-1.0.0.jar` file
2. **Place** the JAR file in your server's `plugins` folder
3. **Restart** your server
4. **Configure** the plugin by editing `plugins/SeamlessDeath/config.yml`
5. **Reload** the configuration with `/seamlessdeath reload`

### For Developers (Build from Source)

1. **Prerequisites:**
   - Java 8 or higher
   - Maven 3.6 or higher
   - Git (optional)

2. **Download the source code:**
   ```bash
   git clone <repository-url>
   cd semlessdeath
   ```
   Or download and extract the ZIP file

3. **Build the plugin:**
   - **Windows:** Double-click `build.bat`
   - **Linux/Mac:** Run `mvn clean package`

4. **Install the plugin:**
   - Copy `target/seamless-death-1.0.0.jar` to your server's `plugins` folder
   - Restart your server

## Server Requirements

- **Minecraft Version:** 1.19.4 or higher
- **Server Software:** Spigot, Paper, or other Bukkit-based servers
- **Java Version:** Java 8 or higher
- **RAM:** No additional requirements

## First Time Setup

1. **Start your server** with the plugin installed
2. **Check the console** for the message: "SeamlessDeath plugin has been enabled!"
3. **Edit the config file** at `plugins/SeamlessDeath/config.yml`
4. **Set your preferences:**
   ```yaml
   enabled: true
   keep-inventory: true
   killcam-duration: 5
   killcam-type: auto
   ```
5. **Reload the config** with `/seamlessdeath reload`
6. **Test the plugin** by dying in-game

## Basic Configuration

### Essential Settings

```yaml
# Enable/disable the plugin
enabled: true

# Keep player inventory on death
keep-inventory: true

# How long the kill cam lasts (seconds)
killcam-duration: 5

# Type of kill cam: auto, first-person, third-person, cinematic
killcam-type: auto
```

### Kill Cam Types Explained

- **auto:** Automatically chooses the best option
- **first-person:** Shows from killer's view (PvP deaths)
- **third-person:** Camera follows behind the killer
- **cinematic:** Orbiting camera around death location

## Commands

- `/seamlessdeath` - Show plugin info
- `/seamlessdeath reload` - Reload configuration
- `/seamlessdeath toggle` - Enable/disable plugin

## Permissions

- `seamlessdeath.admin` - Access admin commands (default: op)
- `seamlessdeath.use` - Use seamless death features (default: all players)

## Troubleshooting

### Plugin Not Working
1. Check server console for errors
2. Verify plugin is enabled: `/seamlessdeath`
3. Check player permissions
4. Try `/seamlessdeath reload`

### Kill Cam Not Showing
1. Verify `enabled: true` in config
2. Check if player has `seamlessdeath.use` permission
3. Try different kill cam types

### Performance Issues
1. Increase `camera-update-interval` in config
2. Disable particle effects
3. Reduce kill cam duration

### Players Getting Stuck
1. Use `/seamlessdeath reload` to reset sessions
2. Check for plugin conflicts
3. Verify server version compatibility

## Support

If you need help:
1. Check this installation guide
2. Read the main README.md file
3. Check server console for error messages
4. Create an issue on the GitHub repository

## Next Steps

- Customize messages in the config file
- Adjust camera settings for your preference
- Set up permissions for different player groups
- Test different kill cam types with your players