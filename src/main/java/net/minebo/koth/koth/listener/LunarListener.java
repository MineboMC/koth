package net.minebo.koth.koth.listener;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.event.ApolloListener;
import com.lunarclient.apollo.event.Listen;
import com.lunarclient.apollo.event.player.ApolloRegisterPlayerEvent;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import net.minebo.koth.koth.Koth;
import net.minebo.koth.koth.event.KothEndEvent;
import net.minebo.koth.koth.event.KothStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.Color;
import java.util.Optional;

import static org.bukkit.Bukkit.getLogger;

public class LunarListener implements Listener, ApolloListener {

    private final WaypointModule waypointModule;

    public LunarListener() {
        this.waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
    }

    @Listen
    public void onRegister(ApolloRegisterPlayerEvent event) {
        if (this.waypointModule == null) return;

        final ApolloPlayer apolloPlayer = event.getPlayer();
        boolean hasSupport = Apollo.getPlayerManager().hasSupport(apolloPlayer.getUniqueId());

        if (!hasSupport) return;

        final Koth current = Koth.currentKoth;

        if (current == null) return;

        Waypoint waypoint = generateWaypoint(current);
        if (waypoint != null) this.waypointModule.displayWaypoint(apolloPlayer, waypoint);
    }

    // Bukkit custom event
    @EventHandler
    public void onKothStart(KothStartEvent event) {
        if (this.waypointModule == null) return;

        final Koth current = event.getKoth(); // use event value rather than global
        if (current == null) return;

        Waypoint waypoint = generateWaypoint(current);
        if (waypoint == null) return;

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!Apollo.getPlayerManager().hasSupport(player.getUniqueId())) return;

            Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            apolloPlayerOpt.ifPresent(apolloPlayer -> this.waypointModule.displayWaypoint(apolloPlayer, waypoint));
        });
    }

    // Bukkit custom event
    @EventHandler
    public void onKothEnd(KothEndEvent event) {
        if (this.waypointModule == null) return;
        if (event.getKoth() == null) return;

        final String waypointName = event.getKoth().getName() + " KoTH";

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!Apollo.getPlayerManager().hasSupport(player.getUniqueId())) return;

            Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            apolloPlayerOpt.ifPresent(apolloPlayer -> this.waypointModule.removeWaypoint(apolloPlayer, waypointName));
        });
    }

    private Waypoint generateWaypoint(Koth koth) {
        if (koth == null || koth.getMidPoint() == null || koth.getMidPoint().getWorld() == null) {
            return null;
        }

        return Waypoint.builder()
                .name(koth.getName() + " KoTH")
                .location(ApolloBlockLocation.builder()
                        .world(koth.getMidPoint().getWorld().getName())
                        .x(koth.getMidPoint().getBlockX())
                        .y(koth.getMidPoint().getBlockY())
                        .z(koth.getMidPoint().getBlockZ())
                        .build())
                .highlightBlock(false)
                .showBeam(true)
                .color(Color.YELLOW)
                .preventRemoval(true)
                .hidden(false)
                .build();
    }
}