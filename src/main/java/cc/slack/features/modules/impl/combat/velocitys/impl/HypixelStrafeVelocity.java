package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class HypixelStrafeVelocity implements IVelocity {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.hurtTime == 9) {
            MovementUtil.strafe(MovementUtil.getSpeed() * 0.9f);
        }

        if (MovementUtil.isBindsMoving() && mc.thePlayer.hurtTime > 3)
            if (Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw))) > 90) {
                MovementUtil.strafe(MovementUtil.getSpeed(), RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ))[0] - 180);
            }
    }

    @Override
    public String toString() {
        return "Hypixel Damage Strafe";
    }
}
