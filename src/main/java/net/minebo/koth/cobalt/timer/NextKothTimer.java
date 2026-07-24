package net.minebo.koth.cobalt.timer;

import net.minebo.cobalt.timer.GlobalTimer;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.koth.KoTH;
import net.minebo.koth.koth.Koth;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NextKothTimer extends GlobalTimer {

    public NextKothTimer(Plugin plugin, int durationSeconds) {
        super(durationSeconds, plugin);
    }

    @Override
    protected void onStart() {
        Koth.nextKoth = Koth.getRandomKoth();
        if (Koth.nextKoth == null) {
            Bukkit.broadcastMessage(ColorUtil.translateColors("<gold>[KoTH] <red>An error occured... No koths available."));
            cancel(); // stop this timer
            // Restart the auto timer (uses the initial delay)
            KoTH.getInstance().scheduleNextAutoKoth();
            return;
        }
        Bukkit.broadcastMessage(ColorUtil.translateColors("<gold>[KoTH] <yellow>" + Koth.nextKoth.getName() + " <gold>will be open in <yellow>" + formatTime(durationSeconds) + "<gold>!"));
    }

    @Override
    protected boolean onTick(int secondsLeft) {
        if (Koth.currentKoth != null) {
            cancel();
            return false;
        }
        return true;
    }

    @Override
    protected void onComplete() {
        Koth.nextKoth.start();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
    }

    private String formatTime(int seconds) {
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
            return seconds + (seconds == 1 ? " second" : " seconds");
        }
    }
}