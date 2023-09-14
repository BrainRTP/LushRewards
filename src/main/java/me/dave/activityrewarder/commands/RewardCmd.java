package me.dave.activityrewarder.commands;

import me.dave.activityrewarder.ActivityRewarder;
import me.dave.activityrewarder.data.RewardUser;
import me.dave.activityrewarder.importer.ConfigImporter;
import me.dave.activityrewarder.importer.DailyRewardsPlusImporter;
import me.dave.activityrewarder.importer.NDailyRewardsImporter;
import me.dave.activityrewarder.module.dailyrewards.DailyRewardsGui;
import me.dave.activityrewarder.module.dailyrewards.DailyRewardsModule;
import me.dave.activityrewarder.module.dailyrewards.DailyRewardsModuleUserData;
import me.dave.activityrewarder.module.playtimedailygoals.PlaytimeDailyGoalsModule;
import me.dave.activityrewarder.module.playtimeglobalgoals.PlaytimeGlobalGoalsModule;
import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RewardCmd implements CommandExecutor, TabCompleter {
    private static final String ABOUT_MESSAGE =
        "<gradient:#CEB5FE:#81BAFE><b>ActivityRewarder</b> (v" + ActivityRewarder.getInstance().getDescription().getVersion() + ")</gradient>" +
            "\n&7An extremely configurable, feature rich rewards plugin. Reward your players each day for logging in and also reward them for their time spent on the server with playtime rewards!" +
            "\n\n&7Author: <color:#f7ba6f>Dav_e_</color:#f7ba6f>" +
            "\n\n&7Links:  <color:#fcff96><click:open_url:https://dave-12.gitbook.io/activity-rewarder>[ Wiki ]</click></color:#fcff96>  <color:#5865F2><click:open_url:https://discord.gg/p3duRZsZ2f>[ Support ]</click></color:#5865F2>";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        ActivityRewarder.getConfigManager().checkRefresh();

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "about" -> {
                    ChatColorHandler.sendMessage(sender, ABOUT_MESSAGE);
                    return true;
                }
                case "claim" -> {
                    if (!(sender instanceof Player player)) {
                        ChatColorHandler.sendMessage(sender, "Console cannot run this command!");
                        return true;
                    }

                    if (!sender.hasPermission("activityrewarder.use")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    if (ActivityRewarder.getModule(DailyRewardsModule.ID) instanceof DailyRewardsModule dailyRewardsModule) {
                        if (dailyRewardsModule.claimRewards(player)) {
                            ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("daily-reward-given"));
                        }
                    }

                    if (ActivityRewarder.getModule(PlaytimeDailyGoalsModule.ID) instanceof PlaytimeDailyGoalsModule playtimeDailyGoalsModule) {
                        if (playtimeDailyGoalsModule.claimRewards(player)) {
                            ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("daily-playtime-reward-given").replaceAll("%minutes%", String.valueOf(ActivityRewarder.getDataManager().getRewardUser(player).getMinutesPlayed())));
                        }
                    }

                    if (ActivityRewarder.getModule(PlaytimeGlobalGoalsModule.ID) instanceof PlaytimeGlobalGoalsModule playtimeGlobalGoalsModule) {
                        if (playtimeGlobalGoalsModule.claimRewards(player)) {
                            ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("global-playtime-reward-given").replaceAll("%minutes%", String.valueOf(ActivityRewarder.getDataManager().getRewardUser(player).getMinutesPlayed())));
                        }
                    }

                    return true;
                }
                case "import" -> {
                    if (!sender.hasPermission("activityrewarder.import")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards import <plugin>"));
                    return true;
                }
                case "messages" -> {
                    if (!sender.hasPermission("activityrewarder.viewmessages")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ActivityRewarder.getConfigManager().getMessages().forEach(message -> ChatColorHandler.sendMessage(sender, message));
                    return true;
                }
                case "reload" -> {
                    if (!sender.hasPermission("activityrewarder.reload")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }
                    ActivityRewarder.getConfigManager().reloadConfig();

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("reload"));
                    return true;
                }
                case "reset-days" -> {
                    if (!sender.hasPermission("activityrewarder.resetdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards reset-days <player>"));
                    return true;
                }
                case "set-days" -> {
                    if (!sender.hasPermission("activityrewarder.setdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-days <player> <day-num>"));
                    return true;
                }
                case "reset-streak" -> {
                    if (!sender.hasPermission("activityrewarder.resetstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards reset-streak <player>"));
                    return true;
                }
                case "set-streak" -> {
                    if (!sender.hasPermission("activityrewarder.setstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-streak <player> <streak>"));
                    return true;
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "import" -> {
                    if (!sender.hasPermission("activityrewarder.import")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ConfigImporter configImporter = null;
                    try {
                        switch (args[1].toLowerCase()) {
                            case "dailyrewardsplus" -> configImporter = new DailyRewardsPlusImporter();
                            case "ndailyrewards" -> configImporter = new NDailyRewardsImporter();
                        }
                    } catch (FileNotFoundException e) {
                        ChatColorHandler.sendMessage(sender, "&#ff6969Could not find files when attempting to import from &#d13636'" + args[1] + "'");
                        return true;
                    }

                    if (configImporter != null) {
                        long startMs = Instant.now().toEpochMilli();
                        configImporter.startImport()
                            .completeOnTimeout(false, 10, TimeUnit.SECONDS)
                            .thenAccept(success -> {
                                if (success) {
                                    ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully imported configuration from &#66b04f'" + args[1] + "' &#b7faa2in &#66b04f" + (Instant.now().toEpochMilli() - startMs) + "ms");
                                    ActivityRewarder.getConfigManager().reloadConfig();
                                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("reload"));
                                } else {
                                    ChatColorHandler.sendMessage(sender, "&#ff6969Failed to import configuration from &#d13636'" + args[1] + "' &#ff6969in &#d13636" + (Instant.now().toEpochMilli() - startMs) +"ms");
                                }
                            });
                    } else {
                        ChatColorHandler.sendMessage(sender, "&#ff6969Failed to import configuration from &#d13636'" + args[1] + "'");
                    }

                    return true;
                }
                case "reset-days" -> {
                    if (!sender.hasPermission("activityrewarder.resetdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("reset-days").replaceAll("%target%", args[1]));
                    return true;
                }
                case "set-days" -> {
                    if (!sender.hasPermission("activityrewarder.setdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-days <player> <day-num>"));
                    return true;
                }
                case "reset-streak" -> {
                    if (!sender.hasPermission("activityrewarder.resetstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("reset-streak").replaceAll("%target%", args[1]));
                    return true;
                }
                case "set-streak" -> {
                    if (!sender.hasPermission("activityrewarder.setstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-streak <player> <streak>"));
                    return true;
                }
            }

        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "reset-days" -> {
                    if (!sender.hasPermission("activityrewarder.resetdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    if (!args[2].equalsIgnoreCase("confirm")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards reset-days <player> confirm"));
                        return true;
                    }

                    if (!setStreak(sender, args[1], 1)) return true;
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-days-confirm").replaceAll("%target%", args[1]).replaceAll("%day%", args[2]));
                    return true;
                }
                case "set-days" -> {
                    if (!sender.hasPermission("activityrewarder.setdays")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-days").replaceAll("%target%", args[1]).replaceAll("%day%", args[2]));
                    return true;
                }
                case "reset-streak" -> {
                    if (!sender.hasPermission("activityrewarder.resetstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    if (!args[2].equalsIgnoreCase("confirm")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards reset-streak <player> confirm"));
                        return true;
                    }

                    if (!setStreak(sender, args[1], 1)) return true;
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-streak-confirm").replaceAll("%target%", args[1]).replaceAll("%streak%", args[2]));
                    return true;
                }
                case "set-streak" -> {
                    if (!sender.hasPermission("activityrewarder.setstreak")) {
                        ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                        return true;
                    }

                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-streak").replaceAll("%target%", args[1]).replaceAll("%streak%", args[2]));
                    return true;
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set-days")) {
                if (!sender.hasPermission("activityrewarder.setdays")) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                    return true;
                }

                if (!args[3].equalsIgnoreCase("confirm")) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-days <player> <day-num> confirm"));
                    return true;
                }

                int dayNum;
                try {
                    dayNum = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-days <player> <day-num> confirm"));
                    return true;
                }

                if (!setDay(sender, args[1], dayNum)) return true;
                ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-days-confirm").replaceAll("%target%", args[1]).replaceAll("%day%", String.valueOf(dayNum)));
                return true;
            } else if (args[0].equalsIgnoreCase("set-streak")) {
                if (!sender.hasPermission("activityrewarder.setstreak")) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
                    return true;
                }

                if (!args[3].equalsIgnoreCase("confirm")) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-streak <player> <streak> confirm"));
                    return true;
                }

                int streak;
                try {
                    streak = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("incorrect-usage").replaceAll("%command-usage%", "/rewards set-streak <player> <streak> confirm"));
                    return true;
                }

                if (!setStreak(sender, args[1], streak)) return true;
                ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("set-streak-confirm").replaceAll("%target%", args[1]).replaceAll("%streak%", String.valueOf(streak)));
                return true;
            }
        }

        if (!(sender instanceof Player player)) {
            ChatColorHandler.sendMessage(sender, "Console cannot run this command!");
            return true;
        }

        if (!player.hasPermission("activityrewarder.use")) {
            ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("no-permissions"));
            return true;
        }

        if (ActivityRewarder.getModule(DailyRewardsModule.ID) instanceof DailyRewardsModule dailyRewardsModule) {
            DailyRewardsGui dailyRewardsGui = new DailyRewardsGui(dailyRewardsModule, player);
            dailyRewardsGui.openInventory();
        } else {
            ChatColorHandler.sendMessage(player, "&#ff6969DailyRewards module is disabled");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;
        if (!commandSender.hasPermission("activityrewarder.use")) {
            return tabComplete;
        }

        if (args.length == 1) {
            tabComplete.add("about");
            if (commandSender.hasPermission("activityrewarder.use")) tabComplete.add("claim");
            if (commandSender.hasPermission("activityrewarder.import")) tabComplete.add("import");
            if (commandSender.hasPermission("activityrewarder.reload")) tabComplete.add("reload");
            if (commandSender.hasPermission("activityrewarder.resetdays") || commandSender.hasPermission("activityrewarder.resetdays.others"))
                tabComplete.add("reset-days");
            if (commandSender.hasPermission("activityrewarder.setdays")) tabComplete.add("set-days");
            if (commandSender.hasPermission("activityrewarder.resetstreak") || commandSender.hasPermission("activityrewarder.resetstreak.others"))
                tabComplete.add("reset-streak");
            if (commandSender.hasPermission("activityrewarder.setstreak")) tabComplete.add("set-streak");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "import" -> tabComplete.add("DailyRewardsPlus");
                case "set-days", "reset-days", "set-streak", "reset-streak" ->
                    tabComplete.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
        }

        for (String currTab : tabComplete) {
            int currArg = args.length - 1;
            if (currTab.startsWith(args[currArg])) {
                wordCompletion.add(currTab);
                wordCompletionSuccess = true;
            }
        }
        if (wordCompletionSuccess) return wordCompletion;
        return tabComplete;
    }

    private boolean setDay(CommandSender sender, String nameOrUuid, int dayNum) {
        Player player = Bukkit.getPlayer(nameOrUuid);
        UUID uuid;
        if (player != null) {
            uuid = player.getUniqueId();
        } else {
            try {
                uuid = UUID.fromString(nameOrUuid);
            } catch (IllegalArgumentException e) {
                ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("unknown-player").replaceAll("%player%", nameOrUuid));
                return false;
            }
        }

        if (player != null && ActivityRewarder.getDataManager().isRewardUserLoaded(uuid)) {
            RewardUser rewardUser = ActivityRewarder.getDataManager().getRewardUser(player);
            rewardUser.setDayNum(dayNum);
        } else {
            ActivityRewarder.getDataManager().loadRewardUser(uuid).thenAccept((rewardUser -> {
                rewardUser.setDayNum(dayNum);
                ActivityRewarder.getDataManager().unloadRewarderUser(uuid);
            }));
        }

        return true;
    }

    private boolean setStreak(CommandSender sender, String nameOrUuid, int streak) {
        Player player = Bukkit.getPlayer(nameOrUuid);
        UUID uuid;
        if (player != null) {
            uuid = player.getUniqueId();
        } else {
            try {
                uuid = UUID.fromString(nameOrUuid);
            } catch (IllegalArgumentException e) {
                ChatColorHandler.sendMessage(sender, ActivityRewarder.getConfigManager().getMessage("unknown-player").replaceAll("%player%", nameOrUuid));
                return false;
            }
        }

        if (player != null && ActivityRewarder.getDataManager().isRewardUserLoaded(uuid)) {
            RewardUser rewardUser = ActivityRewarder.getDataManager().getRewardUser(player);
            if (rewardUser.getModuleData(DailyRewardsModule.ID) instanceof DailyRewardsModuleUserData moduleUserData) {
                moduleUserData.setStreakLength(streak);
                rewardUser.save();
            }
        } else {
            ActivityRewarder.getDataManager().loadRewardUser(uuid).thenAccept((rewardUser -> {
                if (rewardUser.getModuleData(DailyRewardsModule.ID) instanceof DailyRewardsModuleUserData moduleUserData) {
                    moduleUserData.setStreakLength(streak);
                    rewardUser.save();
                }
                ActivityRewarder.getDataManager().unloadRewarderUser(uuid);
            }));
        }

        return true;
    }
}
