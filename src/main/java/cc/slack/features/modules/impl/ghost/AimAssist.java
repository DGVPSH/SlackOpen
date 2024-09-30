// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@ModuleInfo(
        name = "AimAssist",
        category = Category.GHOST
)
public class AimAssist extends Module {

    private final BooleanValue lowerSens = new BooleanValue("Lower Sensitivity On Target", true);
    private final NumberValue<Float> lowerSensAmount = new NumberValue<>("Lowered Sensitity Percentage", 0.6f, 0f, 1f, 0.1f);
    private final BooleanValue accelSens = new BooleanValue("Dynamic Acceleration", true);
    private final NumberValue<Float> accelAmount = new NumberValue<>("Acceleration Speed", 1.3f, 1f, 2f, 0.05f);
    private final BooleanValue insideNudge = new BooleanValue("Inside Nudge", true);
    private final BooleanValue aimbot = new BooleanValue("AimBot", false);
    private final NumberValue<Float> aimbotSpeed = new NumberValue<>("AimBot Speed", 20f, 0f, 90f, 2f);
    private final NumberValue<Float> aimbotFOV = new NumberValue<>("AimBot FOV", 80f, 0f, 180f, 5f);
    private final BooleanValue flick = new BooleanValue("Flick", true);
    private final NumberValue<Float> flickThreshold = new NumberValue<>("Flick Threshold", 120f, 50f, 180f, 5f);
    private final BooleanValue onMouse = new BooleanValue("Only Mouse Down", true);
    private final BooleanValue onlyCombat = new BooleanValue("Only Combat", true);

    
    

    private float prevDist, currDist;
    private float[] prevRot;
    private boolean wasAccel = false;

    private float sens;
    private float gameSens;
    
    public float yawNudge = 0f;
    public float pitchNudge = 0f;

    private float prevSpeed = 0f;

    public AimAssist() {
        addSettings(lowerSens, lowerSensAmount, accelSens, accelAmount, insideNudge, aimbot, aimbotSpeed, aimbotFOV, flick, flickThreshold, onMouse, onlyCombat);
    }

    @Listen
    public void onRender(RenderEvent event) {
        if (event.state != RenderEvent.State.RENDER_2D) return;

        if (onMouse.getValue() && !mc.gameSettings.keyBindAttack.isKeyDown()) return;
        if (onlyCombat.getValue() && !AttackUtil.inCombat) return;


        if (mc.objectMouseOver.entityHit != null) {
            if (insideNudge.getValue()) {
                float[] nudge = RotationUtil.getLimitedRotation(
                        RotationUtil.getPlayerRotation(),
                        RotationUtil.getTargetRotations(mc.objectMouseOver.entityHit.getEntityBoundingBox(), RotationUtil.TargetRotation.OPTIMAL, 0.01),
                        (float) ((float) 30 * RotationUtil.getRotationDifference(RotationUtil.getTargetRotations(mc.objectMouseOver.entityHit.getEntityBoundingBox(), RotationUtil.TargetRotation.MIDDLE, 0.01)) / 3 / Minecraft.getDebugFPS())
                );

                yawNudge = nudge[0] - RotationUtil.getPlayerRotation()[0];
                pitchNudge = nudge[1] - RotationUtil.getPlayerRotation()[1];
            }
        }

        if (mc.objectMouseOver.entityHit == null && aimbot.getValue()) {
            EntityLivingBase target = AttackUtil.getTarget(4.6, "FOV");
            if (target != null) {
                if (RotationUtil.getRotationDifference(RotationUtil.getTargetRotations(target.getEntityBoundingBox(), RotationUtil.TargetRotation.MIDDLE, 0.01)) < aimbotFOV.getValue()) {
                    prevSpeed = (2 * prevSpeed + (float) ((float) 20 * MathUtil.getRandomInRange(aimbotSpeed.getValue() * 0.9f, aimbotSpeed.getValue() * 1.1f) / Minecraft.getDebugFPS())) / 3;
                    float[] nudge = RotationUtil.getLimitedRotation(
                            RotationUtil.getPlayerRotation(),
                            RotationUtil.getTargetRotations(target.getEntityBoundingBox(), RotationUtil.TargetRotation.MIDDLE, 0.01),
                            prevSpeed
                    );

                    yawNudge = nudge[0] - RotationUtil.getPlayerRotation()[0];
                    pitchNudge = nudge[1] - RotationUtil.getPlayerRotation()[1];
                } else {
                    prevSpeed = 0f;
                }
            } else {
                prevSpeed = 0f;
            }
        } else {
            prevSpeed = 0f;
        }

        if (flick.getValue()) {
            EntityLivingBase target = AttackUtil.getTarget(4.6, "FOV");
            if (target != null) {
                if (RotationUtil.getRotationDifference(RotationUtil.getTargetRotations(target.getEntityBoundingBox(), RotationUtil.TargetRotation.CENTER, 0.01)) > flickThreshold.getValue()) {
                    float[] nudge = RotationUtil.getLimitedRotation(
                            RotationUtil.getPlayerRotation(),
                            RotationUtil.getTargetRotations(target.getEntityBoundingBox(), RotationUtil.TargetRotation.MIDDLE, 0.01),
                            (float) (flickThreshold.getValue() * MathUtil.getRandomInRange(0.9, 1.1))
                    );

                    yawNudge += nudge[0] - RotationUtil.getPlayerRotation()[0];
                    pitchNudge += nudge[1] - RotationUtil.getPlayerRotation()[1];
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate (UpdateEvent event) {
        gameSens = mc.gameSettings.mouseSensitivity;
        sens = gameSens;

        if (onMouse.getValue() && !mc.gameSettings.keyBindAttack.isKeyDown()) return;
        if (onlyCombat.getValue() && !AttackUtil.inCombat) return;

        if (mc.objectMouseOver.entityHit != null) {
            if (lowerSens.getValue()) {
                sens = gameSens * lowerSensAmount.getValue();
            }
        }
        if (accelSens.getValue()) {
            if (mc.objectMouseOver.entityHit == null) {
                EntityLivingBase target = AttackUtil.getTarget(4.6, "FOV");
                if (target != null) {
                    if (wasAccel) {
                        prevDist = currDist;
                        currDist = (float) RotationUtil.getRotationDifference((Entity) target);
                        if (RotationUtil.getRotationDifference(prevRot) * 0.6 < prevDist - currDist && currDist < 120) {
                            sens = gameSens * accelAmount.getValue();
                        } else {
                            sens = gameSens;
                        }

                        prevRot = new float[] {mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                    } else {
                        prevRot = new float[] {mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                        prevDist = (float) RotationUtil.getRotationDifference((Entity) target);
                        currDist = prevDist;
                        wasAccel = true;
                    }
                } else {
                    sens = gameSens;
                    wasAccel = false;
                }
            }
        }
    }

    public float getSens() {
        if (isToggle()) {
            return sens;
        } else {
            return mc.gameSettings.mouseSensitivity;
        }
    }
}
