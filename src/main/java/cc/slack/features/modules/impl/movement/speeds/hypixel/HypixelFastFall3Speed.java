// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
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
                MovementUtil.strafe((float) (0.52f + Math.random() * 0.024f));
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.65f + 0.024f * (amplifier + 1));
                }
            }
        } else {

            if(mc.theWorld.getBlockState(new BlockPos(0, mc.thePlayer.motionY, 0)).getBlock() != Blocks.air) {
                MovementUtil.strafe((float) MovementUtil.getBaseMoveSpeed());
            }

            if(mc.thePlayer.offGroundTicks == 1 && mc.thePlayer.posY - mc.thePlayer.motionY > -0.42) {
                mc.thePlayer.motionY = 0.38999998569488525;
                MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.329f));
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtil.strafe(0.349f);
                }
            }

            if(mc.thePlayer.offGroundTicks == 4 ) {
                mc.thePlayer.motionY -= 0.2;
            }

            if(mc.thePlayer.offGroundTicks == 3) {
                mc.thePlayer.motionY -= 0.13089999556541443;
            }

            if(mc.thePlayer.offGroundTicks == 6) {
                mc.thePlayer.motionY += 0.008f;
                MovementUtil.strafe();
                if ((Math.sqrt((mc.thePlayer.motionX * mc.thePlayer.motionZ ) + (mc.thePlayer.motionX  * mc.thePlayer.motionZ )) < MovementUtil.getBaseMoveSpeed() || mc.thePlayer.motionX  == 0.0d || mc.thePlayer.motionZ == 0.0d)) {
                    MovementUtil.strafe((float) (MovementUtil.getSpeed() - 0.01));
                }
            }

        }
//        if (mc.thePlayer.onGround) {
//            if (waitingToDisable) {
//                Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
//                return;
//            }
//            if (MovementUtil.isMoving()) {
//                MovementUtil.strafe((float) (0.62f + Math.random() * 0.024f));
//                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
//                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
//                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
//                    MovementUtil.strafe(0.61f + 0.024f * (amplifier + 1));
//                }
//            }
//        } else {
//            if (mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 30) {
////                if ( mc.thePlayer.offGroundTicks == 3  || (mc.thePlayer.offGroundTicks >= 7 &&
////                        mc.thePlayer.offGroundTicks <= 8)) {
////                    MovementUtil.customStrafeStrength(30);
////                }
//                switch (mc.thePlayer.offGroundTicks) {
//                    case 1:
//                        MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.349f));
//                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
//                            MovementUtil.strafe(0.37f);
//                        }
//                    case 8: {
//                        mc.thePlayer.motionY += 0.015f;
//                        break;
//                    }
//                    case 3: {
//                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.0045;
//                        break;
//                    }
//                    case 4: {
//                        mc.thePlayer.motionY = mc.thePlayer.motionY - 0.186;
//                        break;
//                    }
//                    case 5: {
//                     //   mc.thePlayer.motionY = mc.thePlayer.motionY - 0.042;
//                        break;
//                    }
//                    case 6: {
//                        mc.thePlayer.motionY += 0.015f;
//                        break;
//                    }
//                    case 7: {
//                        //mc.thePlayer.motionY -= 0.004;
//                        mc.thePlayer.motionY += 0.015;
//                        break;
//                    }
//                }
//            }
//        }
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
