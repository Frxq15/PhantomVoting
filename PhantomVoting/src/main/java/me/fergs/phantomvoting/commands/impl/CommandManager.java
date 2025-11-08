package me.fergs.phantomvoting.commands.impl;

import me.fergs.phantomvoting.PhantomVoting;
import me.fergs.phantomvoting.commands.AdminCommands;
import me.fergs.phantomvoting.commands.PlayerCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;

/**
 * Manages command registration using Bukkit's internal command system.
 * Compatible with both Spigot and Paper.
 */
public class CommandManager {
    private final PhantomVoting plugin;
    private final CommandMap commandMap;

    public CommandManager(PhantomVoting plugin) {
        this.plugin = plugin;
        this.commandMap = getCommandMap();
    }

    /**
     * Gets the server's CommandMap using reflection.
     * Works for both Spigot (?) and Paper (?).
     *
     * @return the CommandMap
     */
    private CommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().severe("Failed to get CommandMap: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registers a custom command to the server's command map.
     *
     * @param command the command to register
     */
    public void registerCommand(CustomCommand command) {
        if (commandMap != null) {
            commandMap.register(plugin.getName().toLowerCase(), command);
        }
    }

    /**
     * Registers all plugin commands.
     */
    public void registerCommands() {
        new PlayerCommands().register(plugin, this);
        new AdminCommands().register(plugin, this);
    }
}
