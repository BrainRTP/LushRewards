package me.dave.lushrewards.module;

import me.dave.lushrewards.module.dailyrewards.DailyRewardsModule;
import me.dave.lushrewards.module.playtimegoals.PlaytimeGoalsModule;
import me.dave.platyutils.manager.Manager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class RewardModuleTypeManager extends Manager {
    private ConcurrentHashMap<String, RewardModule.Constructor<? extends RewardModule>> rewardModuleTypes;

    @Override
    public void onEnable() {
        rewardModuleTypes = new ConcurrentHashMap<>();

        register(RewardModule.Type.DAILY_REWARDS, DailyRewardsModule::new);
//        register(ModuleType.ONE_TIME_REWARDS, OneTimeRewardsModule::new);
        register(RewardModule.Type.PLAYTIME_REWARDS, PlaytimeGoalsModule::new);
    }

    @Override
    public void onDisable() {
        if (rewardModuleTypes != null) {
            rewardModuleTypes.clear();
            rewardModuleTypes = null;
        }
    }

    public boolean isRegistered(String type) {
        return rewardModuleTypes.containsKey(type);
    }

    public void register(String type, RewardModule.Constructor<? extends RewardModule> constructor) {
        rewardModuleTypes.put(type, constructor);
    }

    public void unregister(String type) {
        rewardModuleTypes.remove(type);
    }

    @Nullable
    public RewardModule.Constructor<? extends RewardModule> getConstructor(String type) {
        return rewardModuleTypes.get(type);
    }

    public RewardModule loadModuleType(@NotNull String type, @NotNull String moduleId, @NotNull File moduleFile) {
        return rewardModuleTypes.containsKey(type) ? rewardModuleTypes.get(type).build(moduleId, moduleFile) : null;
    }
}
