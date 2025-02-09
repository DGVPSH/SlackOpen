// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.combat;

import cc.slack.events.State;
import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.impl.player.Blink;
import cc.slack.start.Slack;
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
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.network.PingSpoofUtil;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.render.RenderUtil;
import cc.slack.utils.rotations.RaycastUtil;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.rotations.RotationUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;

import java.awt.*;
import java.security.SecureRandom;

@ModuleInfo(
        name = "KillAura",
        category = Category.COMBAT
)
public class KillAura extends Module {

    // range
    private final NumberValue<Double> aimRange = new NumberValue<>("Aim Range", 3.1D, 3.0D, 7.0D, 0.01D);
    public final NumberValue<Double> attackRange = new NumberValue<>("Attack Range", 3.0D, 3.0D, 6.0D, 0.01D);
    private final ModeValue<String> attackTiming = new ModeValue<>("Attack Timing", new String[]{"Update", "Pre", "Strafe", "TickPost"});
    // attack
    private final ModeValue<String> swingMode = new ModeValue<>("Swing", new String[]{"Normal", "Packet", "None"});
    private final ModeValue<AttackUtil.AttackPattern> attackPattern = new ModeValue<>("Pattern", AttackUtil.AttackPattern.values());
    private final NumberValue<Integer> cps = new NumberValue<>("CPS", 14, 1, 30, 1);
    private final NumberValue<Double> randomization = new NumberValue<>("Randomization", 1.50D, 0D, 4D, 0.01D);

    // autoblock
    private final ModeValue<String> autoBlock = new ModeValue<>("Autoblock", new String[]{"None", "Fake", "Blatant", "Vanilla", "Interact", "Blink", "Switch", "Hypixel", "Vanilla Reblock", "Legit", "Test", "Hypixel2", "HypixelInv", "Basic", "Basic2", "Test2", "TimerSwap", "TimerInteract", "Hypixel3"});
    private final ModeValue<String> blinkMode = new ModeValue<>("Blink Autoblock Mode", new String[]{"Legit", "Legit HVH", "Blatant", "Blatant Switch", "Legit Switch"});
    private final NumberValue<Double> blockRange = new NumberValue<>("Block Range", 3.0D, 0.0D, 6.0D, 0.01D);
    private final BooleanValue interactAutoblock = new BooleanValue("Interact", false);
    private final BooleanValue renderBlocking = new BooleanValue("Render Blocking", true);

    // rotation
    private final ModeValue<RotationUtil.TargetRotation> rotationMode = new ModeValue<>("Rotation Mode", RotationUtil.TargetRotation.values());
    private final BooleanValue rotationRand = new BooleanValue("Rotation Randomization", false);
    private final NumberValue<Double> minRotationSpeed = new NumberValue<>("Min Rotation Speed", 65.0, 0.0, 180.0, 5.0);
    private final NumberValue<Double> maxRotationSpeed = new NumberValue<>("Max Rotation Speed", 85.0, 0.0, 180.0, 5.0);
    private final BooleanValue checkHitable = new BooleanValue("Check Hittable", false);


    // tools
    private final ModeValue<String> moveFix = new ModeValue<>("Move Fix", new String[]{"Silent", "Strict", "None"});
    private final BooleanValue keepSprint = new BooleanValue("Keep Sprint", true);
    private final BooleanValue rayCast = new BooleanValue("Ray Cast", true);

    // Checks
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
    private EntityLivingBase rayCastedEntity;
    private float[] rotations;
    private long attackDelay;
    private int queuedAttacks;
    public boolean isBlocking;
    private boolean wasBlink;
    private boolean inInv = false;

    private boolean hasDouble = false;
    private int currentSlot = 0;
    private int sword1 = 0;
    private int sword2 = 0;

    boolean wasTimer = false;

    public boolean renderBlock;

    public boolean hypixel20 = false;

    private boolean hypixel3 = false;

    public KillAura() {
        super();
        addSettings(
                aimRange, attackRange, attackTiming, // range
                swingMode, moveFix, attackPattern, cps, randomization, // Issues
                autoBlock, blinkMode, blockRange, interactAutoblock, renderBlocking, // autoblock
                rotationMode, rotationRand, minRotationSpeed, maxRotationSpeed, checkHitable, // rotations
                keepSprint, rayCast, // fixes
                noScaffold, noFlight, noEat, noBlock, // Checks
                sortMode,
                markMode,
                displayMode);
    }

    @Override
    public void onEnable() {
        wasBlink = false;
        rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        attackDelay = AttackUtil.getAttackDelay(cps.getValue(), randomization.getValue(), attackPattern.getValue());
        queuedAttacks = 0;
        timer.reset();
        rotationCenter.reset();
        currentSlot = mc.thePlayer.inventory.currentItem;
        if (mc.currentScreen instanceof GuiInventory) inInv = true;
    }

    @Override
    public void onDisable() {
        target = null;
        if(isBlocking) unblock();
        if(wasBlink) {
            BlinkUtil.disable();
            PingSpoofUtil.disable();
        }
        if (inInv) PacketUtil.send(new C0DPacketCloseWindow());
        if (currentSlot != mc.thePlayer.inventory.currentItem) {
            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            currentSlot = mc.thePlayer.inventory.currentItem;

        }
        if (wasTimer) mc.timer.timerSpeed = 1f;
        PingSpoofUtil.disable(false, true);
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
        if (attackTiming.getValue() == "Update") {
            if (wasTimer) mc.timer.timerSpeed = 1f;
            auraLoop();
        }
    }

    @Listen
    public void onTick(TickEvent e) {
        if (attackTiming.getValue() == "TickPost" && e.getState() == State.POST) {
            if (wasTimer) mc.timer.timerSpeed = 1f;
            auraLoop();
        }
    }

    @Listen
    public void onMotion(MotionEvent e) {
        if (e.getState() == State.POST) {

            return;
        }
        if (attackTiming.getValue() == "Pre") {
            if (wasTimer) mc.timer.timerSpeed = 1f;
            hypixel3 = false;
            auraLoop();
        }
    }

    @Listen
    public void onStrafe(StrafeEvent e) {
        if (attackTiming.getValue() == "Strafe") {
            if (wasTimer) mc.timer.timerSpeed = 1f;
            auraLoop();
        }
    }

    private void auraLoop() {
        if (noBlock.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().item instanceof ItemBlock) return;
        if (noScaffold.getValue() && Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) return;
        if (noEat.getValue() && mc.thePlayer.isUsingItem() && (mc.thePlayer.getHeldItem().item instanceof ItemFood || mc.thePlayer.getHeldItem().item instanceof ItemBucketMilk || mc.thePlayer.isUsingItem() && (mc.thePlayer.getHeldItem().item instanceof ItemPotion))) return;
        if (noFlight.getValue() && Slack.getInstance().getModuleManager().getInstance(Flight.class).isToggle()) return;

        target = AttackUtil.getTarget(aimRange.getValue(), sortMode.getValue());

        if (target == null) {
            attackDelay = 0;
            if (isBlocking)
                unblock();
            if (wasBlink) {
                wasBlink = false;
                BlinkUtil.disable();
            }
            renderBlock = false;
            rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
            return;
        }

        if (mc.thePlayer.getDistanceToEntity(target) > aimRange.getValue()) return;
        renderBlock = canAutoBlock() && (renderBlocking.getValue() || isBlocking || autoBlock.getValue().equals("Fake")) && (!autoBlock.getValue().equals("None"));

        rotations = calculateRotations(target);

        RotationUtil.setClientRotation(rotations, 1);

        switch (moveFix.getValue()) {
            case "None":
                RotationUtil.setStrafeFix(false, false);
                break;
            case "Silent":
                RotationUtil.setStrafeFix(true, false);
                break;
            case "Strict":
                RotationUtil.setStrafeFix(true, true);
                break;
        }

        sword1 = -1;
        sword2 = -1;
        for (int i = 36; i < 45; i++) {
            if (InventoryUtil.getSlot(i).getHasStack()) {
                ItemStack itemStack = InventoryUtil.getSlot(i).getStack();
                if (itemStack.item instanceof ItemSword) {
                    if (sword1 == -1) {
                        sword1 = i - 36;
                    } else {
                        sword2 = i - 36;
                    }
                }
            }
        }

        hasDouble = sword2 != -1;

        if (preTickBlock()) return;

        if (queuedAttacks == 0) return;

        if (isBlocking)
            if (preAttack()) return;

        while (queuedAttacks > 0) {
            attack(target);
            queuedAttacks--;
        }
        if (canAutoBlock()) postAttack();
    }

    private void attack(EntityLivingBase target) {
        rayCastedEntity = null;
        if (rayCast.getValue()) rayCastedEntity = RaycastUtil.rayCast(attackRange.getValue(), rotations);

        if (!ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_9)) {
            if (swingMode.getValue().contains("Normal")){
                mc.thePlayer.swingItem();
            } else if (swingMode.getValue().contains("Packet")) {
                PacketUtil.sendNoEvent(new C0APacketAnimation());
            }
        }

        // 1.8 you swing before you attack

        if (mc.thePlayer.getDistanceToEntity(rayCastedEntity == null ? target : rayCastedEntity) > attackRange.getValue() + 0.3)
            return;

        if (checkHitable.getValue() && !RaycastUtil.itHitable(attackRange.getValue() + 0.5, rotations, target)) return;

        if (keepSprint.getValue()) {
            mc.playerController.syncCurrentPlayItem();
            PacketUtil.send(new C02PacketUseEntity(rayCastedEntity == null ? target : rayCastedEntity, C02PacketUseEntity.Action.ATTACK));
            if (mc.thePlayer.fallDistance > 0 && !mc.thePlayer.onGround) {
                mc.thePlayer.onCriticalHit(rayCastedEntity == null ? target : rayCastedEntity);
            }
        } else {
            mc.playerController.attackEntity(mc.thePlayer, rayCastedEntity == null ? target : rayCastedEntity);
        }

        if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_9)) {
            if (swingMode.getValue().contains("Normal")){
                mc.thePlayer.swingItem();
            } else if (swingMode.getValue().contains("Packet")) {
                PacketUtil.sendNoEvent(new C0APacketAnimation());
            }
        }

        AttackUtil.inCombat = true;
        AttackUtil.combatTarget = rayCastedEntity == null ? target : rayCastedEntity;
        AttackUtil.combatTimer.reset();
    }

    private boolean preTickBlock() {
        switch (autoBlock.getValue().toLowerCase()) {
            case "double sword":
                if (!hasDouble && currentSlot != mc.thePlayer.inventory.currentItem && !isBlocking && target == null) {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    currentSlot = mc.thePlayer.inventory.currentItem;
                }
                if (hasDouble && currentSlot != mc.thePlayer.inventory.currentItem) {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    currentSlot = mc.thePlayer.inventory.currentItem;
                }
                break;
            case "test":
                switch (mc.thePlayer.ticksExisted % 2) {
                    case 0:
                        if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                            PingSpoofUtil.enableOutbound(true, (int) (25 + Math.random() * 5));
                            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                            isBlocking = false;
                        }
                        return true;
                    case 1:
                        if (currentSlot != mc.thePlayer.inventory.currentItem) {
                            PingSpoofUtil.enableOutbound(true, 10);
                            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            currentSlot = mc.thePlayer.inventory.currentItem;
                            isBlocking = false;
                        }
                        return false;
                }
                break;
            case "hypixel":
                switch (mc.thePlayer.ticksExisted % 3) {
                    case 0:
                        if (isBlocking) {
                            BlinkUtil.enable(false, true);
                            if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                                PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                            }
                            wasBlink = true;
                            isBlocking = false;
                            return true;
                        }
                    case 1:
                        if (currentSlot != mc.thePlayer.inventory.currentItem) {
                            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            currentSlot = mc.thePlayer.inventory.currentItem;
                            isBlocking = false;
                        }
                        return false;
                    case 2:
                        return true;
                }
                break;
            case "hypixel2":
                switch (mc.thePlayer.ticksExisted % 3) {
                    case 0:
                        return true;
                    case 1:
                        if (isBlocking) {
                            BlinkUtil.enable(false, true);
                            unblock();
                            wasBlink = true;
                            isBlocking = false;
                            return true;
                        }
                    case 2:
                        return false;
                }
                break;
            case "hypixel3":
                switch (mc.thePlayer.ticksExisted % 5) {
                    case 0:
                        return true;
                    case 1:
                        if (isBlocking) {
                            BlinkUtil.enable(false, true);
                            unblock();
                            wasBlink = true;
                            isBlocking = false;
                            return true;
                        }
                    case 2:
                        return false;
                    case 3:
                        if (isBlocking) {
                            BlinkUtil.enable(false, true);
                            unblock();
                            wasBlink = true;
                            isBlocking = false;
                            return true;
                        }
                    case 4:
                        return false;
                }
                break;
            case "legit":
                if (mc.thePlayer.hurtTime < 3) {
                    if (!isBlocking) {
                        block(false);
                    }
                } else {
                    if (isBlocking) {
                        unblock();
                        return true;
                    }
                }
                return isBlocking;
            case "basic":
                switch (mc.thePlayer.ticksExisted % 3) {
                    case 0:
                        unblock();
                        return true;
                    case 1:
                        return false;
                    case 2:
                        block(false);
                        return true;
                }
                break;
            case "basic2":
                switch (mc.thePlayer.ticksExisted % 3) {
                    case 0:
                        PingSpoofUtil.enableOutbound(true, 60);
                        wasBlink = true;
                        unblock();
                        return true;
                    case 1:
                        PingSpoofUtil.enableOutbound(true, 15);
                        return false;
                    case 2:
                        PingSpoofUtil.enableOutbound(true, 0);
                        return false;
                }
                break;
            case "basic3":
                switch (mc.thePlayer.ticksExisted % 3) {
                    case 0:
                        BlinkUtil.enable(false, true);
                        wasBlink = true;
                        PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                        return false;
                    case 1:
                        PacketUtil.send(new C0DPacketCloseWindow());
                        BlinkUtil.disable();
                        wasBlink = false;
                        return false;
                    case 2:
                        isBlocking = false;
                        block(false);
                        return true;
                }
                break;
            case "blink":
                switch (blinkMode.getValue().toLowerCase()) {
                    case "legit":
                        switch (mc.thePlayer.ticksExisted % 3) {
                            case 0:
                                unblock();
                                return true;
                            case 1:
                                return false;
                            case 2:
                                block();
                                if (!BlinkUtil.isEnabled)
                                    BlinkUtil.enable(true, true);
                                BlinkUtil.setConfig(true, true);
                                BlinkUtil.releasePackets();
                                wasBlink = true;
                                return true;
                        }
                        break;
                    case "legit hvh":
                        switch (mc.thePlayer.ticksExisted % 5) {
                            case 0:
                                unblock();
                                return true;
                            case 4:
                                block();
                                if (!BlinkUtil.isEnabled)
                                    BlinkUtil.enable(true, true);
                                BlinkUtil.setConfig(true, true);
                                BlinkUtil.releasePackets();
                                wasBlink = true;
                                return true;
                        }
                        break;
                    case "blatant":
                        switch (mc.thePlayer.ticksExisted % 2) {
                            case 0:
                                unblock();
                                return true;
                            case 1:
                                return false;
                        }
                        break;
                    case "blatant switch":
                        switch (mc.thePlayer.ticksExisted % 2) {
                            case 0:
                                if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                    currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                                }
                                return true;
                            case 1:
                                if (currentSlot != mc.thePlayer.inventory.currentItem) {
                                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                    currentSlot = mc.thePlayer.inventory.currentItem;
                                    isBlocking = false;
                                }
                                return false;
                        }
                        break;
                    case "legit switch":
                        switch (mc.thePlayer.ticksExisted % 3) {
                            case 0:
                                if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                    currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                                }
                                return true;
                            case 1:
                                if (currentSlot != mc.thePlayer.inventory.currentItem) {
                                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                    currentSlot = mc.thePlayer.inventory.currentItem;
                                    isBlocking = false;
                                }
                                return false;
                            case 2:
                                block();
                                if (!BlinkUtil.isEnabled)
                                    BlinkUtil.enable(true, true);
                                BlinkUtil.setConfig(true, true);
                                BlinkUtil.releasePackets();
                                wasBlink = true;
                                return true;
                        }
                        break;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private boolean preAttack() {
        switch (autoBlock.getValue().toLowerCase()) {
            case "double sword":
                if (hasDouble) {
                    if (currentSlot != sword1) {
                        PacketUtil.send(new C09PacketHeldItemChange(sword1));
                        currentSlot = sword1;
                    } else {
                        PacketUtil.send(new C09PacketHeldItemChange(sword2));
                        currentSlot = sword2;
                    }
                    isBlocking = false;
                }
                break;
            case "blatant":
                unblock();
                isBlocking = false;
                break;
            case "drop":
                if (isBlocking) {
                    PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                isBlocking = false;
                break;
            case "switch":
                if (isBlocking) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
                isBlocking = false;
                break;
            case "interact":
                if (isBlocking) {
                    unblock();
                    return true;
                }
                break;
            case "hypixel3":
            case "hypixel2":
                break;
            case "hypixelinv2":
                PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.OPEN_INVENTORY));
                break;
            case "hypixelinv":
                if (inInv) {
                    BlinkUtil.disable();
                    wasBlink = false;
                    PacketUtil.send(new C0DPacketCloseWindow());
                    inInv = false;
                } else {
                    BlinkUtil.enable(false, true);
                    PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    inInv = true;
                    wasBlink = true;
                    return true;
                }
                break;
            case "hypixel20":
                // true = c16 unblock, false = c09 unblock
                if (hypixel20) {
                    if (inInv)
                        PacketUtil.send(new C0DPacketCloseWindow());
                    inInv = false;
                } else if (currentSlot != mc.thePlayer.inventory.currentItem) {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    currentSlot = mc.thePlayer.inventory.currentItem;
                }
                break;
            case "test2":
                if (mc.thePlayer.hurtTime < 3 && mc.thePlayer.ticksSinceLastDamage < 15) {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                            currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                        }
                        PacketUtil.send(new C03PacketPlayer(mc.thePlayer.onGround));
                        wasTimer = true;
                        mc.timer.timerSpeed = 0.5f;
                        if (currentSlot != mc.thePlayer.inventory.currentItem) {
                            PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            currentSlot = mc.thePlayer.inventory.currentItem;
                            isBlocking = false;
                        }
                    } else {
                        unblock();
                        PacketUtil.send(new C03PacketPlayer(mc.thePlayer.onGround));
                        wasTimer = true;
                        mc.timer.timerSpeed = 0.5f;
                    }
                } else {
                    if (isBlocking) {
                        unblock();
                        return true;
                    }
                }
                break;
            case "timerinteract":
                unblock();
                PacketUtil.send(new C03PacketPlayer(mc.thePlayer.onGround));
                wasTimer = true;
                mc.timer.timerSpeed = 0.5f;
                break;
            case "timerswap":
                if (currentSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                }
                PacketUtil.send(new C03PacketPlayer(mc.thePlayer.onGround));
                wasTimer = true;
                mc.timer.timerSpeed = 0.5f;
                if (currentSlot != mc.thePlayer.inventory.currentItem) {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    currentSlot = mc.thePlayer.inventory.currentItem;
                    isBlocking = false;
                }

                break;
            default:
                break;
        }
        return false;
    }

    private void postAttack() {
        switch (autoBlock.getValue().toLowerCase()) {
            case "test2":
                if (mc.thePlayer.hurtTime < 3 && mc.thePlayer.ticksSinceLastDamage < 15) {
                    block(true);
                }
                break;
            case "hypixel3":
            case "hypixel":
            case "hypixel2":
                block(true);
                BlinkUtil.disable();
                break;
            case "interact":
                block(true);
                break;
            case "vanilla reblock":
                isBlocking = false;
                block();
                break;
            case "hypixelinv2":
                isBlocking = false;
                block(true);
                break;
            case "hypixelinv":
                isBlocking = false;
                block(true);
                BlinkUtil.disable();
                break;
            case "hypixel20":
                hypixel20 = !hypixel20;

                BlinkUtil.releasePackets();
                EntityLivingBase tEntity = rayCastedEntity == null ? target : rayCastedEntity;
                PacketUtil.sendNoEvent(new C02PacketUseEntity(tEntity, C02PacketUseEntity.Action.INTERACT));
                PacketUtil.sendBlocking(false, false);
                if (!wasBlink)
                    BlinkUtil.enable(false, true);
                wasBlink = true;

                // true = c16 unblock, false = c09 unblock
                if (hypixel20) {
                    PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    inInv = true;
                } else {
                    PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    currentSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                }
                break;

            case "test":
                block();
                break;
            case "double sword":
                if (hasDouble) {
                    EntityLivingBase targetEntity = rayCastedEntity == null ? target : rayCastedEntity;
                    if (interactAutoblock.getValue()) {
                        PacketUtil.send(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
                        // PacketUtil.send(new C02PacketUseEntity(targetEntity, new Vec3(0,0,0)));
                    }
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(InventoryUtil.getSlot(currentSlot).getStack()));
                    isBlocking = true;
                }
                break;
            case "drop":
            case "switch":
            case "blatant":
            case "vanilla":
            case "timerswap":
            case "timerinteract":
                block();
                break;
            case "blink":
                if (blinkMode.getValue().equals("Blatant")) {
                    block();
                    if (!BlinkUtil.isEnabled)
                        BlinkUtil.enable(true, true);
                    BlinkUtil.setConfig(true, true);
                    BlinkUtil.releasePackets();
                    wasBlink = true;
                } else if (blinkMode.getValue().equalsIgnoreCase("Blatant Switch")) {
                    block();
                    if (!BlinkUtil.isEnabled)
                        BlinkUtil.enable(false, true);
                    BlinkUtil.setConfig(false, true);
                    BlinkUtil.releasePackets();
                    wasBlink = true;
                }
                break;
            case "basic3":
            case "basic2":
                if (mc.thePlayer.ticksExisted % 3 == 2) {
                    block(true);
                }
                break;
            default:
                break;
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

    private void block() {
        block(interactAutoblock.getValue());
    }

    private void block(boolean interact) {
        if (isBlocking) return;
        EntityLivingBase targetEntity = rayCastedEntity == null ? target : rayCastedEntity;
        if (interact) {
            PacketUtil.send(new C02PacketUseEntity(targetEntity, C02PacketUseEntity.Action.INTERACT));
            // PacketUtil.send(new C02PacketUseEntity(targetEntity, new Vec3(0,0,0)));
        }
        PacketUtil.sendBlocking(true, false);
        isBlocking = true;
    }

    private void unblock() {
        if (!isBlocking) return;
        if (!mc.gameSettings.keyBindUseItem.isKeyDown())
            PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        else
            if (canAutoBlock()) mc.gameSettings.keyBindUseItem.setPressed(false);
        isBlocking = false;
    }

    private boolean canAutoBlock() {
        return target != null && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.getDistanceToEntity(target) < blockRange.getValue();
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
}
