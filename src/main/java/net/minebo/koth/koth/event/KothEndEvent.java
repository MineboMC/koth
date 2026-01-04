package net.minebo.koth.koth.event;

import lombok.Getter;
import lombok.Setter;
import net.minebo.koth.koth.Koth;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter @Setter
public class KothEndEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Koth koth;
    private final Player winner; // Can be null if no winner
    
    public KothEndEvent(Koth koth, Player winner) {
        this.koth = koth;
        this.winner = winner;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}