package net.minebo.koth.koth;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import net.minebo.cobalt.gson.Gson;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.koth.KoTH;
import net.minebo.koth.koth.event.*;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Setter @Getter
public class Koth {

    public static List<Koth> koths = new ArrayList<>();

    public static Koth currentKoth;
    public static Koth lastKoth;
    public static Koth nextKoth;

    String name;
    Location midPoint;
    Integer capSize;
    Integer capTime;

    private transient UUID cappingPlayer;
    private transient int capProgress;
    private transient BukkitRunnable capTask;

    public static void init() {
        currentKoth = null;
        lastKoth = null;
    }

    public static Koth get(String name) {
       return koths.stream().filter(k -> k.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public String getRemainingInWords() {
        Integer seconds = capTime - capProgress;

        if (seconds >= 60) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;

            if (remainingSeconds == 0) {
                return minutes + (minutes == 1 ? " minute" : " minutes");
            } else {
                return minutes + (minutes == 1 ? " minute" : " minutes") +
                        " " + remainingSeconds + (remainingSeconds == 1 ? " second" : " seconds");
            }
        } else {
            return seconds + (seconds == 1 ? " second" :  " seconds");
        }
    }

    public String getRemaining() {
        long millisLeft = TimeUnit.SECONDS.toMillis(capTime - capProgress);
        if (millisLeft <= 0) return "0";

        if (millisLeft >= TimeUnit.MINUTES.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "mm:ss");
        }

        if (millisLeft >= TimeUnit.HOURS.toMillis(1)) {
            return DurationFormatUtils.formatDuration(millisLeft, "hh:mm:ss");
        }

        // Show seconds with one decimal (e.g., 15.3s)
        double seconds = millisLeft / 1000.0;
        return String.format("%.1fs", seconds);
    }

    public void start() {
        currentKoth = this;
        cappingPlayer = null;
        capProgress = 0;

        KothStartEvent startEvent = new KothStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);

        Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] &e" + currentKoth.getName() + "&6 can now be contested!"));
    }

    public void end(Player winner) {
        if (capTask != null) {
            capTask.cancel();
            capTask = null;
        }

        cappingPlayer = null;
        capProgress = 0;

        KothEndEvent endEvent = new KothEndEvent(this, winner);
        Bukkit.getPluginManager().callEvent(endEvent);

        lastKoth = this;
        currentKoth = null;

        if (winner != null) {
            Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] " + winner.getDisplayName() + " &6has captured &e" + name + "&6!"));
        } else {
            Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] " + currentKoth.getName() + " &6has been terminated."));
        }

        // Schedule the next auto koth
        KoTH.getInstance().scheduleNextAutoKoth();
    }

    public void startCapping(Player player) {
        if (cappingPlayer != null) {
            return;
        }

        cappingPlayer = player.getUniqueId();
        capProgress = 0;

        player.sendMessage(ColorUtil.translateColors("&6[KoTH] " + "&eYou &6are now controlling &e" + name + "&6!"));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (! p.getUniqueId().equals(player.getUniqueId())) {
                p.sendMessage(ColorUtil.translateColors("&6[KoTH] " + "&eSomeone" + " &6is controlling &e" + name + "&6!"));
            }
        }

        capTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player capper = Bukkit.getPlayer(cappingPlayer);

                if (capper == null || ! capper.isOnline()) {
                    stopCapping();
                    return;
                }

                if (capper.getLocation().distance(midPoint) > capSize) {
                    stopCapping();
                    capper.sendActionBar(ColorUtil.translateColors("&cYou left the capture zone!"));
                    return;
                }

                capProgress++;

                KothCapProgressEvent progressEvent = new KothCapProgressEvent(
                        Koth.this, capper, capProgress, capTime
                );
                Bukkit.getPluginManager().callEvent(progressEvent);

                int percentage = (capProgress * 100) / capTime;
                capper.sendActionBar(ColorUtil.translateColors("&6You are controlling &e" + currentKoth.name + "&6! &7(&e" + percentage + "%&7)"));

                if (capProgress >= capTime) {
                    capper.sendActionBar(ColorUtil.translateColors("&6You have captured &e" + currentKoth.name + "&6!"));
                    end(capper);
                }
            }
        };

        capTask.runTaskTimer(KoTH.getInstance(), 20L, 20L);
    }

    public void stopCapping() {
        if (capTask != null) {
            capTask.cancel();
            capTask = null;
        }

        Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] " + "&6Control of &e" + currentKoth.name + "&6 lost!"));

        cappingPlayer = null;
        capProgress = 0;
    }

    public boolean isCapping(Player player) {
        return cappingPlayer != null && cappingPlayer.equals(player.getUniqueId());
    }

    public boolean isInCaptureZone(Player player) {
        if (midPoint == null || midPoint.getWorld() != player.getWorld()) {
            return false;
        }
        return player.getLocation().distance(midPoint) <= capSize;
    }

    public static Koth getRandomKoth() {
        if (koths.isEmpty()) return null;

        Random rand = new Random();
        Koth randomKoth = koths.get(rand.nextInt(koths.size()));

        if(lastKoth != null && lastKoth == randomKoth && koths.size() > 1) {
            return getRandomKoth();
        }

        return randomKoth;
    }

    public static void loadAll() {
        File file = new File(KoTH.getInstance().getDataFolder(), "koths.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Koth>>() {}.getType();
                List<Koth> loadedKoths = Gson.GSON.fromJson(reader, listType);

                if (loadedKoths != null) {
                    koths = loadedKoths;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        File file = new File(KoTH.getInstance().getDataFolder(), "koths.json");
        file.getParentFile().mkdirs();

        try (Writer writer = new FileWriter(file)) {
            Gson.GSON.toJson(koths, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}