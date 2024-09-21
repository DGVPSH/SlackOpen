package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.start.Slack;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class MMCVelocity implements IVelocity {

    @Override
    public void onUpdate(UpdateEvent event) {
        Velocity velocityModule = Slack.getInstance().getModuleManager().getInstance(Velocity.class);
        if (mc.thePlayer.hurtTime == 10) {
            PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }

        if (mc.thePlayer.hurtTime == 4) {
            PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        }

        if (mc.thePlayer.hurtTime > 4) {
            mc.thePlayer.motionZ *= 0.6;
            mc.thePlayer.motionX *= 0.6;
        }

        if (mc.thePlayer.hurtTime <7 && mc.thePlayer.hurtTime > 3 && mc.thePlayer.onGround) {
            MovementUtil.strafe();
        }
    }

    @Override
    public String toString() {
        return "MMC";
    }
}
