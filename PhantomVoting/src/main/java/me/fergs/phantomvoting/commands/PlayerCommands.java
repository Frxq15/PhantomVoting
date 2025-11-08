package me.fergs.phantomvoting.commands;

import me.fergs.phantomvoting.PhantomVoting;
import me.fergs.phantomvoting.commands.impl.CommandArguments;
import me.fergs.phantomvoting.commands.impl.CommandManager;
import me.fergs.phantomvoting.commands.impl.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PlayerCommands {
    /**
     * Registers the player commands.
     *
     * @param plugin The plugin instance.
     * @param commandManager The command manager.
     */
    public void register(final PhantomVoting plugin, CommandManager commandManager) {
        String baseCommand = plugin.getConfigurationManager().getConfig("config").getString("Commands.Base.Command", "vote");
        List<String> aliases = plugin.getConfigurationManager().getConfig("config").getStringList("Commands.Base.Aliases");

        commandManager.registerCommand(
                new CustomCommand(baseCommand, "Vote command", "/" + baseCommand, aliases)
                        .executesPlayer((Player player, CommandArguments args) ->
                                plugin.getMessageManager().sendMessage(player, "VOTE_LIST",
                                        "%daily_votes%", String.valueOf(plugin.getVoteStorage().getPlayerVoteCount(player.getUniqueId(), "daily"))))
                        .withSubcommand(createLeaderboardCommand(plugin))
                        .withSubcommand(createMilestonesCommand(plugin))
                        .withSubcommand(createStreaksCommand(plugin))
                        .withSubcommand(createToggleCommand(plugin))
        );
    }

    /**
     * Creates the leaderboard subcommand.
     */
    private CustomCommand createLeaderboardCommand(PhantomVoting plugin) {
        return new CustomCommand("leaderboard")
                .executesPlayer((Player player, CommandArguments args) ->
                        player.openInventory(plugin.getLeaderboardInventory().createInventory(player)));
    }

    /**
     * Creates the milestones subcommand.
     */
    private CustomCommand createMilestonesCommand(PhantomVoting plugin) {
        return new CustomCommand("milestones")
                .withTabCompleter((sender, args) -> Collections.emptyList())
                .executesPlayer((Player player, CommandArguments args) -> {
                    if (!plugin.getConfigurationManager().isModuleEnabled("Milestones")) {
                        plugin.getMessageManager().sendMessage(player, "MODULE_DISABLED");
                        return;
                    }

                    if (plugin.getConfigurationManager().getConfig("modules").getBoolean("Module-Permissions.Enabled", false)) {
                        String permission = plugin.getConfigurationManager().getConfig("modules")
                                .getString("Module-Permissions.Modules.Milestones.Permission", "phantomvoting.milestones");
                        if (!player.hasPermission(permission)) {
                            plugin.getMessageManager().sendMessage(player, "NO_PERMISSION");
                            return;
                        }
                    }

                    player.openInventory(plugin.getMilestonesInventory().createInventory(player));
                });
    }

    /**
     * Creates the streaks subcommand.
     */
    private CustomCommand createStreaksCommand(PhantomVoting plugin) {
        return new CustomCommand("streaks")
                .withTabCompleter((sender, args) -> Collections.emptyList())
                .executesPlayer((Player player, CommandArguments args) -> {
                    if (!plugin.getConfigurationManager().isModuleEnabled("Streaks-Menu")) {
                        plugin.getMessageManager().sendMessage(player, "MODULE_DISABLED");
                        return;
                    }

                    if (plugin.getConfigurationManager().getConfig("modules").getBoolean("Module-Permissions.Enabled", false)) {
                        String permission = plugin.getConfigurationManager().getConfig("modules")
                                .getString("Module-Permissions.Modules.Streaks.Permission", "phantomvoting.streaks");
                        if (!player.hasPermission(permission)) {
                            plugin.getMessageManager().sendMessage(player, "NO_PERMISSION");
                            return;
                        }
                    }

                    player.openInventory(plugin.getStreaksInventory().createInventory(player));
                });
    }

    /**
     * Creates the toggle command with reminder subcommand.
     */
    private CustomCommand createToggleCommand(PhantomVoting plugin) {
        return new CustomCommand("toggle")
                .withSubcommand(createReminderToggleCommand(plugin));
    }

    /**
     * Creates the reminder toggle subcommand.
     */
    private CustomCommand createReminderToggleCommand(PhantomVoting plugin) {
        return new CustomCommand("reminder")
                .executesPlayer((Player player, CommandArguments args) -> {
                    if (!plugin.getConfigurationManager().isModuleEnabled("VoteReminder")) {
                        plugin.getMessageManager().sendMessage(player, "MODULE_DISABLED");
                        return;
                    }

                    String permission = plugin.getConfigurationManager().getConfig("modules/vote_reminder")
                            .getString("Permission-Settings.Toggle-Permission", "phantomvoting.votereminder");

                    if (!player.hasPermission(permission)) {
                        String setCommand = plugin.getConfigurationManager().getConfig("modules/vote_reminder")
                                .getString("Permission-Settings.Set-Permission-Command")
                                .replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), setCommand);
                        plugin.getMessageManager().sendMessage(player, "VOTE_REMINDER_TOGGLE", "%status%", "enabled");
                    } else {
                        String removeCommand = plugin.getConfigurationManager().getConfig("modules/vote_reminder")
                                .getString("Permission-Settings.Remove-Permission-Command")
                                .replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), removeCommand);
                        plugin.getMessageManager().sendMessage(player, "VOTE_REMINDER_TOGGLE", "%status%", "disabled");
                    }
                });
    }
}
