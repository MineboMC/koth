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

    public NextKothTimer(Plugin plugin) {
        super(300, plugin); // 300 seconds = 5 minutes
    }

    @Override
    protected void onStart() {
        Koth.nextKoth = Koth.getRandomKoth();

        if (Koth.nextKoth == null) {
            Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] &cAn error occured... No koths available."));
            // Restart the auto timer
            KoTH.getInstance().scheduleNextAutoKoth();
            return;
        }
    }

    @Override
    protected boolean onTick(int secondsLeft) {
        if (Koth.currentKoth != null) {
            cancel();
            return false;
        }

        if (secondsLeft % 60 == 0 || secondsLeft == 30 || secondsLeft == 10 || secondsLeft <= 5) {

            String timeFormat = formatTime(secondsLeft);

            Bukkit.broadcastMessage(ColorUtil.translateColors("&6[KoTH] &e" + Koth.nextKoth.getName() + " &6will be open in &e" + timeFormat + "&6!"));

            for (Player p :  Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
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
            return seconds + (seconds == 1 ? " second" :  " seconds");
        }
    }
}