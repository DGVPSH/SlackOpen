// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import net.minecraft.potion.Potion;


public class HypixelFastFall2Speed implements ISpeed {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe(0.47f);
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.47f + 0.024f * (amplifier + 1));
                }
            }
        } else {

            if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled) {
                switch (mc.thePlayer.offGroundTicks) {
                    case 1:
                        MovementUtil.strafe(0.34f);
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtil.strafe(0.37f);
                        }
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.03;
                        break;
                    case 5:
                        mc.thePlayer.motionY -= 0.1905189780583944;
                        break;
                }
            }

            mc.thePlayer.motionX *= 1.0005;
            mc.thePlayer.motionZ *= 1.0005;

            if (mc.thePlayer.hurtTime == 9) {
                MovementUtil.strafe();
            }
        }

    }

    @Override
    public String toString() {
        return "Hypixel FastFall2";
    }
}
