// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.ChatComponentText;


public class HypixelFastFall3Speed implements ISpeed {

    boolean waitingToDisable = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            if (waitingToDisable) {
                Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
                return;
            }
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe((float) (0.62f + Math.random() * 0.024f));
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.61f + 0.024f * (amplifier + 1));
                }
            }
        } else {

            if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled  && mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 30) {
                if (mc.thePlayer.offGroundTicks == 1) mc.thePlayer.addChatMessage(new ChatComponentText(String.valueOf(MovementUtil.getSpeed())));
//                if ( mc.thePlayer.offGroundTicks == 3  || (mc.thePlayer.offGroundTicks >= 7 &&
//                        mc.thePlayer.offGroundTicks <= 8)) {
//                    MovementUtil.customStrafeStrength(30);
//                }
                switch (mc.thePlayer.offGroundTicks) {
                    case 1:
                        MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.349f));
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtil.strafe(0.37f);
                        }
                    case 8: {
                        mc.thePlayer.motionY += 0.015f;
                        break;
                    }
                    case 3: {
                        mc.thePlayer.motionY -= -0.0045;
                        break;
                    }
                    case 4: {
                        mc.thePlayer.motionY -= 0.186;
                        break;
                    }
                    case 5: {
                        mc.thePlayer.motionY -= 0.042;
                        break;
                    }
                    case 6: {
                    //    MovementUtil.customStrafeStrength(80);
                        break;
                        }
                    case 7: {
                        //mc.thePlayer.motionY -= 0.004;
                        mc.thePlayer.motionY += 0.012f;
                        break;
                    }
                }
            }
        }

    }

    @Override
    public void onDisable() {
        if (mc.thePlayer.onGround) {
            waitingToDisable = false;
        } else {
            waitingToDisable = true;
            Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
        }
    }

    @Override
    public String toString() {
        return "Hypixel Fastfall 3";
    }
}
