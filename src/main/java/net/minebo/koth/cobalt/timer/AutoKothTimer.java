package net.minebo.koth.cobalt.timer;

import net.minebo.cobalt.timer.GlobalTimer;
import net.minebo.cobalt.timer.Timer;
import net.minebo.cobalt.util.ColorUtil;
import net.minebo.koth.KoTH;
import net.minebo.koth.koth.Koth;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AutoKothTimer extends GlobalTimer {

    private final int playerRequirement;

    public AutoKothTimer(Plugin plugin, int delaySeconds, int playerRequirement) {
        super(delaySeconds, plugin);
        this.playerRequirement = playerRequirement;
    }

    @Override
    protected void onStart() {
        // Silent start - no announcement
    }

    @Override
    protected boolean onTick(int secondsLeft) {
        // Only tick every 60 seconds to check player requirement
        if (secondsLeft % 60 != 0) {
            return true;
        }

        // Check if a KoTH is already active
        if (Koth.currentKoth != null) {
            return true; // Keep waiting
        }

        // Check player requirement
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers < playerRequirement) {
            if (secondsLeft % 300 == 0) { // Announce every 5 minutes
                Bukkit.broadcastMessage(ColorUtil.translateColors(
                    "<gold><bold>KoTH<reset> <red>delayed - need at least <yellow>" + playerRequirement +
                    " <red>players online. <gray>(" + onlinePlayers + "/" + playerRequirement + ")"
                ));
            }
            return true; // Keep waiting
        }

        return true;
    }

    @Override
    protected void onComplete() {
        // Check if a KoTH is already active
        if (Koth.currentKoth != null) {
            // Restart the auto timer
            KoTH.getInstance().scheduleNextAutoKoth();
            return;
        }

        // Check player requirement one last time
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers < playerRequirement) {
            Bukkit.broadcastMessage(ColorUtil.translateColors(
                "<gold><bold>KoTH <reset><red>delayed - need at least <yellow>" + playerRequirement +
                " <red>players online.<gray>(" + onlinePlayers + "/" + playerRequirement + ")"
            ));
            // Restart the auto timer
            KoTH.getInstance().scheduleNextAutoKoth();
            return;
        }

        // Check if there are any KoTHs available
        if (Koth.koths.isEmpty()) {
            Bukkit.broadcastMessage(ColorUtil.translateColors("<red><bold>Error: <reset><red>No KoTH arenas configured!"));
            // Restart the auto timer
            KoTH.getInstance().scheduleNextAutoKoth();
            return;
        }

        // Start the 5-minute countdown timer
        KoTH.getInstance().getNextKothTimer().start();
    }
}