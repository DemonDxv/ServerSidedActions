package dev.demon.api.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import dev.demon.ServerSidedActions;
import dev.demon.api.user.PlayerData;
import org.bukkit.entity.Player;

public class PacketHook extends PacketListenerAbstract {

    public PacketHook() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            PlayerData user = ServerSidedActions.INSTANCE.getUserManager().getUser(player);

            if (user != null) {

                // We use the metadata packet to see if they are on fire, as its easier to do it this way
                // Than to check if they walked near water to stop being on fire.
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {

                    WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(event);

                    // Confirm it's the players we want.
                    if (user.getPlayer().getEntityId() != metadata.getEntityId()) {
                        return;
                    }

                    // Check if the data is there.
                    if (metadata.getEntityMetadata() != null && metadata.getEntityMetadata().size() > 0) {

                        for (EntityData data : metadata.getEntityMetadata()) {

                            // Check for a byte as that's what fire is located under.
                            if (data.getValue() instanceof Byte) {

                                byte value = (byte) data.getValue();

                                //index 0, 0 = no fire, 1, 0 = fire.
                                if (value == 0 && data.getIndex() == 0) {
                                    user.setOnFire(false);
                                } else if (value == 1 && data.getIndex() == 0) {
                                    user.setOnFire(true);
                                }
                            }
                        }
                    }
                }

                /**
                 * Experimental server sided potion stuff..
                 */

                if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
                    WrapperPlayServerEntityEffect effect = new WrapperPlayServerEntityEffect(event);

                    if (effect.getEntityId() != user.getPlayer().getEntityId()) {
                        return;
                    }

             //       Bukkit.broadcastMessage("sent add");

               //     user.getPotionListMap().add(new PotionEffectInfo(effect.getEffectAmplifier(),
                 //           effect.getPotionType().getId(user.getVersion()),
                   //         effect.getEffectDurationTicks()));
                }

                if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
                    WrapperPlayServerRemoveEntityEffect effect = new WrapperPlayServerRemoveEntityEffect(event);

                    if (effect.getEntityId() != user.getPlayer().getEntityId()) {
                        return;
                    }

                //    user.getPotionListMap().removeIf(info ->
                  //          info.getId() == effect.getPotionType().getId(user.getVersion()));
                }
            }
        }
    }
}
