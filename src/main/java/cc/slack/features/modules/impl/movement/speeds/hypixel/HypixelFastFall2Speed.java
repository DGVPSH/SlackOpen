// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.PostStrafeEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;


public class HypixelFastFall2Speed implements ISpeed {


    @Override
    public void onPostStrafe(PostStrafeEvent event) {
        if (Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - MovementUtil.getPlayerBindsDirection())) > 110) {
            MovementUtil.strafe(MovementUtil.getSpeed(), RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - 180);
        }
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe((float) (0.58f + Math.random() * 0.01f));
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.58f + 0.024f * (amplifier + 1));
                }
            }
        } else {
            if (MovementUtil.getSpeed() < 0.12f) {
                MovementUtil.strafe(0.12f);
            }
            if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled && mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 20) {
                switch (mc.thePlayer.offGroundTicks) {
                    case 1:
                        MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.34f));
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtil.strafe(0.37f);
                        }
                        break;
                    case 3:
                        mc.thePlayer.motionY -= -0.0025;
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.04;
                        break;
                    case 5:
                        mc.thePlayer.motionY -= 0.1905189780583944;
                        break;
                    case 6:
                        mc.thePlayer.motionX *= 1.001;
                        mc.thePlayer.motionZ *= 1.001;
                    case 7:
                        mc.thePlayer.motionY -= 0.004;
                        break;
                    case 8:
                        mc.thePlayer.motionY -= 0.01;
                }
            }
        }

    }

    @Override
    public String toString() {
        return "Hypixel2";
    }
}
