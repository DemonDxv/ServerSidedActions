package dev.demon.api.user;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import dev.demon.objects.PotionEffectInfo;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class PlayerData {
    private final UUID uuid;
    private final Player player;
    private final String username;
    private ClientVersion version;

    private int lastRegainHealthTick = 0;
    private int exemptRegainHealthTicks = 0;

    private int lastShootTick = 0;

    private int lastConsumeTick = 0;

    private int fireTick = 0;
    private int fireTickedTimes = 0;
    private boolean fireReady = false;
    private boolean onFire = false;

    private final List<PotionEffectInfo> potionListMap = new CopyOnWriteArrayList<>();

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.username = player.getName();

        this.version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
    }
}