// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.start.Slack;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class DelayVelocity implements IVelocity {

    boolean blink = false;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = event.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                BlinkUtil.enable(true, true);
                blink = true;
            }
        }

        Velocity velocityModule = Slack.getInstance().getModuleManager().getInstance(Velocity.class);
        if (mc.thePlayer.ticksSinceLastDamage == velocityModule.velocityTick.getValue() && blink) {
            BlinkUtil.disable();
            blink = false;
        }
    }

    @Override
    public String toString() {
        return "Delayed";
    }
}
