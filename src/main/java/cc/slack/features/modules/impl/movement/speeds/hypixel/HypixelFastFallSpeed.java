// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.hypixel;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;


public class HypixelFastFallSpeed implements ISpeed {

    boolean waitingToDisable;
    boolean wasSlow = false;
    float strafeYaw;

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
                MovementUtil.strafe((float) (0.57f + Math.random() * 0.01f));
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
                        mc.thePlayer.motionY += 0.05700000002980232;
                        break;
                    case 3:
                        mc.thePlayer.motionY -= 0.1309;
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.2;
                        break;
                }
            }
            if (Slack.getInstance().getModuleManager().getInstance(Speed.class).hypixelSemiStrafe.getValue()) {
                if (mc.thePlayer.offGroundTicks == 5) {
                    if (Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw))) > 80) {
                        MovementUtil.strafe(0.12f);
                        strafeYaw = MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw);
                        wasSlow = true;
                    }
                }
                if (mc.thePlayer.offGroundTicks == 6) {
                    if (wasSlow) {
                        MovementUtil.strafe(0.12f, strafeYaw);
                    }
                }
                if (mc.thePlayer.offGroundTicks == 7) {
                    if (wasSlow) {
                        MovementUtil.strafe(0.25f, strafeYaw);
                        wasSlow = false;
                    }
                }
            }
        }

    }

    @Override
    public void onDisable() {
        if (mc.thePlayer.onGround || mc.thePlayer.offGroundTicks > 9) {
            waitingToDisable = false;
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
