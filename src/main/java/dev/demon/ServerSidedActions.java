package dev.demon;

import com.github.retrooper.packetevents.PacketEvents;
import dev.demon.api.event.ServerTickEvent;
import dev.demon.api.packet.PacketHook;
import dev.demon.api.user.UserManager;
import dev.demon.listener.BukkitListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@Setter
public class ServerSidedActions extends JavaPlugin {

    public static ServerSidedActions INSTANCE;

    // User Manager
    private final UserManager userManager = new UserManager();

    // "Current" Server Tick
    private int serverTick = 0;

    // Executor service we use for tracking server ticks, and for making bukkit events "lighter".
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Register listeners
        this.listenersToRegister(consumer -> consumer.forEach(listener ->
                getServer().getPluginManager().registerEvents(listener, this)));

        // Register Packets from PacketEvents 2.0
        PacketEvents.getAPI().getEventManager().registerListener(new PacketHook());

        this.executorService.scheduleAtFixedRate(() -> {

            if (this.serverTick < 5000) {
                this.serverTick++;
            } else {
                this.serverTick = 0;
            }

            // Execute the server tick for each player, and call the event, so we can use it to track the timing
            this.userManager.getUserMap().values().forEach(playerData ->
                    Bukkit.getServer().getPluginManager()
                            .callEvent(new ServerTickEvent(this.serverTick, playerData)));

            // 50 milliseconds = 1 game tick.
        }, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDisable() {
        //
    }


    public void listenersToRegister(Consumer<List<Listener>> listConsumer) {
        listConsumer.accept(Collections.singletonList(new BukkitListener()));
    }
}
