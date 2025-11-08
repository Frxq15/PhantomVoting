package me.fergs.phantomvoting.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Custom command implementation that supports subcommands and arguments.
 * Compatible with both Spigot and Paper.
 */
public class CustomCommand extends Command {
    private CommandExecutor executor;
    private PlayerCommandExecutor playerExecutor;
    private TabCompleter tabCompleter;
    private String permission;
    private final Map<String, CustomCommand> subcommands = new HashMap<>();

    public CustomCommand(@NotNull String name) {
        super(name);
    }

    public CustomCommand(@NotNull String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    /**
     * Sets the command executor for both players and console.
     *
     * @param executor the executor
     * @return this command
     */
    public CustomCommand executes(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Sets the command executor for players only.
     *
     * @param executor the player executor
     * @return this command
     */
    public CustomCommand executesPlayer(PlayerCommandExecutor executor) {
        this.playerExecutor = executor;
        return this;
    }

    /**
     * Sets the permission required to execute this command.
     *
     * @param permission the permission
     * @return this command
     */
    public CustomCommand withPermission(String permission) {
        this.permission = permission;
        this.setPermission(permission);
        return this;
    }

    /**
     * Sets the tab completer for this command.
     *
     * @param tabCompleter the tab completer
     * @return this command
     */
    public CustomCommand withTabCompleter(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    /**
     * Adds a subcommand to this command.
     *
     * @param subcommand the subcommand
     * @return this command
     */
    public CustomCommand withSubcommand(CustomCommand subcommand) {
        this.subcommands.put(subcommand.getName().toLowerCase(), subcommand);
        return this;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            return true;
        }

        if (args.length > 0) {
            String subcommandName = args[0].toLowerCase();
            CustomCommand subcommand = subcommands.get(subcommandName);

            if (subcommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subcommand.execute(sender, subcommandName, subArgs);
            }
        }

        try {
            if (playerExecutor != null) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be executed by players.");
                    return true;
                }
                playerExecutor.execute((Player) sender, new CommandArguments(args));
            } else if (executor != null) {
                executor.execute(sender, new CommandArguments(args));
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("An error occurred while executing command '" + getName() + "': " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (tabCompleter != null) {
            try {
                List<String> customCompletions = tabCompleter.complete(sender, args);
                if (customCompletions != null) {
                    return customCompletions;
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("An error occurred while tab-completing command '" + getName() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String subcommandName : subcommands.keySet()) {
                if (subcommandName.startsWith(partial)) {
                    CustomCommand subcommand = subcommands.get(subcommandName);
                    if (subcommand.permission == null || sender.hasPermission(subcommand.permission)) {
                        completions.add(subcommandName);
                    }
                }
            }
        } else if (args.length > 1) {
            String subcommandName = args[0].toLowerCase();
            CustomCommand subcommand = subcommands.get(subcommandName);
            if (subcommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subcommand.tabComplete(sender, subcommandName, subArgs);
            }
        }

        return completions;
    }

    /**
     * Functional interface for command execution.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        void execute(CommandSender sender, CommandArguments args) throws Exception;
    }

    /**
     * Functional interface for player-only command execution.
     */
    @FunctionalInterface
    public interface PlayerCommandExecutor {
        void execute(Player player, CommandArguments args) throws Exception;
    }

    /**
     * Functional interface for tab completion.
     */
    @FunctionalInterface
    public interface TabCompleter {
        List<String> complete(CommandSender sender, String[] args) throws Exception;
    }
}
