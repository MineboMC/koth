package net.minebo.koth.koth.event;

import lombok.Getter;
import lombok.Setter;
import net.minebo.koth.koth.Koth;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class KothCapProgressEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Koth koth;
    private final Player player;
    private final int progress; // Seconds progressed
    private final int total; // Total seconds needed
    
    public KothCapProgressEvent(Koth koth, Player player, int progress, int total) {
        this.koth = koth;
        this.player = player;
        this.progress = progress;
        this.total = total;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}