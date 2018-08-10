package com.garbagemule.MobArena;

import com.garbagemule.MobArena.commands.CommandHandler;
import com.garbagemule.MobArena.config.LoadsConfigFile;
import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.framework.ArenaMaster;
import com.garbagemule.MobArena.listeners.MAGlobalListener;
import com.garbagemule.MobArena.listeners.MagicSpellsListener;
import com.garbagemule.MobArena.metrics.ArenaCountChart;
import com.garbagemule.MobArena.metrics.ClassChestsChart;
import com.garbagemule.MobArena.metrics.ClassCountChart;
import com.garbagemule.MobArena.metrics.FoodRegenChart;
import com.garbagemule.MobArena.metrics.IsolatedChatChart;
import com.garbagemule.MobArena.metrics.MonsterInfightChart;
import com.garbagemule.MobArena.metrics.PvpEnabledChart;
import com.garbagemule.MobArena.metrics.VaultChart;
import com.garbagemule.MobArena.signs.ArenaSign;
import com.garbagemule.MobArena.signs.SignBootstrap;
import com.garbagemule.MobArena.signs.SignListeners;
import com.garbagemule.MobArena.things.ThingManager;
import com.garbagemule.MobArena.util.VersionChecker;
import com.garbagemule.MobArena.util.config.ConfigUtils;
import com.garbagemule.MobArena.waves.ability.AbilityManager;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;

/**
 * MobArena
 * @author garbagemule
 */
public class MobArena extends JavaPlugin
{
    private ArenaMaster arenaMaster;

    // Vault
    private Economy economy;

    private FileConfiguration config;
    private LoadsConfigFile loadsConfigFile;
    private Throwable lastFailureCause;

    public static final double MIN_PLAYER_DISTANCE_SQUARED = 225D;
    public static Random random = new Random();

    private Messenger messenger;
    private ThingManager thingman;

    private SignListeners signListeners;

    @Override
    public void onLoad() {
        thingman = new ThingManager(this);
    }

    public void onEnable() {
        ServerVersionCheck.check(getServer());
        try {
            setup();
            reload();
            checkForUpdates();
        } catch (ConfigError e) {
            getLogger().log(Level.SEVERE, "You have an error in your config-file!\n\n  " + e.getMessage() + "\n");
            getLogger().log(Level.SEVERE, "Fix it, then run /ma load");
        }
    }

    public void onDisable() {
        if (arenaMaster != null) {
            arenaMaster.getArenas().forEach(Arena::forceEnd);
            arenaMaster.resetArenaMap();
            arenaMaster = null;
        }
        loadsConfigFile = null;
        ConfigurationSerialization.unregisterClass(ArenaSign.class);
        VersionChecker.shutdown();
    }

    private void setup() {
        try {
            createDataFolder();
            setupArenaMaster();
            setupCommandHandler();

            registerConfigurationSerializers();
            setupVault();
            setupMagicSpells();
            setupBossAbilities();
            setupListeners();
            setupMetrics();
        } catch (RuntimeException e) {
            setLastFailureCauseAndRethrow(e);
        }
        lastFailureCause = null;
    }

    private void createDataFolder() {
        File dir = getDataFolder();
        if (!dir.exists()) {
            if (dir.mkdir()) {
                getLogger().info("Data folder plugins/MobArena created.");
            } else {
                getLogger().warning("Failed to create data folder plugins/MobArena!");
            }
        }
    }
    
    private void setupArenaMaster() {
        arenaMaster = new ArenaMasterImpl(this);
    }

    private void setupCommandHandler() {
        getCommand("ma").setExecutor(new CommandHandler(this));
    }

    private void registerConfigurationSerializers() {
        ConfigurationSerialization.registerClass(ArenaSign.class);
    }

    private void setupVault() {
        Plugin vaultPlugin = this.getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null) {
            getLogger().info("Vault was not found. Economy rewards will not work.");
            return;
        }

        ServicesManager manager = this.getServer().getServicesManager();
        RegisteredServiceProvider<Economy> e = manager.getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (e != null) {
            economy = e.getProvider();
            getLogger().info("Vault found; economy rewards enabled.");
        } else {
            getLogger().warning("Vault found, but no economy plugin detected. Economy rewards will not work!");
        }
    }

    private void setupMagicSpells() {
        Plugin spells = this.getServer().getPluginManager().getPlugin("MagicSpells");
        if (spells == null) {
            return;
        }

        getLogger().info("MagicSpells found, loading config-file.");
        this.getServer().getPluginManager().registerEvents(new MagicSpellsListener(this), this);
    }

    private void setupBossAbilities() {
        AbilityManager.loadCoreAbilities();
        AbilityManager.loadCustomAbilities(getDataFolder());
    }

    private void setupListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new MAGlobalListener(this, arenaMaster), this);
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new VaultChart(this));
        metrics.addCustomChart(new ArenaCountChart(this));
        metrics.addCustomChart(new ClassCountChart(this));
        metrics.addCustomChart(new ClassChestsChart(this));
        metrics.addCustomChart(new FoodRegenChart(this));
        metrics.addCustomChart(new IsolatedChatChart(this));
        metrics.addCustomChart(new MonsterInfightChart(this));
        metrics.addCustomChart(new PvpEnabledChart(this));
    }

    public void reload() {
        try {
            reloadConfig();
            reloadGlobalMessenger();
            reloadArenaMaster();
            reloadAnnouncementsFile();
            reloadSigns();
        } catch (RuntimeException e) {
            setLastFailureCauseAndRethrow(e);
        }
        lastFailureCause = null;
    }

    @Override
    public void reloadConfig() {
        if (loadsConfigFile == null) {
            loadsConfigFile = new LoadsConfigFile(this);
        }
        config = loadsConfigFile.load();
    }

    private void reloadGlobalMessenger() {
        String prefix = config.getString("global-settings.prefix", "");
        if (prefix.isEmpty()) {
            prefix = ChatColor.GREEN + "[MobArena] ";
        }
        messenger = new Messenger(prefix);
    }

    private void reloadArenaMaster() {
        arenaMaster.getArenas().forEach(Arena::forceEnd);
        arenaMaster.initialize();
    }

    private void reloadAnnouncementsFile() {
        // Create if missing
        File file = new File(getDataFolder(), "announcements.yml");
        try {
            if (file.createNewFile()) {
                getLogger().info("announcements.yml created.");
                YamlConfiguration yaml = Msg.toYaml();
                yaml.save(file);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Otherwise, load the announcements from the file
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(file);
            ConfigUtils.addMissingRemoveObsolete(file, Msg.toYaml(), yaml);
            Msg.load(yaml);
        } catch (IOException e) {
            throw new RuntimeException("There was an error reading the announcements-file:\n" + e.getMessage());
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("\n\n>>>\n>>> There is an error in your announements-file! Handle it!\n>>> Here's what snakeyaml says:\n>>>\n\n" + e.getMessage());
        }
    }

    private void reloadSigns() {
        if (signListeners != null) {
            signListeners.unregister();
        }
        SignBootstrap bootstrap = SignBootstrap.create(this);
        signListeners = new SignListeners();
        signListeners.register(bootstrap);
    }

    private void checkForUpdates() {
        if (getConfig().getBoolean("global-settings.update-notification", false)) {
            VersionChecker.checkForUpdates(this, null);
        }
    }

    private void setLastFailureCauseAndRethrow(RuntimeException up) {
        lastFailureCause = up;
        throw up;
    }

    public Throwable getLastFailureCause() {
        return lastFailureCause;
    }

    public File getPluginFile() {
        return getFile();
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public ArenaMaster getArenaMaster() {
        return arenaMaster;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Messenger getGlobalMessenger() {
        return messenger;
    }

    public ThingManager getThingManager() {
        return thingman;
    }
}
