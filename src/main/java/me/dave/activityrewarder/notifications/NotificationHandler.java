package me.dave.activityrewarder.notifications;

import me.dave.activityrewarder.ActivityRewarder;
import me.dave.activityrewarder.data.RewardUser;
import me.dave.activityrewarder.module.dailyrewards.DailyRewardsModule;
import me.dave.activityrewarder.module.dailyrewards.DailyRewardsModuleUserData;
import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class NotificationHandler {
    private int counter = 0;


    public void reloadNotifications(int reminderPeriod) {
        counter += 1;
        startNotificationTask(reminderPeriod);
    }

    private void startNotificationTask(int reminderPeriod) {
        if (reminderPeriod <= 0) {
            return;
        }

        int reminderPeriodMs = reminderPeriod * 50;
        int thisNotifNum = counter;


        ActivityRewarder.getMorePaperLib().scheduling().asyncScheduler().runAtFixedRate((task) -> {
            if (counter != thisNotifNum) {
                task.cancel();
                return;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                RewardUser rewardUser = ActivityRewarder.getDataManager().getRewardUser(player);
                if (!(rewardUser.getModuleData(DailyRewardsModule.ID) instanceof DailyRewardsModuleUserData moduleUserData)) {
                    continue;
                }


                boolean collectedToday = moduleUserData.hasCollectedToday();
                if (collectedToday) {
                    continue;
                }
                
                ChatColorHandler.sendMessage(player, ActivityRewarder.getConfigManager().getMessage("reminder"));
                player.playSound(player.getLocation(), ActivityRewarder.getConfigManager().getReminderSound(), 1f, 1.5f);
            }

        }, Duration.of(Math.round((double) reminderPeriodMs / 3), ChronoUnit.MILLIS), Duration.of(reminderPeriodMs, ChronoUnit.MILLIS));
    }
}
