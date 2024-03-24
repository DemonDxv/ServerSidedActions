package dev.demon.listener;

import dev.demon.ServerSidedActions;
import dev.demon.api.event.ServerTickEvent;
import dev.demon.api.user.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BukkitListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        this.processEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        this.processEvent(event);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onHealthUpdate(EntityRegainHealthEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        this.process(event);
    }

    @EventHandler
    public void onServerTick(ServerTickEvent event) {
        if (event.getPlayerData() != null) {

            // Make sure the tick is set, and they are on fire.
            if (event.getPlayerData().getFireTick() > 0 && event.getPlayerData().isOnFire()) {

                // Update their fire ticks to match with the server, so they can't modify packets to bypass.
                event.getPlayerData().getPlayer().setFireTicks(event.getPlayerData().getFireTick());
                event.getPlayerData().setFireTick(event.getPlayerData().getFireTick() - 1);
            } else {
                // Reset from the beginning.
                event.getPlayerData().setFireReady(false);
                event.getPlayerData().setFireTickedTimes(0);
            }

            /**
             * Scapped idea... (might come back to it)
             */


            /**
             * Note: On the client the sometimes it won't match up (it really depends)
             * Most of the time it's accurate for the client but little game speed-ups or slow-downs
             * May make it off timing for them in the inventory.
             * But shouldn't affect gameplay.
             */
       /*     for (PotionEffectInfo potionInfo : event.getPlayerData().getPotionListMap()) {

                if (potionInfo == null) return;

                if (potionInfo.getDuration() > 0) {
                    potionInfo.setDuration(potionInfo.getDuration() - 1);
                } else {
                    event.getPlayerData().getPotionListMap().remove(potionInfo);
                }

                PotionEffectType type = null;

                switch (potionInfo.getId()) {
                    case 2: {
                        type = PotionEffectType.SLOW;
                        break;
                    }

                    case 4: {
                        type = PotionEffectType.SLOW_DIGGING;
                        break;
                    }

                    case 9: {
                        type = PotionEffectType.CONFUSION;
                        break;
                    }

                    case 15: {
                        type = PotionEffectType.BLINDNESS;
                        break;
                    }

                    case 17: {
                        type = PotionEffectType.HUNGER;
                        break;
                    }

                    case 18: {
                        type = PotionEffectType.WEAKNESS;
                        break;
                    }

                    case 19: {
                        type = PotionEffectType.POISON;
                        break;
                    }

                    case 20: {
                        type = PotionEffectType.WITHER;
                        break;
                    }
                }

                if (type != null && potionInfo.getDuration() > 1) {
                    potionEffectUpdate(event.getPlayerData().getPlayer(), type, potionInfo.getDuration(),
                            potionInfo.getAmplifer());
                }
            }*/
        }
    }


    void processEvent(Event event) {
        ServerSidedActions.INSTANCE.getExecutorService().execute(() -> this.process(event));
    }

    void process(Event event) {
        if (event instanceof PlayerJoinEvent) {
            ServerSidedActions.INSTANCE.getUserManager().addUser(((PlayerJoinEvent) event).getPlayer());
        }

        if (event instanceof PlayerQuitEvent) {
            ServerSidedActions.INSTANCE.getUserManager().removeUser(((PlayerQuitEvent) event).getPlayer());
        }

        // Regen
        if (event instanceof EntityRegainHealthEvent) {

            if (((EntityRegainHealthEvent) event).getEntity() instanceof Player) {
                PlayerData data = ServerSidedActions.INSTANCE.getUserManager().getUser((Player)
                        ((EntityRegainHealthEvent) event).getEntity());

                if (data != null) {

                    // Use the server tick and not client packets as its unreliable.
                    int serverTick = ServerSidedActions.INSTANCE.getServerTick();

                    // Check the time between the current, and last heal time.
                    int delta = Math.abs(serverTick - data.getLastRegainHealthTick());

                    // If it's anything else than normal healing exempt for a moment.
                    if (((EntityRegainHealthEvent) event).getRegainReason()
                            != EntityRegainHealthEvent.RegainReason.SATIATED) {
                        data.setExemptRegainHealthTicks(3);
                    }

                    // Very basic method for detecting if the player's health is updating too quickly over time.
                    if (delta < 75 && ((EntityRegainHealthEvent) event).getRegainReason()
                            == EntityRegainHealthEvent.RegainReason.SATIATED && data.getExemptRegainHealthTicks() < 1) {

                        // Cancel the event so they can't heal themselves.
                        ((EntityRegainHealthEvent) event).setCancelled(true);
                    }

                    // Set the last healing tick from the server for comparison.
                    data.setLastRegainHealthTick(serverTick);
                }
            }
        }

        // Fast Consume
        if (event instanceof PlayerItemConsumeEvent) {
            PlayerData data = ServerSidedActions.INSTANCE.getUserManager().getUser(((PlayerItemConsumeEvent) event).getPlayer());

            if (data != null) {
                int serverTick = ServerSidedActions.INSTANCE.getServerTick();
                int delta = Math.abs(serverTick - data.getLastConsumeTick());

                // Basic time between last eat check.
                if (delta <= 30) {
                    ((PlayerItemConsumeEvent) event).setCancelled(true);
                }

                data.setLastConsumeTick(serverTick);
            }
        }

        // Fire Control
        if (event instanceof EntityDamageEvent) {

            if (((EntityDamageEvent) event).getEntity() instanceof Player) {
                PlayerData data = ServerSidedActions.INSTANCE.getUserManager().getUser((Player)
                        ((EntityDamageEvent) event).getEntity());

                if (data != null) {

                    if (((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.FIRE
                            || ((EntityDamageEvent) event).getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {

                        data.setFireTickedTimes(data.getFireTickedTimes() + 1);

                        // Basic fire control, basically, I check if they take at least 2 ticks of fire damage
                        // Then we set their fire ticks based on the server side.
                        if (data.getFireTickedTimes() > 1 && !data.isFireReady() && data.isOnFire()) {
                            data.setFireTick(170);
                            data.setFireReady(true);
                        }
                    }
                }
            }
        }

        // FastBow
        if (event instanceof EntityShootBowEvent) {
            if (((EntityShootBowEvent) event).getEntity() instanceof Player) {

                PlayerData data = ServerSidedActions.INSTANCE.getUserManager().getUser((Player)
                        ((EntityShootBowEvent) event).getEntity());

                if (data != null) {
                    int serverTick = ServerSidedActions.INSTANCE.getServerTick();
                    int delta = Math.abs(serverTick - data.getLastShootTick());

                    // Basic timing with speed bow check
                    if (this.isInvalidBowSpeed(delta, ((EntityShootBowEvent) event).getForce())) {
                        ((EntityShootBowEvent) event).setCancelled(true);
                    }

                    data.setLastShootTick(serverTick);
                }
            }
        }
    }

    public boolean isInvalidBowSpeed(int delta, float force) {
        return delta <= 3 && force > .3F || delta < 20 && force == 1.0F || delta < 7 && force > .5F;
    }

    public void potionEffectUpdate(Player player, PotionEffectType type, int duration, int amplifier) {
        Bukkit.getScheduler().runTask(ServerSidedActions.INSTANCE, () ->
                player.addPotionEffect(new PotionEffect(type, duration, amplifier)));
    }
}