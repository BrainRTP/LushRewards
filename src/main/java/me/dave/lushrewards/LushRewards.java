package me.dave.lushrewards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.dave.lushrewards.command.RewardsCommand;
import me.dave.lushrewards.hook.FloodgateHook;
import me.dave.lushrewards.hook.PlaceholderAPIHook;
import me.dave.lushrewards.module.RewardModuleTypeManager;
import me.dave.lushrewards.module.RewardModule;
import me.dave.lushrewards.module.playtimetracker.PlaytimeTrackerModule;
import me.dave.lushrewards.notifications.NotificationHandler;
import me.dave.lushrewards.rewards.RewardManager;
import me.dave.lushrewards.utils.LocalPlaceholders;
import me.dave.lushrewards.utils.gson.LocalDateTypeAdapter;
import me.dave.lushrewards.utils.gson.UserDataExclusionStrategy;
import org.bukkit.Bukkit;
import me.dave.lushrewards.config.ConfigManager;
import me.dave.lushrewards.data.DataManager;
import me.dave.lushrewards.listener.RewardUserListener;
import org.bukkit.util.FileUtil;
import org.lushplugins.lushlib.LushLib;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.lushlib.utils.Updater;
import space.arim.morepaperlib.MorePaperLib;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public final class LushRewards extends SpigotPlugin {
    private static final Gson GSON;
    private static LushRewards plugin;
    private static MorePaperLib morePaperLib;

    private ConfigManager configManager;
    private DataManager dataManager;
    private NotificationHandler notificationHandler;
    private LocalPlaceholders localPlaceholders;
    private Updater updater;

    static {
        GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .addSerializationExclusionStrategy(new UserDataExclusionStrategy())
            .create();
    }

    @Override
    public void onLoad() {
        plugin = this;
        morePaperLib = new MorePaperLib(plugin);

        LushLib.getInstance().enable(this);
    }

    @Override
    public void onEnable() {
        File oldDataFolder = new File(getDataFolder().getParentFile(), "ActivityRewarder");
        if (!getDataFolder().exists() && oldDataFolder.exists()) {
            if (FileUtil.copy(oldDataFolder, getDataFolder())) {
                File dataFolder = new File(getDataFolder(), "data");
                for (File file : dataFolder.listFiles()) {
                    file.delete();
                }
            }
        }

        registerManager(
            new RewardModuleTypeManager(),
            new RewardManager()
        );

        updater = new Updater(this, "djC8I9ui", "lushrewards.update", "rewards update");
        notificationHandler = new NotificationHandler();
        localPlaceholders = new LocalPlaceholders();

        configManager = new ConfigManager();
        configManager.reloadConfig();

        dataManager = new DataManager();
        dataManager.enable();

        addHook("floodgate", () -> registerHook(new FloodgateHook()));
        addHook("PlaceholderAPI", () -> registerHook(new PlaceholderAPIHook()));

        new RewardUserListener().registerListeners();

        registerCommand(new RewardsCommand());

        getModule(RewardModule.Type.PLAYTIME_TRACKER).ifPresent(module -> {
            if (module instanceof PlaytimeTrackerModule playtimeTracker) {
                Bukkit.getOnlinePlayers().forEach(playtimeTracker::startPlaytimeTracker);
            }
        });
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }

        if (notificationHandler != null) {
            notificationHandler.stopNotificationTask();
            notificationHandler = null;
        }

        if (hooks != null) {
            unregisterAllHooks();
            hooks = null;
        }

        if (modules != null) {
            unregisterAllModules();
            modules = null;
        }

        if (dataManager != null) {
            dataManager.disable();
            dataManager = null;
        }

        configManager = null;
        localPlaceholders = null;

        morePaperLib.scheduling().cancelGlobalTasks();
        LushLib.getInstance().disable();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public NotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    public LocalPlaceholders getLocalPlaceholders() {
        return localPlaceholders;
    }

    public Updater getUpdater() {
        return updater;
    }

    public Gson getGson() {
        return GSON;
    }

    public List<RewardModule> getRewardModules() {
        return modules.values().stream()
            .filter(module -> module instanceof RewardModule)
            .map(module -> (RewardModule) module)
            .toList();
    }

    public List<RewardModule> getEnabledRewardModules() {
        return modules.values().stream()
            .filter(module -> module instanceof RewardModule && module.isEnabled())
            .map(module -> (RewardModule) module)
            .toList();
    }

    public static LushRewards getInstance() {
        return plugin;
    }

    public static MorePaperLib getMorePaperLib() {
        return morePaperLib;
    }
}
