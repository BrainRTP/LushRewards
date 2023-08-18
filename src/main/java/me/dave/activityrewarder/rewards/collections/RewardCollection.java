package me.dave.activityrewarder.rewards.collections;

import me.dave.activityrewarder.ActivityRewarder;
import me.dave.activityrewarder.rewards.custom.Reward;
import me.dave.activityrewarder.utils.ConfigParser;
import me.dave.activityrewarder.utils.Debugger;
import me.dave.activityrewarder.utils.SimpleItemStack;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RewardCollection {
    private final Collection<Reward> rewards;
    private final int priority;
    private final String category;
    private final SimpleItemStack itemStack;
    private final Sound sound;

    public RewardCollection(@Nullable Collection<Reward> rewards, int priority, @Nullable String category, @Nullable SimpleItemStack itemStack, @Nullable Sound sound) {
        this.rewards = rewards != null ? rewards : Collections.emptyList();
        this.priority = priority;
        this.category = category;
        this.itemStack = itemStack;
        this.sound = sound;
    }

    public Collection<Reward> getRewards() {
        return rewards;
    }

    public int getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public SimpleItemStack getDisplayItem() {
        return itemStack != null ? itemStack : ActivityRewarder.getConfigManager().getCategoryTemplate(category);
    }

    public Sound getSound() {
        return sound;
    }

    public int getRewardCount() {
        return rewards.size();
    }

    public boolean isEmpty() {
        return rewards.isEmpty();
    }

    public void giveAll(Player player) {
        rewards.forEach(reward -> reward.giveTo(player));
    }

    @NotNull
    public static RewardCollection from(ConfigurationSection rewardCollectionSection) {
        Debugger.DebugMode debugMode = Debugger.DebugMode.DAILY;
        Debugger.sendDebugMessage("Attempting to load reward collection at '" + rewardCollectionSection.getCurrentPath() + "'", debugMode);

        int priority = rewardCollectionSection.getInt("priority", 0);
        Debugger.sendDebugMessage("Reward collection priority set to " + priority, debugMode);

        String category = rewardCollectionSection.getString("category", "small");
        Debugger.sendDebugMessage("Reward collection category set to " + category, debugMode);

        ConfigurationSection itemSection = rewardCollectionSection.getConfigurationSection("display-item");
        SimpleItemStack itemStack = itemSection != null ? SimpleItemStack.from(itemSection) : new SimpleItemStack();
        Debugger.sendDebugMessage("Reward collection item set to: " + itemStack, debugMode);

        Sound redeemSound = ConfigParser.getSound(rewardCollectionSection.getString("redeem-sound", "ENTITY_EXPERIENCE_ORB_PICKUP").toUpperCase());

        Debugger.sendDebugMessage("Attempting to load rewards", debugMode);
        List<Map<?, ?>> rewardMaps = rewardCollectionSection.getMapList("rewards");

        List<Reward> rewardList = !rewardMaps.isEmpty() ? Reward.loadRewards(rewardMaps, rewardCollectionSection.getCurrentPath() + ".rewards") : null;
        Debugger.sendDebugMessage("Successfully loaded " + (rewardList != null ? rewardList.size() : 0) + " rewards from '" + rewardCollectionSection.getCurrentPath() + "'", debugMode);

        return rewardList != null ? new RewardCollection(rewardList, 0, category, itemStack, redeemSound) : RewardCollection.empty();
    }

    public static RewardCollection empty() {
        return new RewardCollection(null, 0, null, null, null);
    }
}
