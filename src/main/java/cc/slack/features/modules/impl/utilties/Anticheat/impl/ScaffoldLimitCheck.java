package cc.slack.features.modules.impl.utilties.Anticheat.impl;

import cc.slack.features.modules.impl.utilties.Anticheat.utils.AnticheatAlert;
import cc.slack.features.modules.impl.utilties.Anticheat.utils.NewBlocks;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class ScaffoldLimitCheck implements IMinecraft {

    private EntityPlayer lp = null;
    private EntityPlayer p = null;

    private int scaffTicks = 0;
    private boolean isBackwards = false;
    private int vl = 0;
    private double startingY = 0.0;
    private boolean wasNewBlock = false;

    public ScaffoldLimitCheck(EntityPlayer player) {
        p = player;
        vl = 0;
    }

    public void runCheck(EntityPlayer player) {
        if (lp == null) {
            lp = p;
            p = player;
            return;
        } else {
            lp = p;
            p = player;
        }


        // Flat scaffold checks

        if (player.motionY <= 0 && player.posY % 1 < 0.04) {
            if (NewBlocks.isNewBlock(new BlockPos(player.posX, player.posY - 1, player.posZ))) {
                wasNewBlock = true;
                if (isBackwards) {
                    scaffTicks ++;
                }
            } else {
                wasNewBlock = false;
                scaffTicks = 0;
            }
        } else {
            if (wasNewBlock) {
                if (isBackwards) {
                    scaffTicks ++;
                }
            }
        }
        
        if (player.isSneaking()) {
            scaffTicks = 0;
        }

        isBackwards = true;
        float[] move = RotationUtil.getRotations(new Vec3(0, 0, 0), new Vec3(p.motionX, 0, p.motionZ));
        if (Math.abs(MathHelper.wrapAngleTo180_double(move[0] - player.rotationYawHead)) > 90 && Math.abs(Math.hypot(p.motionX, p.motionZ)) > 0.1) {
            isBackwards = true;
        }

        if (scaffTicks > 40 && isBackwards) {
            vl ++;
            if (vl > 20) {
                if (player.posY - startingY < 5) {
                    AnticheatAlert.flag("[Scaffold Limit] - " + player.getDisplayName().getFormattedText() + "- vl:" + vl);
                }
            }
        }

        if (scaffTicks % 200 == 30 && isBackwards) {
            startingY = player.posY;
        }

        AnticheatAlert.debugMessage("[Scaffold Limit] "  + p.getDisplayName().getFormattedText() + " ScaffTicks: " + scaffTicks + " backwards: " + isBackwards + " WNB: " + wasNewBlock + " nb: " + NewBlocks.isNewBlock(new BlockPos(p.posX, p.posY - 1, p.posZ)) + " air: " + PlayerUtil.isOverAir());
    }
}

