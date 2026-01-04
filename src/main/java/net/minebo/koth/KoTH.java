package net.minebo.koth;

import co.aikar.commands.PaperCommandManager;
import com.lunarclient.apollo.event.EventBus;
import lombok.Getter;
import net.minebo.cobalt.acf.ACFCommandController;
import net.minebo.cobalt.acf.ACFManager;
import net.minebo.cobalt.gson.Gson;
import net.minebo.koth.cobalt.completion.KothCompletionHandler;
import net.minebo.koth.cobalt.context.KothContextResolver;
import net.minebo.koth.cobalt.timer.AutoKothTimer;
import net.minebo.koth.cobalt.timer.NextKothTimer;
import net.minebo.koth.command.KothCommands;
import net.minebo.koth.koth.Koth;
import net.minebo.koth.koth.listener.KothListener;
import net.minebo.koth.koth.listener.LunarListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.N;

@Getter
public class KoTH extends JavaPlugin {

    @Getter
    private static KoTH instance;

    public AutoKothTimer autoKothTimer;
    public NextKothTimer nextKothTimer;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Cobalt Gson
        Gson.init();

        // Load KoTHs
        Koth.loadAll();
        Koth.init();

        // Register listeners
        getServer().getPluginManager().registerEvents(new KothListener(), this);

        if(Bukkit.getPluginManager().isPluginEnabled("Apollo-Bukkit")) {
            getServer().getPluginManager().registerEvents(new LunarListener(), this);
            EventBus.getBus().register(new LunarListener());
        }

        // Register commands with ACF
        new ACFManager(this);

        ACFCommandController.registerAll(this);
        ACFCommandController.registerCompletion("koths", new KothCompletionHandler());
        ACFCommandController.registerContext(Koth.class, new KothContextResolver());

        // Start auto koth timer if enabled
        if (getConfig().getBoolean("auto-koth.enabled", true)) {
            scheduleNextAutoKoth();
        }
    }

    @Override
    public void onDisable() {
        // Stop any active KoTH
        if (Koth.currentKoth != null) {
            Koth.currentKoth.end(null);
        }

        // Save KoTHs
        Koth.saveAll();
    }

    public void scheduleNextAutoKoth() {
        int delay = getConfig().getInt("auto-koth.delay", 1000);
        int playerRequirement = getConfig().getInt("auto-koth.player-requirement", 0);

        nextKothTimer = new NextKothTimer(this);
        autoKothTimer = new AutoKothTimer(this, delay, playerRequirement);
        autoKothTimer.start();
    }

    public void cancelAutoKoth() {
        if (autoKothTimer != null) {
            autoKothTimer.cancel();
            autoKothTimer = null;
        }
    }
}