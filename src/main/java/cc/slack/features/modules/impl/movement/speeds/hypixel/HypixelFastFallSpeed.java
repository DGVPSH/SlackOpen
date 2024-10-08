// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.State;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import net.minecraft.potion.Potion;


public class HypixelFastFallSpeed implements ISpeed {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe(MovementUtil.getSpeed() + 0.23f);
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                mc.thePlayer.motionX *= 0.95;
                mc.thePlayer.motionZ *= 0.95;
                if (MovementUtil.getSpeed() < 0.46)
                    MovementUtil.strafe(0.46f);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.47f + 0.024f * (amplifier + 1));
                }
            }
        } else {

            if (mc.thePlayer.offGroundTicks == 5 && mc.thePlayer.hurtTime < 5 && Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled) {
                mc.thePlayer.motionY = -0.1523351824467155;
            }

            mc.thePlayer.motionX *= 1.0005;
            mc.thePlayer.motionZ *= 1.0005;
        }

    }

    @Override
    public String toString() {
        return "Hypixel FastFall";
    }
}
