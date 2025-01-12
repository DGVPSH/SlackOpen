package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.features.modules.impl.world.Breaker;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.utils.player.AttackUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class HypixelVelocity implements IVelocity {

    @Override
    public void onPacket(PacketEvent event) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = event.getPacket();
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    event.cancel();
                    if (mc.thePlayer.onGround || AttackUtil.inCombat) {
                        if (mc.thePlayer.offGroundTicks > 2 && mc.thePlayer.offGroundTicks < 8 &&
                                !Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle())
                            mc.thePlayer.motionY = packet.getMotionY() / 8000.0;
                    }
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = packet.getMotionY() / 8000.0;
                    }
                }
            }
    }

    @Override
    public String toString() {
        return "Hypixel";
    }
}
