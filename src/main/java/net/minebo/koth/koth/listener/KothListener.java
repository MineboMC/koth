package net.minebo.koth.koth.listener;

import net.minebo.koth.koth.Koth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class KothListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only check if player moved blocks (not just head movement)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Koth currentKoth = Koth.currentKoth;

        if (currentKoth == null) {
            return;
        }

        // Check if player is in capture zone
        if (currentKoth.isInCaptureZone(player)) {
            // Player is within cap zone - start capping if not already
            if (!currentKoth.isCapping(player)) {
                currentKoth.startCapping(player);
            }
        } else {
            // Player left cap zone - stop if they were capping
            if (currentKoth.isCapping(player)) {
                currentKoth.stopCapping();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Koth currentKoth = Koth.currentKoth;

        if (currentKoth != null && currentKoth.isCapping(event.getPlayer())) {
            currentKoth.stopCapping();
        }
    }

}