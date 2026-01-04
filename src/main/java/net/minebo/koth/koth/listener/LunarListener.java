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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.util.Optional;

public class LunarListener implements Listener, ApolloListener {

    public static WaypointModule waypointModule;

    public LunarListener() {
        waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
    }

    @Listen
    public void onRegister(ApolloRegisterPlayerEvent event) {
        ApolloPlayer player = event.getPlayer();

        if(Koth.currentKoth != null) {
            waypointModule.displayWaypoint(player, generateWaypoint(Koth.currentKoth));
        }
    }

    @EventHandler
    public void onKothStart(KothStartEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (Koth.currentKoth != null) {
                Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());

                apolloPlayerOpt.ifPresent(apolloPlayer -> {
                    waypointModule.displayWaypoint(apolloPlayer, generateWaypoint(Koth.currentKoth));
                });
            }
        });
    }

    @EventHandler
    public void onKothEnd(KothEndEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Optional<ApolloPlayer> apolloPlayerOpt = Apollo.getPlayerManager().getPlayer(player.getUniqueId());
            apolloPlayerOpt.ifPresent(apolloPlayer -> waypointModule.removeWaypoint(apolloPlayer, event.getKoth().getName() + " KoTH"));
        });
    }

    public Waypoint generateWaypoint(Koth koth) {
        return Waypoint.builder()
                .name(koth.getName() + " KoTH")
                .location(ApolloBlockLocation.builder()
                        .world(koth.getMidPoint().getWorld().getName())
                        .x(koth.getMidPoint().getBlockX())
                        .y(koth.getMidPoint().getBlockY())
                        .z(koth.getMidPoint().getBlockZ())
                        .build()
                )
                .color(Color.YELLOW)
                .preventRemoval(true)
                .hidden(false)
                .build();
    }
}
