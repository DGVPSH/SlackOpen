// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.start.Slack;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class DelayTillGroundVelocity implements IVelocity {

    private boolean damaged = false;

    @Override
    public void onDisable() {
        BlinkUtil.disable();
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = event.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                damaged = true;
            }
        }
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        BlinkUtil.BLINK_INBOUND = true;
        BlinkUtil.isEnabled = true;
        if (mc.thePlayer.onGround || !damaged) {
            BlinkUtil.releasePackets(true, false);
            damaged = false;
        }
    }

    @Override
    public String toString() {
        return "Delay Till Ground";
    }
}
