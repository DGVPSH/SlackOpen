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


public class HypixelFastFallSpeed implements ISpeed {

    boolean waitingToDisable;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            if (waitingToDisable) {
                Slack.getInstance().getModuleManager().getInstance(Speed.class).toggle();
                return;
            }
            if (!Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle())
                RotationUtil.overrideRotation(new float[]{MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw), RotationUtil.clientRotation[1]});
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
        return "Hypixel";
    }
}
