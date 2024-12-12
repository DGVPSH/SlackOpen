// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.world;

import cc.slack.events.impl.player.JumpEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.start.Slack;
import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.movement.Speed;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.other.BlockUtils;
import cc.slack.utils.player.*;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.round;


@ModuleInfo(
        name = "Scaffold",
        category = Category.WORLD
)
public class Scaffold extends Module {

    private final ModeValue<String> rotationMode = new ModeValue<>("Rotation Mode", new String[] {"Vanilla", "Vanilla Center", "Hypixel", "Hypixel Ground", "Vulcan", "FastBridge", "Custom"});
    private final NumberValue<Double> customYaw = new NumberValue<>("Custom Yaw", 180.0, -180.0, 180.0, 0.1);
    private final NumberValue<Double> customPitch = new NumberValue<>("Custom Pitch", 87.5, -90.0, 90.0, 0.05);

    private final NumberValue<Integer> keepRotationTicks = new NumberValue<>("Keep Rotation Length", 1, 0, 10, 1);
    private final ModeValue<String> swingMode = new ModeValue<>("Swing", new String[]{"Normal", "Packet", "None"});

    private final ModeValue<String> raycastMode = new ModeValue<>("Placement Check", new String[] {"Off", "Normal", "Strict"});
    private final ModeValue<String> placeTiming = new ModeValue<>("Placement Timing", new String[] {"Legit", "Pre", "Post"});
    private final NumberValue<Integer> searchDistance = new NumberValue<>("Search Distance", 1, 0, 6, 1);
    private final NumberValue<Double> expandAmount = new NumberValue<>("Expand Amount", 0.0, -1.0, 6.0, 0.1);
    private final NumberValue<Double> towerExpandAmount = new NumberValue<>("Tower Expand Amount", 0.0, -1.0, 6.0, 0.1);


    private final ModeValue<String> sprintMode = new ModeValue<>("Sprint Mode", new String[] {"Always", "No Packet", "Hypixel Safe", "Hypixel Jump", "Hypixel", "Off"});
    private final BooleanValue lowhop = new BooleanValue("Hypixel Jump Lowhop", true);
    private final ModeValue<String> sameY = new ModeValue<>("Same Y", new String[] {"Off", "Only Speed", "Always", "Hypixel Jump", "Hypixel Limit", "Auto Jump"});
    private final NumberValue<Double> speedModifier = new NumberValue<>("Speed Modifier", 1.0, 0.0, 2.0, 0.01);

    private final ModeValue<String> safewalkMode = new ModeValue<>("Safewalk", new String[] {"Ground", "Always", "Sneak", "Off"});

    private final NumberValue<Float> timerSpeed = new NumberValue<>("Timer", 1f, 0.2f, 2f, 0.05f);

    private final BooleanValue strafeFix = new BooleanValue("Movement Correction", false);

    private final ModeValue<String> towerMode = new ModeValue<>("Tower Mode", new String[] {"Off", "Vanilla", "Vulcan", "Watchdog", "Watchdog2", "Static", "Hypixel", "Motion"});
    private final BooleanValue towerNoMove = new BooleanValue("Tower No Move", false);

    private final ModeValue<String> pickMode = new ModeValue<>("Block Pick Mode", new String[] {"Biggest Stack", "First Stack"});
    private final BooleanValue spoofSlot = new BooleanValue("Spoof Item Slot", false);

    // Display
    private final ModeValue<String> displayMode = new ModeValue<>("Display", new String[]{"Advanced","Simple", "Off"});

    double groundY;
    double placeX;
    double placeY;
    double placeZ;

    boolean isTowering = false;
    boolean canTower = false;

    double startExpand = 0.0;
    double endExpand = 0.0;

    boolean hasBlock = false;
    float[] blockRotation = new float[] {0f, 0f};
    BlockPos blockPlace = new BlockPos(0,0,0);
    BlockPos blockPlacement = new BlockPos(0,0,0);
    EnumFacing blockPlacementFace = EnumFacing.DOWN;

    double jumpGround = 0.0;
    int jumpCounter = 0;
    boolean firstJump = false;

    int towerTicks = 0;


    boolean hasPlaced = false;


    boolean wasTimer = false;

    boolean blinkNSpoof = false;

    public Scaffold() {
        super();
        addSettings(rotationMode, customYaw, customPitch, keepRotationTicks, // rotations
                swingMode, // Swing Method
                raycastMode, placeTiming, searchDistance, expandAmount, towerExpandAmount, // placements
                sprintMode, lowhop, sameY, speedModifier, timerSpeed, safewalkMode, strafeFix, // movements
                towerMode, towerNoMove, // tower
                pickMode, spoofSlot, displayMode // slots
        );
    }

    @Override
    public void onEnable() {
        firstJump = true;
        groundY = mc.thePlayer.posY;
        blinkNSpoof = false;

    }

    @Override
    public void onDisable() {
        ItemSpoofUtil.stopSpoofing();
        RotationUtil.disable();
        if (wasTimer) mc.timer.timerSpeed = 1f;
        if (blinkNSpoof) {
            BlinkUtil.disable();
            blinkNSpoof = false;
        }
    }

    @Listen
    public void onMotion(MotionEvent event) {
        if (event.getState() == State.PRE) {
            if (placeTiming.getValue() == "Pre") placeBlock();
        } else {
            if (placeTiming.getValue() == "Post") placeBlock();
        }
    }

    @Listen
    public void onMove(MoveEvent event) {
        switch (safewalkMode.getValue().toLowerCase()) {
            case "ground":
                event.safewalk = event.safewalk || mc.thePlayer.onGround;
                break;
            case "always":
                event.safewalk = true;
                break;
            case "sneak":
                mc.gameSettings.keyBindSneak.pressed = PlayerUtil.isOverAir();
                break;
            default:
                break;

        }
    }

    @Listen
    public void onJump(JumpEvent event) {
        if (sprintMode.getValue().equalsIgnoreCase("hypixel jump")) {
            event.cancel();
        }
    }

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate(UpdateEvent event) {
        if (!pickBlock()) {
            RotationUtil.disable();
            return;
        }

        if (timerSpeed.getValue() != 1) {
            wasTimer = true;
            mc.timer.timerSpeed = timerSpeed.getValue();
        }

        if (mc.thePlayer.onGround && GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
            canTower = true;
        } else {
            if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
                canTower = false;
            }
        }

        setSprint();
        updateSameY();
        runFindBlock();
        updatePlayerRotations();
        setMovementCorrection();

        startExpand = Math.min(0, expandAmount.getValue());
        if (isTowering) startExpand = Math.min(0, towerExpandAmount.getValue());

        endExpand = Math.max(0, expandAmount.getValue());
        if (isTowering) endExpand = Math.max(0, towerExpandAmount.getValue());


        runTowerMove();
        if (placeTiming.getValue() == "Legit") placeBlock();
    }


    private boolean pickBlock() {
        int slot = InventoryUtil.pickHotarBlock(pickMode.getValue().equals("Biggest Stack"));
        if (slot != -1) {
            if (spoofSlot.getValue()) {
                ItemSpoofUtil.startSpoofing(slot);
            } else {
                mc.thePlayer.inventory.currentItem = slot;
            }
            return true;
        }
        ItemSpoofUtil.stopSpoofing();
        return false;
    }

    private void setSprint() {
        switch (sprintMode.getValue().toLowerCase()) {
            case "always":
            case "no packet":
                mc.thePlayer.setSprinting(true);
                break;
            case "hypixel safe":
                mc.thePlayer.setSprinting(false);
                if (mc.thePlayer.onGround && MovementUtil.isBindsMoving()) {
                    MovementUtil.strafe(0.215f);
                }
                break;
            case "hypixel jump":
                mc.thePlayer.setSprinting(true);
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                    hasPlaced = false;
                    if (!firstJump) {
                        MovementUtil.strafe((float) (0.61f + Math.random() * 0.01f));
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            float amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                            MovementUtil.strafe(0.61f + 0.024f * (amplifier + 1));
                        }
                    } else {
                        if (enabledTime.hasReached(1000)) {
                            MovementUtil.strafe(0.2f);
                            groundY = mc.thePlayer.posY;
                        }
                        jumpCounter = 1 ;
                    }
                    jumpCounter ++;
                } else if (lowhop.getValue() && MovementUtil.isMoving() && Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled && !isTowering) {
                    switch (mc.thePlayer.offGroundTicks) {
                        case 1:
                            MovementUtil.strafe(0.34f);
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
                    }
                }
                break;
            case "hypixel":
                mc.thePlayer.setSprinting(true);
                break;
            case "off":
                mc.thePlayer.setSprinting(false);
                break;
        }
    }

    private void setMovementCorrection() {
        if (strafeFix.getValue()) {
            RotationUtil.setStrafeFix(true, false);
        } else {
            RotationUtil.setStrafeFix(false, false);
        }
    }

    private void updatePlayerRotations() {



        switch (rotationMode.getValue().toLowerCase()) {
            case "hypixel ground":
            case "hypixel":

                if (towerMode.getValue().toLowerCase().contains("watchdog") && isTowering && !MovementUtil.isBindsMoving()) {
                            RotationUtil.overrideRotation(new float[] {MovementUtil.getDirection() + 120, 90f});
                    return;
                }
                if (Math.abs(MathHelper.wrapAngleTo180_double(BlockUtils.getFaceRotation(blockPlacementFace, blockPlace)[0] - MovementUtil.getDirection() - 102)) < Math.abs(MathHelper.wrapAngleTo180_double(BlockUtils.getFaceRotation(blockPlacementFace, blockPlace)[0] - MovementUtil.getDirection() + 102))) {
                    if (Math.round(mc.thePlayer.rotationYaw / 45) % 2 == 0) {
                        RotationUtil.setClientRotation(new float[]{(float) (MovementUtil.getDirection() + 102 + Math.random()), 87.5f}, keepRotationTicks.getValue());
                    } else {
                        RotationUtil.setClientRotation(new float[]{(float) (MovementUtil.getDirection() + 132 + Math.random()), 87.5f}, keepRotationTicks.getValue());
                    }
                } else {
                    if (Math.round(mc.thePlayer.rotationYaw / 45) % 2 == 0) {
                        RotationUtil.setClientRotation(new float[]{(float) (MovementUtil.getDirection() - 102 + Math.random()), 87.5f}, keepRotationTicks.getValue());
                    } else {
                        RotationUtil.setClientRotation(new float[]{(float) (MovementUtil.getDirection() - 132 + Math.random()), 87.5f}, keepRotationTicks.getValue());
                    }
                }
                break;

            case "vanilla":
                RotationUtil.setClientRotation(BlockUtils.getFaceRotation(blockPlacementFace, blockPlace), keepRotationTicks.getValue());
                break;
            case "vanilla center":
                RotationUtil.setClientRotation(BlockUtils.getCenterRotation(blockPlace), keepRotationTicks.getValue());
                break;
            case "vulcan":
                RotationUtil.setClientRotation(new float[] {MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) + 180, 77.5f}, keepRotationTicks.getValue());
                break;
            case "fastbridge":
                if (Math.round(mc.thePlayer.rotationYaw/45) % 2 == 0) {
                    RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw + 135, 77.5f}, keepRotationTicks.getValue());
                } else {
                    RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw + 180, 78f}, keepRotationTicks.getValue());
                }
                break;
            case "custom":
                RotationUtil.setClientRotation(new float[] {(float) (MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) + customYaw.getValue()), (float) (customPitch.getValue() + 0.0)}, keepRotationTicks.getValue());
                break;
        }

        BlockPos below = new BlockPos(mc.thePlayer.posX, placeY - 1, mc.thePlayer.posZ);
        if (!BlockUtils.isReplaceable(below)) {
            if (keepRotationTicks.getValue() == 0) {
                RotationUtil.disable();
            }
        }
    }

    private void updateSameY() {
        if (mc.thePlayer.onGround) {
            if (!sameY.getValue().equals("Hypixel Jump")) groundY = mc.thePlayer.posY;
        }
        switch (sameY.getValue().toLowerCase()) {
            case "off":
                placeY = mc.thePlayer.posY;
                break;
            case "only speed":
                if (!Slack.getInstance().getModuleManager().getInstance(Speed.class).isToggle()) {
                    placeY = mc.thePlayer.posY;
                } else {
                    placeY = groundY;
                }
                break;
            case "hypixel jump":
                if (mc.thePlayer.onGround && mc.thePlayer.posY - groundY != 1) groundY = mc.thePlayer.posY;
                if (Math.round(MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) / 45) * 45 % 90 == 0) {
                    endExpand = 0.3;
                } else {
                    endExpand = 0.4;
                }
                if ((PlayerUtil.isOverAir() && mc.thePlayer.motionY < -0 && mc.thePlayer.posY - groundY < 1.7 &&  mc.thePlayer.posY - groundY > 0.7) || firstJump) {
                    firstJump = false;
                    endExpand += 0.07;
                    placeY = mc.thePlayer.posY;
                } else {
                    placeY = groundY;
                }
                break;
            case "hypixel limit":
                if (mc.thePlayer.ticksExisted % 30 < 3) {
                    placeY = mc.thePlayer.posY + 1;
                    startExpand = -1.2;
                } else {
                    placeY = mc.thePlayer.posY;
                }
                break;
            case "auto jump":
                if (mc.thePlayer.onGround) mc.thePlayer.jump();
                placeY = groundY;
                break;
            case "always":
                placeY = groundY;
                break;

        }
        if (isTowering) {
            placeY = mc.thePlayer.posY;
            groundY = mc.thePlayer.posY;
        }
    }

    private void runTowerMove() {
        isTowering = false;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && !(towerNoMove.getValue() && MovementUtil.isMoving()) && mc.getCurrentScreen() == null && canTower) {
            isTowering = true;
            switch (towerMode.getValue().toLowerCase()) {
                case "static":
                    mc.thePlayer.motionY = 0.42;
                    break;
                case "vanilla":
                    if (mc.thePlayer.onGround) {
                        jumpGround = mc.thePlayer.posY;
                        mc.thePlayer.motionY = 0.41985 + Math.random() * 0.000095;
                    }

                    switch (mc.thePlayer.offGroundTicks % 3) {
                        case 0:
                            mc.thePlayer.motionY = 0.41985 + Math.random() * 0.000095;
                            break;
                        case 2:
                            mc.thePlayer.motionY = Math.ceil(mc.thePlayer.posY) - mc.thePlayer.posY;
                            break;
                    }
                    break;
                case "motion":
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                        MovementUtil.move(0.18f);
                    }
                    if (mc.thePlayer.offGroundTicks == 5 && mc.thePlayer.hurtTime < 5) {
                        mc.thePlayer.motionY = -0.1523351824467155;
                    }
                    break;
                case "vulcan":
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                    }

                    if (!MovementUtil.isMoving()) {
                        if (mc.thePlayer.ticksExisted % 3 == 0) {
                            endExpand = 2;
                        }
                    }
                    switch (mc.thePlayer.offGroundTicks % 3) {
                        case 0:
                            mc.thePlayer.motionY = 0.41985 + Math.random() * 0.000095;
                            break;
                        case 2:
                            mc.thePlayer.motionY = Math.ceil(mc.thePlayer.posY) - mc.thePlayer.posY;
                            break;
                    }
                    break;
                case "watchdog":
                    if (MovementUtil.isBindsMoving()) {
                        break;
                    }
                    if (mc.thePlayer.onGround) {
                        jumpGround = mc.thePlayer.posY;
                        towerTicks++;
                        if (towerTicks > 10) {
                            towerTicks = 0;
                        } else if (towerTicks > 5) {
                            mc.thePlayer.motionY = 0;
                            break;
                        }
                        mc.thePlayer.motionY = 0.4197 + Math.random() * 0.000095;
                    }

                    if (towerTicks <= 5) {
                        switch (mc.thePlayer.offGroundTicks % 3) {
                            case 0:
                                mc.thePlayer.motionY = 0.419 + Math.random() * 0.000095;
                                break;
                            case 1:
                                mc.thePlayer.motionY = 0.3328 + Math.random() * 0.000095;
                                //MovementUtil.spoofNextC03(true);
                                break;
                            case 2:
                                mc.thePlayer.motionY = Math.ceil(mc.thePlayer.posY) - mc.thePlayer.posY;
                                MovementUtil.spoofNextC03(true);
                                break;
                        }
                    }
                    MovementUtil.resetMotion();
                    if (mc.thePlayer.getHorizontalFacing() == EnumFacing.EAST || mc.thePlayer.getHorizontalFacing() == EnumFacing.WEST) {
                        mc.thePlayer.motionX = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posX) - mc.thePlayer.posX));
                    } else {
                        mc.thePlayer.motionZ = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posZ)- mc.thePlayer.posZ));
                    }
                    startExpand = -0.2;
                    endExpand = 0.2;
                    break;
                case "watchdog2":
                    if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled && mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 20) {
                        if (MovementUtil.isBindsMoving()) {
                            if (mc.thePlayer.onGround) {
                                jumpGround = mc.thePlayer.posY;
                                mc.thePlayer.motionY = 0.41999998688697815;
                            } else {

                                switch (mc.thePlayer.offGroundTicks % 3) {
                                    case 0:
                                        MovementUtil.strafe();
                                        mc.thePlayer.motionZ *= 1.03;
                                        mc.thePlayer.motionX *= 1.03;
                                        mc.thePlayer.motionY =  0.41999998688697815;
                                        break;
                                    case 1:
                                        MovementUtil.strafe();
                                        mc.thePlayer.motionY = 0.33;
                                        //MovementUtil.spoofNextC03(true);
                                        break;
                                    case 2:
                                        mc.thePlayer.motionY = Math.ceil(mc.thePlayer.posY) - mc.thePlayer.posY;
                                        break;
                                }
                            }
                        } else {
                            if (mc.thePlayer.onGround) {
                                jumpGround = mc.thePlayer.posY;
                                towerTicks++;
                                if (towerTicks > 10) {
                                    towerTicks = 0;
                                } else if (towerTicks > 5) {
                                    mc.thePlayer.motionY = 0;
                                    break;
                                }
                                mc.thePlayer.motionY = 0.4197 + Math.random() * 0.000095;
                            }

                            if (towerTicks <= 5) {
                                switch (mc.thePlayer.offGroundTicks % 3) {
                                    case 0:
                                        mc.thePlayer.motionY = 0.419 + Math.random() * 0.000095;
                                        break;
                                    case 1:
                                        mc.thePlayer.motionY = 0.3328 + Math.random() * 0.000095;
                                        //MovementUtil.spoofNextC03(true);
                                        break;
                                    case 2:
                                        mc.thePlayer.motionY = Math.ceil(mc.thePlayer.posY) - mc.thePlayer.posY;
                                        MovementUtil.spoofNextC03(true);
                                        break;
                                }
                            }
                            MovementUtil.resetMotion();
                            if (mc.thePlayer.getHorizontalFacing() == EnumFacing.EAST || mc.thePlayer.getHorizontalFacing() == EnumFacing.WEST) {
                                mc.thePlayer.motionX = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posX) - mc.thePlayer.posX));
                            } else {
                                mc.thePlayer.motionZ = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posZ) - mc.thePlayer.posZ));
                            }
                            startExpand = -0.2;
                            endExpand = 0.2;
                        }
                    }
                    break;
                case "off":
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                        isTowering = false;
                    }
                    break;
                case "hypixel":
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
                        MovementUtil.move(0.18f);
                    }
                    final double[] motions = new double[]{
                            0.399999006,
                            0.3536000119,
                            0.2681280169,
                            0.1843654552,
                            -0.0807218421,
                            -0.3175074179,
                            -0.3145572677,
                            -0.3866661346};
                    if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled && mc.thePlayer.ticksSinceLastDamage > mc.thePlayer.offGroundTicks && mc.thePlayer.ticksSinceLastTeleport > 20) {
                        if (mc.thePlayer.offGroundTicks < 8 && mc.thePlayer.hurtTime == 0)
                            mc.thePlayer.motionY = motions[mc.thePlayer.offGroundTicks];
                    }
                    break;

            }
        }
    }

    private void runFindBlock() {

        for (double x = startExpand; x <= endExpand; x += 0.1) {
            placeX = mc.thePlayer.posX - (MathHelper.sin((float) Math.toRadians(MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw))) * x);
            placeZ = mc.thePlayer.posZ + (MathHelper.cos((float) Math.toRadians(MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw))) * x);
            if (startSearch()) return;

        }
    }

    private boolean startSearch() {
        BlockPos below = new BlockPos(
                placeX,
                placeY - 1,
                placeZ);
        if(!BlockUtils.isReplaceable(below)) return false;

        List<BlockPos> searchQueue = new ArrayList<>();

        searchQueue.add(below.down());
        if (searchDistance.getValue() == 0) {
            searchQueue.add(blockPlace);
        } else {
            for (int x = -searchDistance.getValue(); x <= searchDistance.getValue(); x++) {
                for (int z = -searchDistance.getValue(); z <= searchDistance.getValue(); z++) {
                    searchQueue.add(below.add(x,0, z));
                }
            }
        }

        searchQueue.sort(Comparator.comparingDouble(BlockUtils::getScaffoldPriority));

        for (int i = 0; i < searchQueue.size(); i++)
        {
            if (searchBlock(searchQueue.get(i))) {
                hasBlock = true;
                return true;
            }
        }

        for (int i = 0; i < searchQueue.size(); i++)
        {
            if (searchBlock(searchQueue.get(i).down())) {
                hasBlock = true;
                return true;
            }
        }
        return false;
    }

    private boolean searchBlock(BlockPos block) {
        if (!BlockUtils.isReplaceable(block)) {
            EnumFacing placeFace = BlockUtils.getHorizontalFacingEnum(block, placeX, placeZ);
            if (block.getY() <= placeY - 2) {
                placeFace = EnumFacing.UP;
            }
            blockPlacement = block.add(placeFace.getDirectionVec());
            if (!BlockUtils.isReplaceable(blockPlacement)) {
                return false;
            }
            blockRotation = BlockUtils.getFaceRotation(placeFace, block);
            blockPlace = block;
            blockPlacementFace = placeFace;
            return true;
        } else {
            return false;
        }
    }

    private void placeBlock() {
        if (!hasBlock) return;
        boolean canContinue = true;
        MovingObjectPosition raytraced = mc.theWorld.rayTraceBlocks(
                mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks),
                mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks).add(mc.thePlayer.getVectorForRotation(RotationUtil.clientRotation[1], RotationUtil.clientRotation[0]).multiply(4)),
                false, true, false);
        switch (raycastMode.getValue().toLowerCase()) {
            case "normal":
                if (raytraced == null) {
                    canContinue = false;
                    break;
                }

                if (raytraced.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                    canContinue = false;
                } else {
                    canContinue = raytraced.getBlockPos().getX() == blockPlace.getX()
                    && raytraced.getBlockPos().getY() == blockPlace.getY()
                    && raytraced.getBlockPos().getZ() == blockPlace.getZ();
                }
                break;
            case "strict":
                if (raytraced == null) {
                    canContinue = false;
                    break;
                }
                if (raytraced.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    canContinue = raytraced.getBlockPos().getX() == blockPlace.getX()
                            && raytraced.getBlockPos().getY() == blockPlace.getY()
                            && raytraced.getBlockPos().getZ() == blockPlace.getZ() && raytraced.sideHit == blockPlacementFace;
                }
                break;
            default:
                break;
        }
        if (!canContinue) return;

        Vec3 hitVec = (new Vec3(blockPlacementFace.getDirectionVec())).multiply(0.5).add(new Vec3(0.5, 0.5, 0.5)).add(blockPlace);

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), blockPlace, blockPlacementFace, hitVec)) {

            if (swingMode.getValue().contains("Normal")) {
                mc.thePlayer.swingItem();
            } else if (swingMode.getValue().contains("Packet")) {
                PacketUtil.sendNoEvent(new C0APacketAnimation());
            }

            if (!sprintMode.getValue().equalsIgnoreCase("hypixel") || mc.thePlayer.onGround) {
                mc.thePlayer.motionX *= speedModifier.getValue();
                mc.thePlayer.motionZ *= speedModifier.getValue();
            }
            hasBlock = false;


        }
    }

    @Override
    public String getMode() {
        switch (displayMode.getValue()) {
            case "Advanced":
                return rotationMode.getValue() + ", " + sprintMode.getValue() + ", " + sameY.getValue() + ", " + safewalkMode.getValue();
            case "Simple":
                return rotationMode.getValue().toString();
        }
        return null;
    }
}
