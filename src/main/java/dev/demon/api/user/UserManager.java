package dev.demon.api.user;

import dev.demon.ServerSidedActions;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {
    private final Map<UUID, PlayerData> userMap = new ConcurrentHashMap<>();

    public void addUser(Player player) {
        UUID uuid = player.getUniqueId();
        userMap.computeIfAbsent(uuid, key -> new PlayerData(player));
    }

    public PlayerData getUser(Player player) {
        return this.userMap.get(player.getUniqueId());
    }

    public void removeUser(Player player) {
        PlayerData playerData = ServerSidedActions.INSTANCE.getUserManager().getUser(player);

        if (playerData != null) {
            this.userMap.remove(player.getUniqueId());
        }
    }
}