// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.PostStrafeEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;


public class HypixelFastFallSpeed implements ISpeed {

    boolean waitingToDisable;
    boolean wasSlow = false;
    float strafeYaw;
    double jumpY;

    @Override
    public void onPostStrafe(PostStrafeEvent event) {
        if (Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - MovementUtil.getPlayerBindsDirection())) > 110) {
            MovementUtil.strafe(MovementUtil.getSpeed(), RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - 180);
        }
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (waitingToDisable && mc.thePlayer.offGroundTicks > 10) {
            Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
            return;
        }
        if (mc.thePlayer.onGround) {
            wasSlow = false;
            if (waitingToDisable) {
                Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
                return;
            }
            if (MovementUtil.isMoving()) {
                MovementUtil.strafe((float) (0.56f + Math.random() * 0.03f));
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    MovementUtil.strafe(0.58f + 0.024f * (amplifier + 1));
                }
                jumpY = mc.thePlayer.posY;
            }
        } else {
            mc.thePlayer.motionZ *= 1.0008;
            mc.thePlayer.motionX *= 1.0008;
            if (MovementUtil.getSpeed() < 0.12f) {
                MovementUtil.strafe(0.12f);
            }
            if (jumpY % 1 == 0 && !mc.thePlayer.isCollided && Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled && mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 20 + mc.thePlayer.offGroundTicks) {
                switch (mc.thePlayer.offGroundTicks) {
                    case 1:
                        MovementUtil.strafe(Math.max(MovementUtil.getSpeed(), 0.34f));
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtil.strafe(0.36f);
                        }
                        mc.thePlayer.motionY += 0.05700000002980232;
                        break;
                    case 3:
                        mc.thePlayer.motionY -= 0.1309;
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.2;
                        break;
                    case 6:
                        if (Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelSemiStrafe.getValue()) {
                            if (!PlayerUtil.isOverAir(mc.thePlayer.posX, mc.thePlayer.posY + 1 + mc.thePlayer.motionY * 3, mc.thePlayer.posZ)) {
                                mc.thePlayer.motionY += 0.075;
                                MovementUtil.strafe();
                            }
                        }
                        break;
                }
            }
        }

    }

    @Override
    public void onDisable() {
        if (mc.thePlayer.onGround || mc.thePlayer.offGroundTicks > 9) {
            waitingToDisable = false;
            mc.thePlayer.motionZ *= 0.4;
            mc.thePlayer.motionX *= 0.4;
        } else {
            waitingToDisable = true;
            Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
        }
    }

    @Override
    public String toString() {
        return "Hypixel";
    }
}
