// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.world;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.features.modules.impl.render.Hud;
import cc.slack.start.Slack;
import cc.slack.events.State;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.ghost.AutoTool;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.other.BlockUtils;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector4d;
import java.awt.*;
import java.util.ArrayList;

@ModuleInfo(
        name = "Breaker",
        category = Category.WORLD
)
public class Breaker extends Module {
    public final ModeValue<String> mode = new ModeValue<>("Bypass", new String[]{"Hypixel", "None", "MMC"});
    public final NumberValue<Double> radiusDist = new NumberValue<>("Radius", 4.5, 1.0, 7.0, 0.5);
    public final ModeValue<String> sortMode = new ModeValue<>("Sort", new String[]{"Distance", "Absolute"});
    public final NumberValue<Integer> switchDelay = new NumberValue<>("Switch Delay", 50, 0, 500, 10);
    public final NumberValue<Integer> targetSwitchDelay = new NumberValue<>("Target Switch Delay", 50, 0, 500, 10);

    public final NumberValue<Double> breakPercent = new NumberValue<>("FastBreak Percent", 1.0, 0.0, 1.0, 0.05);
    public final BooleanValue spoofGround = new BooleanValue("Hypixel Faster", true);
    public final BooleanValue spoof = new BooleanValue("Hypixel Faster Spoof Ground", true);
    public final BooleanValue noCombat = new BooleanValue("No Combat", true);
    public final BooleanValue whitelist = new BooleanValue("Whitelist Own Bed", true);
    public final BooleanValue moveFix = new BooleanValue("Movement Fix", true);

    // Display
    private final ModeValue<String> displayMode = new ModeValue<>("Display", new String[]{"Simple", "Off"});

    public Breaker() {
        addSettings(mode, radiusDist, sortMode, switchDelay, targetSwitchDelay, breakPercent, spoofGround, spoof, noCombat, whitelist, moveFix, displayMode);
    }

    private BlockPos targetBlock;
    public BlockPos currentBlock;
    private EnumFacing currentFace;

    private float breakingProgress;
    private float fasterProgress;
    private boolean timer = false;

    private TimeUtil switchTimer = new TimeUtil();

    boolean waitingBed = false;
    BlockPos spawnPos = new BlockPos(0,0,0);
    BlockPos bedPos = new BlockPos(0,0,0);


    @Override
    public void onEnable() {
        targetBlock = null;
        currentBlock = null;
        breakingProgress = 0;
        fasterProgress = 0;
    }

    @Override
    public void onDisable() {
        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(false, BlockUtils.getBlock(currentBlock), 0, true);
    }

    @Listen
    public void onMotion(MotionEvent event) {
        if (event.getState() == State.POST) return;
        if (timer) {
            mc.timer.timerSpeed = 1f;
            timer = false;
        }

        if (moveFix.getValue()) {
            RotationUtil.setStrafeFix(true, true);
        }
        if (Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) return;
        if (AttackUtil.inCombat && noCombat.getValue()) return;

        if (targetBlock == null) {
            if (switchTimer.hasReached(targetSwitchDelay.getValue())) {
                findTargetBlock();
            }
        } else {
            if (currentBlock == null) {
                if (switchTimer.hasReached(switchDelay.getValue())) {
                    findBreakBlock();
                    breakingProgress = 0f;
                    fasterProgress = 0f;
                    Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, true);
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, currentBlock, currentFace));
                    RotationUtil.overrideRotation(BlockUtils.getFaceRotation(currentFace, currentBlock));
                    mc.thePlayer.swingItem();
                    return;
                }
            }

            if (currentBlock != null) {

                Hud hud = Slack.getInstance().getModuleManager().getInstance(Hud.class);
                hud.centerTimeout = 400;
                hud.centerTitle = "Breaking Bed";
                hud.centerMode = 1;
                hud.centerProgress = breakingProgress;
                hud.centerTimer.reset();

                if (breakingProgress >= breakPercent.getValue()) {
                    if (!mc.thePlayer.onGround && spoofGround.getValue()) return;
                }

                Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, true);

                if (!mc.thePlayer.onGround && spoofGround.getValue()) fasterProgress += 5 * BlockUtils.getHardness(currentBlock);
                breakingProgress += BlockUtils.getHardness(currentBlock);
                hud.centerTimeout = 400;
                hud.centerTitle = "Breaking Bed";
                hud.centerMode = 1;
                hud.centerProgress = breakingProgress;
                hud.centerTimer.reset();
                mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentBlock, (int) (Math.max(breakingProgress, fasterProgress) * 10) - 1);

                if (breakingProgress > breakPercent.getValue() || fasterProgress > breakPercent.getValue()) {
                    if (!mc.thePlayer.onGround && spoofGround.getValue()) {
                        if (spoof.getValue()) {
                            mc.timer.timerSpeed = 0.5f;
                            PacketUtil.send(new C03PacketPlayer(true));
                            timer = true;
                        } else {
                            return;
                        }
                    }
                    RotationUtil.overrideRotation(BlockUtils.getFaceRotation(currentFace, currentBlock));
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentBlock, currentFace));
                    Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(false, BlockUtils.getBlock(currentBlock), 0, true);

                    mc.theWorld.setBlockState(currentBlock, Blocks.air.getDefaultState(), 11);
                    if (currentBlock == targetBlock) {
                        targetBlock = null;
                    }
                    currentBlock = null;
                    switchTimer.reset();
                    mc.thePlayer.swingItem();
                    return;

                } else {
                    if (BlockUtils.getCenterDistance(currentBlock) > radiusDist.getValue()) {
                        currentBlock = null;
                    }
                    if (BlockUtils.getCenterDistance(targetBlock) > radiusDist.getValue()) {
                        currentBlock = null;
                        targetBlock = null;
                    }
                }

                if (!spoofGround.getValue())  mc.thePlayer.swingItem();
            }
        }
    }

    @Listen
    public void onPacket(PacketEvent event) {
        if (whitelist.getValue()) {
            Packet packet = event.getPacket();
            if (packet instanceof S02PacketChat) {
                if (((S02PacketChat) packet).getChatComponent().getUnformattedText().toLowerCase().contains("protect your bed and destroy")) {
                    waitingBed = true;
                }
            }
        }
    }

    @Listen
    public void onRender(RenderEvent event) {
        if (event.getState() == RenderEvent.State.RENDER_2D && currentBlock != null) {
            ScaledResolution sr = mc.getScaledResolution();
            Vector4d pos4 = RenderUtil.getProjectedCoord(currentBlock.getX() + 0.5, currentBlock.getY() + 0.5, currentBlock.getZ() + 0.5, event.getPartialTicks());
            mc.entityRenderer.setupOverlayRendering();
            String displayString = (int) (Math.min(1, Math.max(breakingProgress, fasterProgress)) * 100) + "%";
            if (pos4 != null) {
                mc.MCfontRenderer.drawString(displayString, (float) Math.max(pos4.x, pos4.z) - (mc.MCfontRenderer.getStringWidth(displayString) / 2f), (float) pos4.y, new Color(255, 255, 255).getRGB(), true);

            }
        }


        if (event.getState() != RenderEvent.State.RENDER_3D) return;
        if (currentBlock == null) return;
        double deltaX = currentBlock.getX();
        double deltaY = currentBlock.getY();
        double deltaZ = currentBlock.getZ();
        RenderUtil.drawFilledAABB(new AxisAlignedBB(deltaX, deltaY, deltaZ, deltaX + 1.0, deltaY + Math.min(1, Math.max(breakingProgress, fasterProgress)), deltaZ + 1.0), new Color(255,255,255,60).getRGB());
        RenderUtil.drawFilledBlock(targetBlock, new Color(255,25,25,60).getRGB());

    }

    private void findTargetBlock() {
        int radius = (int) Math.ceil(radiusDist.getValue());
        BlockPos bestBlock = null;
        double bestDist = -1.0;
        int bestAbs = -1;

        for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
                for (int z = radius; z >= -radius + 1; z--) {
                    BlockPos blockPos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                    Block block = BlockUtils.getBlock(blockPos);
                    if (whitelist.getValue() && !waitingBed) {
                        if (bedPos.toVec3().distanceTo(blockPos.toVec3()) < 4) {
                            continue;
                        }
                    }
                    if (block != null) {
                        if (block instanceof BlockBed) {
                            if (waitingBed) {
                                bedPos = blockPos;
                                waitingBed = false;
                                Slack.getInstance().addNotification("Found Bed", "", 3000l, Slack.NotificationStyle.SUCCESS);
                            }
                            switch (sortMode.getValue().toLowerCase()) {
                                case "distance":
                                    if (bestDist == -1 || BlockUtils.getCenterDistance(blockPos) < bestDist) {
                                        bestBlock = blockPos;
                                        bestDist = BlockUtils.getCenterDistance(blockPos);
                                    }
                                    break;
                                case "absolute":
                                    if (bestAbs == -1 || BlockUtils.getAbsoluteValue(blockPos) < bestDist) {
                                        bestBlock = blockPos;
                                        bestAbs = BlockUtils.getAbsoluteValue(blockPos);
                                    }
                            }
                        }
                    }
                }
            }
        }

        if (bestBlock != null) {
            targetBlock = bestBlock;
            switchTimer.reset();
        }
    }

    private void findBreakBlock() {
        if (targetBlock == null) return;

        switch (mode.getValue().toLowerCase()) {
            case "hypixel":
                if (BlockUtils.isReplaceableNotBed(targetBlock.east()) ||
                    BlockUtils.isReplaceableNotBed(targetBlock.north()) ||
                    BlockUtils.isReplaceableNotBed(targetBlock.west()) ||
                    BlockUtils.isReplaceableNotBed(targetBlock.south()) ||
                    BlockUtils.isReplaceableNotBed(targetBlock.up())) {
                    currentBlock = targetBlock;
                } else {
                    float softest = 0f;
                    BlockPos bestBlock;
                    currentBlock = targetBlock.north();
                    if (!(BlockUtils.getBlock(currentBlock) instanceof BlockBed)) {
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        softest = (BlockUtils.getHardness(currentBlock));
                        bestBlock = currentBlock;
                    } else {
                        currentBlock = targetBlock.up();
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        softest = (BlockUtils.getHardness(currentBlock));
                        bestBlock = currentBlock;
                    }

                    currentBlock = targetBlock.west();
                    if (!(BlockUtils.getBlock(currentBlock) instanceof BlockBed)) {
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        if (BlockUtils.getHardness(currentBlock) > softest) {
                            softest = BlockUtils.getHardness(currentBlock);
                            bestBlock = currentBlock;
                        }
                    }

                    currentBlock = targetBlock.up();
                    if (!(BlockUtils.getBlock(currentBlock) instanceof BlockBed)) {
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        if (BlockUtils.getHardness(currentBlock) > softest) {
                            softest = BlockUtils.getHardness(currentBlock);
                            bestBlock = currentBlock;
                        }
                    }

                    currentBlock = targetBlock.east();
                    if (!(BlockUtils.getBlock(currentBlock) instanceof BlockBed)) {
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        if (BlockUtils.getHardness(currentBlock) > softest) {
                            softest = BlockUtils.getHardness(currentBlock);
                            bestBlock = currentBlock;
                        }
                    }

                    currentBlock = targetBlock.south();
                    if (!(BlockUtils.getBlock(currentBlock) instanceof BlockBed)) {
                        Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(true, BlockUtils.getBlock(currentBlock), 0, false);
                        if (BlockUtils.getHardness(currentBlock) > softest) {
                            softest = BlockUtils.getHardness(currentBlock);
                            bestBlock = currentBlock;
                        }
                    }

                    currentBlock = bestBlock;
                    Slack.getInstance().getModuleManager().getInstance(AutoTool.class).getTool(false, BlockUtils.getBlock(currentBlock), 0, false);
                }
                currentFace = EnumFacing.UP;
                break;
            case "none":
                currentBlock = targetBlock;
                currentFace = EnumFacing.UP;
                break;
            case "mmc":
                currentBlock = targetBlock;
                while (!BlockUtils.isReplaceable(currentBlock.up())) {
                    currentBlock = currentBlock.up();
                }
                currentFace = EnumFacing.UP;
                break;
        }
    }

    @Override
    public String getMode() {
        switch (displayMode.getValue()) {
            case "Simple":
                return mode.getValue().toString();
        }
        return null;
    }
}
