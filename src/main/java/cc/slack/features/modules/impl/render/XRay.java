// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render;

import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.other.PrintUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector4d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(
        name = "XRay",
        category = Category.RENDER
)
public class XRay extends Module {

    private final ModeValue<String> mode = new ModeValue<>("Mode", new String[]{"Hypixel", "Vanilla"});
    private final NumberValue<Integer> updateDistance = new NumberValue<>("Update Distance", 10, 5, 20, 1);
    private final NumberValue<Integer> updateSpeed = new NumberValue<>("Update Speed", 1, 1, 3,  1);

    public XRay() { addSettings(mode, updateDistance, updateSpeed); }

    private float oldgammavalue;
    private final int[] blockIds = new int[]{14, 15, 16, 21, 56, 73, 74, 129, 153};
    private float packetsSent = 0;
    private final int[] oreIds = new int[]{14, 15, 16, 21, 56, 73, 129, 153};
    private static List<String> sentCoords = new ArrayList<>();
    private static int renderX = -1;
    private static int renderY = -1;
    private static int renderZ = -1;

    @Listen
    public void onMove(MoveEvent event) {

        if (mc.gameSettings.keyBindSneak.isPressed()) {
            sentCoords.clear();
        }

        if (mode.getValue().equalsIgnoreCase("hypixel") && !mc.playerController.isHittingBlock) {

            int range = updateDistance.getValue();
            int speed = updateSpeed.getValue();

            packetsSent = 0;
            renderX = -1;
            renderY = -1;
            renderZ = -1;

            for (int x = mc.thePlayer.getPosition().getX() - range; x <= mc.thePlayer.getPosition().getX() + range; x++) {
                for (int y = mc.thePlayer.getPosition().getY() - range; y <= mc.thePlayer.getPosition().getY() + range; y++) {
                    for (int z = mc.thePlayer.getPosition().getZ() - range; z <= mc.thePlayer.getPosition().getZ() + range; z++) {

                        if (packetsSent >= speed) return;

                        BlockPos pos = new BlockPos(x, y, z);
                        Block blockPos = mc.theWorld.getBlockState(pos).getBlock();

                        if (Arrays.stream(oreIds).anyMatch(id -> id == Block.getIdFromBlock(blockPos))) {

                            int posX = pos.getX();
                            int posY = pos.getY();
                            int posZ = pos.getZ();

                            String coordsKey = posX + "," + posY + "," + posZ;

                            if (sentCoords.indexOf(coordsKey) == -1) {
                                renderX = posX;
                                renderY = posY;
                                renderZ = posZ;

                                PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                                PacketUtil.send(new C0APacketAnimation());
                                packetsSent++;

                                sentCoords.add(coordsKey);
                                //PrintUtil.message("[" + mc.thePlayer.ticksExisted + "] " + posX + ", " + posY + ", " + posZ);
                            }
                        }
                    }
                }
            }
        }
    }

    @Listen
    public void onRender(RenderEvent event) {
        if (mode.getValue().equalsIgnoreCase("hypixel")) {
            if (renderY > 0) {
                RenderUtil.drawFilledBlock(new BlockPos(renderX, renderY, renderZ), new Color(255, 255, 255, 100).getRGB());
            }
        }
    }

    @Override
    public void onEnable() {
        oldgammavalue = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10.0f;
        mc.gameSettings.ambientOcclusion = 0;

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft.getMinecraft().renderGlobal.loadRenderers();
            });
        }).start();
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = oldgammavalue;
        mc.gameSettings.ambientOcclusion = 1;
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    public boolean shouldRenderBlock(Block block) {
        for (int id : this.blockIds) {
            if (block != Block.getBlockById(id)) continue;
            return true;
        }
        return false;
    }
}
