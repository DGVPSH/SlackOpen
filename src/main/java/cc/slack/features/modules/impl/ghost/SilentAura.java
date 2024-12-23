// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.movement.Flight;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.network.PingSpoofUtil;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.render.FreeLookUtil;
import cc.slack.utils.render.RenderUtil;
import cc.slack.utils.rotations.RaycastUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.security.SecureRandom;

@ModuleInfo(
        name = "SilentAura",
        category = Category.GHOST
)
public class SilentAura extends Module {

    // range
    private final NumberValue<Double> aimRange = new NumberValue<>("Aim Range", 3.1D, 3.0D, 7.0D, 0.01D);
    public final NumberValue<Double> attackRange = new NumberValue<>("Attack Range", 3.0D, 3.0D, 6.0D, 0.01D);

    // attack
    private final ModeValue<AttackUtil.AttackPattern> attackPattern = new ModeValue<>("Pattern", AttackUtil.AttackPattern.values());
    private final NumberValue<Integer> cps = new NumberValue<>("CPS", 14, 1, 30, 1);
    private final NumberValue<Double> randomization = new NumberValue<>("Randomization", 1.50D, 0D, 4D, 0.01D);

    // autoblock
    private final ModeValue<String> autoBlock = new ModeValue<>("Autoblock", new String[]{"None", "Click"});
    private final NumberValue<Double> blockRange = new NumberValue<>("Block Range", 3.0D, 0.0D, 6.0D, 0.01D);
    private final BooleanValue smartAutoblock = new BooleanValue("Smart Autoblock", true);

    // rotation
    private final ModeValue<RotationUtil.TargetRotation> rotationMode = new ModeValue<>("Rotation Mode", RotationUtil.TargetRotation.values());
    private final BooleanValue rotationRand = new BooleanValue("Rotation Randomization", false);
    private final NumberValue<Double> minRotationSpeed = new NumberValue<>("Min Rotation Speed", 65.0, 0.0, 180.0, 5.0);
    private final NumberValue<Double> maxRotationSpeed = new NumberValue<>("Max Rotation Speed", 85.0, 0.0, 180.0, 5.0);

    // Checks
    private final BooleanValue fixMove = new BooleanValue("Fix Movement", true);
    private final BooleanValue noScaffold = new BooleanValue("Disable on Scaffold", false);
    private final BooleanValue noFlight = new BooleanValue("Disable on Flight", false);
    private final BooleanValue noEat = new BooleanValue("Disable on Eat", true);
    private final BooleanValue noBlock = new BooleanValue("Disable on Block", true);

    private final ModeValue<String> sortMode = new ModeValue<>("Sort", new String[]{"Priority", "FOV", "Distance", "Health", "Hurt Ticks"});
    private final ModeValue<String> markMode = new ModeValue<>("Killaura Mark Mode", new String[]{"None", "Tracer", "Slack"});

    // Display
    private final ModeValue<String> displayMode = new ModeValue<>("Display", new String[]{"Advanced", "Simple", "Autoblock", "Off"});


    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil rotationCenter = new TimeUtil();
    private double rotationOffset;
    public EntityLivingBase target;
    private float[] rotations;
    private long attackDelay;
    private int queuedAttacks;
    public boolean isBlocking;

    public SilentAura() {
        super();
        addSettings(
                aimRange, attackRange, // range
                attackPattern, cps, randomization, // Issues
                autoBlock, blockRange, smartAutoblock, // autoblock
                rotationMode, rotationRand, minRotationSpeed, maxRotationSpeed, // rotations
                fixMove, noScaffold, noFlight, noEat, noBlock, // Checks
                sortMode,
                markMode,
                displayMode);
    }

    @Override
    public void onEnable() {
        rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        attackDelay = AttackUtil.getAttackDelay(cps.getValue(), randomization.getValue(), attackPattern.getValue());
        queuedAttacks = 0;
        timer.reset();
        rotationCenter.reset();
    }

    @Override
    public void onDisable() {
        target = null;
        unrot();
        mc.gameSettings.keyBindUseItem.pressed = false;
    }

    @Listen
    public void onRender(RenderEvent e) {
        if(e.getState() != RenderEvent.State.RENDER_3D) return;
        if (timer.hasReached(attackDelay) && target != null) {
            queuedAttacks++;
            timer.reset();
            attackDelay = AttackUtil.getAttackDelay(cps.getValue(), randomization.getValue(), attackPattern.getValue());
        }

        if (target != null) {
            switch (markMode.getValue().toLowerCase()) {
                case "tracer":
                    RenderUtil.drawTracer(target, 250, 250, 250, 130);
                    break;
                case "slack":
                    RenderUtil.drawFilledAABB(target.getEntityBoundingBox().expand(0.1, 0.1, 0.1), (target.hurtTime > 1) ? new Color(255, 0, 0, 90).getRGB() : new Color(0, 255, 0, 90).getRGB());
                    break;
                default:
                    break;
            }
        }
    }

    @Listen
    public void onUpdate(UpdateEvent e) {

        if (noBlock.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().item instanceof ItemBlock) return;
        if (noScaffold.getValue() && Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) return;
        if (noEat.getValue() && mc.thePlayer.isUsingItem() && (mc.thePlayer.getHeldItem().item instanceof ItemFood || mc.thePlayer.getHeldItem().item instanceof ItemBucketMilk || mc.thePlayer.isUsingItem() && (mc.thePlayer.getHeldItem().item instanceof ItemPotion))) return;
        if (noFlight.getValue() && Slack.getInstance().getModuleManager().getInstance(Flight.class).isToggle()) return;

        target = AttackUtil.getTarget(aimRange.getValue(), sortMode.getValue());

        if (target == null) {
            attackDelay = 0;
            unrot();
            rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
            return;
        }

        if (mc.thePlayer.getDistanceToEntity(target) > aimRange.getValue()) {
            unrot();
            return;
        }

        if (!FreeLookUtil.freelooking) {
            FreeLookUtil.enable();
            FreeLookUtil.cameraYaw += 180;
        } else {
            if (fixMove.getValue()) {
                RotationUtil.strafeFixBinds(mc.thePlayer.rotationYaw - FreeLookUtil.cameraYaw + 180 - MovementUtil.getBindsDirection(0));
            }
        }

        rotations = calculateRotations(target);

        RotationUtil.setPlayerRotation(rotations);

        if (queuedAttacks == 0) return;

        while (queuedAttacks > 0) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            queuedAttacks--;
        }

        if (mc.thePlayer.getDistanceToEntity(target) < blockRange.getValue() && (!smartAutoblock.getValue() || mc.thePlayer.hurtTime < 3)) {
            switch (autoBlock.getValue().toLowerCase()) {
                case "none":
                    break;
                case "click":
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    break;
            }
        }

    }

    private float[] calculateRotations(Entity entity) {
        final AxisAlignedBB bb = entity.getEntityBoundingBox();

        if(rotationCenter.hasReached(1200) && rotationRand.getValue()) {
            rotationOffset = new SecureRandom().nextDouble() / 4;
            rotationCenter.reset();
        }

        float[] newRots = RotationUtil.getLimitedRotation(
                rotations,
                RotationUtil.getTargetRotations(bb, rotationMode.getValue(), rotationOffset),
                (float) MathUtil.getRandomInRange(minRotationSpeed.getValue(), maxRotationSpeed.getValue()));

        return RotationUtil.applyGCD(newRots, rotations);
    }

    @Override
    public String getMode() {
        switch (displayMode.getValue()) {
            case "Advanced":
                return cps.getValue() + " cps" + ", " + autoBlock.getValue() + ", " + sortMode.getValue();
            case "Simple":
                return sortMode.getValue();
            case "Autoblock":
                return autoBlock.getValue();
            case "None":
                return "";
        }
        return null;
    }

    private void unrot() {
        if (!FreeLookUtil.freelooking) return;
        mc.thePlayer.rotationYaw = FreeLookUtil.cameraYaw + 180;
        mc.thePlayer.rotationPitch = FreeLookUtil.cameraPitch;
        FreeLookUtil.setFreelooking(false);
        MovementUtil.updateBinds();
    }
}
