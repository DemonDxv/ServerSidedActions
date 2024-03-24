package dev.demon.api.event;

import dev.demon.api.user.PlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class ServerTickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final int serverTick;
    private final PlayerData playerData;

    public ServerTickEvent(int serverTick, PlayerData playerData) {
        this.serverTick = serverTick;
        this.playerData = playerData;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}