package me.fergs.phantomvoting.commands;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import me.fergs.phantomvoting.PhantomVoting;
import me.fergs.phantomvoting.commands.impl.CommandArguments;
import me.fergs.phantomvoting.commands.impl.CommandManager;
import me.fergs.phantomvoting.commands.impl.CustomCommand;
import me.fergs.phantomvoting.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommands {
    /**
     * Registers the admin commands.
     *
     * @param plugin The plugin instance.
     * @param commandManager The command manager.
     */
    public void register(final PhantomVoting plugin, CommandManager commandManager) {
        String baseCommand = plugin.getConfigurationManager().getConfig("config").getString("Commands.Admin.Base", "phantomvoting");
        List<String> aliases = plugin.getConfigurationManager().getConfig("config").getStringList("Commands.Admin.Aliases");

        commandManager.registerCommand(
                new CustomCommand(baseCommand, "PhantomVoting admin command", "/" + baseCommand, aliases)
                        .withPermission("phantomvoting.admin")
                        .executes((CommandSender sender, CommandArguments args) ->
                                plugin.getMessageManager().sendMessage(sender, "ADMIN_HELP", "%admin_command%", baseCommand))
                        .withSubcommand(createReloadCommand(plugin))
                        .withSubcommand(createGiveVoteCommand(plugin, baseCommand))
                        .withSubcommand(createTestVoteCommand(plugin, baseCommand))
                        .withSubcommand(createRemoveVoteCommand(plugin, baseCommand))
                        .withSubcommand(createVotePartyCommand(plugin, baseCommand))
                        .withSubcommand(createStreaksCommand(plugin, baseCommand))
                        .withSubcommand(createOpenGuiCommand(plugin, baseCommand))
        );
    }

    /**
     * Creates the reload subcommand.
     */
    private CustomCommand createReloadCommand(PhantomVoting plugin) {
        return new CustomCommand("reload")
                .executes((sender, args) -> {
                    plugin.getConfigurationManager().reloadAllConfigs();
                    plugin.getVotePartyManager().reloadSettings();
                    plugin.getLeaderboardInventory().reloadInventory();

                    if (plugin.getConfigurationManager().isModuleEnabled("Milestones")) {
                        plugin.getMilestonesInventory().reloadInventory();
                    }
                    if (plugin.getConfigurationManager().isModuleEnabled("VoteReminder")) {
                        plugin.getVoteReminderManager().reloadTask("VoteReminder");
                    }
                    if (plugin.getConfigurationManager().isModuleEnabled("Streaks-Menu")) {
                        plugin.getStreaksInventory().reloadInventory();
                    }

                    plugin.getMessageManager().sendMessage(sender, "RELOAD");
                });
    }

    /**
     * Creates the givevote subcommand.
     */
    private CustomCommand createGiveVoteCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("givevote")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 2) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " givevote <player> <amount>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    Integer amount = args.getInt(1);

                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }
                    if (amount == null || amount <= 0) {
                        sender.sendMessage(Color.hex("&cAmount must be a positive number!"));
                        return;
                    }

                    plugin.getVoteStorage().addMultipleVotes(target.getUniqueId(), amount);
                    plugin.getMessageManager().sendMessage(sender, "GIVE_VOTE",
                            "%player%", target.getName(),
                            "%amount%", String.valueOf(amount));
                });
    }

    /**
     * Creates the testvote subcommand.
     */
    private CustomCommand createTestVoteCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("testvote")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 1) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " testvote <player>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }

                    Vote vote = new Vote("TestVote", target.getName(), "127.0.0.1",
                            Long.toString(System.currentTimeMillis(), 10));
                    Bukkit.getPluginManager().callEvent(new VotifierEvent(vote));
                });
    }

    /**
     * Creates the removevote subcommand.
     */
    private CustomCommand createRemoveVoteCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("removevote")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 2) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " removevote <player> <amount>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    Integer amount = args.getInt(1);

                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }
                    if (amount == null || amount <= 0) {
                        sender.sendMessage(Color.hex("&cAmount must be a positive number!"));
                        return;
                    }

                    plugin.getVoteStorage().removeVote(target.getUniqueId(), amount);
                    plugin.getMessageManager().sendMessage(sender, "REMOVE_VOTE",
                            "%player%", target.getName(),
                            "%amount%", String.valueOf(amount));
                });
    }

    /**
     * Creates the voteparty command with subcommands.
     */
    private CustomCommand createVotePartyCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("voteparty")
                .withSubcommand(createVotePartyForceStartCommand(plugin))
                .withSubcommand(createVotePartyAddCommand(plugin, baseCommand))
                .withSubcommand(createVotePartySetCommand(plugin, baseCommand));
    }

    /**
     * Creates the voteparty forcestart subcommand.
     */
    private CustomCommand createVotePartyForceStartCommand(PhantomVoting plugin) {
        return new CustomCommand("forcestart")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        List<String> options = Arrays.asList("true", "false");
                        return options.stream()
                                .filter(opt -> opt.startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    boolean resetVoteProgress = args.size() > 0 &&
                            args.getBoolean(0) != null &&
                            args.getBoolean(0);
                    plugin.getVotePartyManager().forceVoteParty(resetVoteProgress);
                });
    }

    /**
     * Creates the voteparty add subcommand.
     */
    private CustomCommand createVotePartyAddCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("add")
                .executes((sender, args) -> {
                    if (args.size() < 1) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " voteparty add <amount>"));
                        return;
                    }

                    Integer amount = args.getInt(0);
                    if (amount == null) {
                        sender.sendMessage(Color.hex("&cAmount must be a number!"));
                        return;
                    }

                    plugin.getVotePartyManager().forceAddAmount(amount);
                });
    }

    /**
     * Creates the voteparty set subcommand.
     */
    private CustomCommand createVotePartySetCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("set")
                .executes((sender, args) -> {
                    if (args.size() < 1) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " voteparty set <amount>"));
                        return;
                    }

                    Integer amount = args.getInt(0);
                    if (amount == null) {
                        sender.sendMessage(Color.hex("&cAmount must be a number!"));
                        return;
                    }

                    plugin.getVotePartyManager().setCurrentVoteCount(amount);
                });
    }

    /**
     * Creates the streaks command with subcommands.
     */
    private CustomCommand createStreaksCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("streaks")
                .withSubcommand(createStreaksResetCommand(plugin, baseCommand))
                .withSubcommand(createStreaksSetCommand(plugin, baseCommand))
                .withSubcommand(createStreaksAddCommand(plugin, baseCommand));
    }

    /**
     * Creates the streaks reset subcommand.
     */
    private CustomCommand createStreaksResetCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("reset")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 1) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " streaks reset <player>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }

                    LocalDate today = LocalDate.now();
                    plugin.getVoteStorage().resetStreak(target.getUniqueId(), today.toString());
                    plugin.getMessageManager().sendMessage(sender, "STREAK_RESET", "%player%", target.getName());
                });
    }

    /**
     * Creates the streaks set subcommand.
     */
    private CustomCommand createStreaksSetCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("set")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 2) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " streaks set <player> <streak>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    Integer streak = args.getInt(1);

                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }
                    if (streak == null) {
                        sender.sendMessage(Color.hex("&cStreak must be a number!"));
                        return;
                    }

                    plugin.getVoteStorage().setVoteStreak(target.getUniqueId(), streak);
                    plugin.getMessageManager().sendMessage(sender, "STREAK_SET",
                            "%player%", target.getName(),
                            "%streak%", String.valueOf(streak));
                });
    }

    /**
     * Creates the streaks add subcommand.
     */
    private CustomCommand createStreaksAddCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("add")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 2) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " streaks add <player> <streak>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    Integer streak = args.getInt(1);

                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }
                    if (streak == null) {
                        sender.sendMessage(Color.hex("&cStreak must be a number!"));
                        return;
                    }

                    plugin.getVoteStorage().addStreak(target.getUniqueId(), streak);
                    plugin.getMessageManager().sendMessage(sender, "STREAK_ADD",
                            "%player%", target.getName(),
                            "%streak%", String.valueOf(streak));
                });
    }

    /**
     * Creates the opengui subcommand.
     */
    private CustomCommand createOpenGuiCommand(PhantomVoting plugin, String baseCommand) {
        return new CustomCommand("opengui")
                .withTabCompleter((sender, args) -> {
                    if (args.length == 1) {
                        String partial = args[0].toLowerCase();
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(partial))
                                .collect(Collectors.toList());
                    } else if (args.length == 2) {
                        String partial = args[1].toLowerCase();
                        List<String> guis = new ArrayList<>(Arrays.asList("leaderboard"));
                        if (plugin.getConfigurationManager().isModuleEnabled("Milestones")) {
                            guis.add("milestones");
                        }
                        if (plugin.getConfigurationManager().isModuleEnabled("Streaks-Menu")) {
                            guis.add("streaks");
                        }
                        return guis.stream()
                                .filter(gui -> gui.startsWith(partial))
                                .collect(Collectors.toList());
                    }
                    return Collections.emptyList();
                })
                .executes((sender, args) -> {
                    if (args.size() < 2) {
                        sender.sendMessage(Color.hex("&cUsage: /" + baseCommand + " opengui <player> <gui>"));
                        return;
                    }

                    Player target = args.getPlayer(0);
                    String gui = args.getString(1);

                    if (target == null) {
                        sender.sendMessage(Color.hex("&cPlayer not found!"));
                        return;
                    }
                    if (gui == null) {
                        sender.sendMessage(Color.hex("&4&l[&c&l!&4&l] &cThe GUI type does not exist."));
                        return;
                    }

                    switch (gui.toLowerCase()) {
                        case "leaderboard":
                            target.openInventory(plugin.getLeaderboardInventory().createInventory(target));
                            break;
                        case "milestones":
                            if (plugin.getConfigurationManager().isModuleEnabled("Milestones")) {
                                target.openInventory(plugin.getMilestonesInventory().createInventory(target));
                            }
                            break;
                        case "streaks":
                            if (plugin.getConfigurationManager().isModuleEnabled("Streaks-Menu")) {
                                target.openInventory(plugin.getStreaksInventory().createInventory(target));
                            }
                            break;
                        default:
                            sender.sendMessage(Color.hex("&4&l[&c&l!&4&l] &cThe GUI type &f" + gui + " &cdoes not exist."));
                            break;
                    }
                });
    }
}
