package me.fergs.phantomvoting.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles command arguments with type-safe retrieval.
 */
public class CommandArguments {
    private final String[] rawArgs;
    private final Map<String, Object> parsedArgs = new HashMap<>();

    public CommandArguments(String[] args) {
        this.rawArgs = args;
    }

    /**
     * Gets a raw argument by index.
     *
     * @param index the index
     * @return the argument or null if out of bounds
     */
    public String getRaw(int index) {
        if (index >= 0 && index < rawArgs.length) {
            return rawArgs[index];
        }
        return null;
    }

    /**
     * Gets the raw arguments array.
     *
     * @return the raw arguments
     */
    public String[] getRawArgs() {
        return rawArgs;
    }

    /**
     * Gets the number of arguments.
     *
     * @return the argument count
     */
    public int size() {
        return rawArgs.length;
    }

    /**
     * Adds a parsed argument.
     *
     * @param key the argument key
     * @param value the argument value
     */
    public void put(String key, Object value) {
        parsedArgs.put(key, value);
    }

    /**
     * Gets a parsed argument.
     *
     * @param key the argument key
     * @return the argument value
     */
    public Object get(String key) {
        return parsedArgs.get(key);
    }

    /**
     * Gets a player argument by index.
     *
     * @param index the index
     * @return the player or null if not found
     */
    public Player getPlayer(int index) {
        String name = getRaw(index);
        if (name != null) {
            return Bukkit.getPlayer(name);
        }
        return null;
    }

    /**
     * Gets an integer argument by index.
     *
     * @param index the index
     * @return the integer or null if invalid
     */
    public Integer getInt(int index) {
        String value = getRaw(index);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets a boolean argument by index.
     *
     * @param index the index
     * @return the boolean or null if invalid
     */
    public Boolean getBoolean(int index) {
        String value = getRaw(index);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    /**
     * Gets a string argument by index.
     *
     * @param index the index
     * @return the string or null if out of bounds
     */
    public String getString(int index) {
        return getRaw(index);
    }
}

