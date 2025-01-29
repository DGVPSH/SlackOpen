// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.utilties.Anticheat.impl;

import cc.slack.features.modules.impl.utilties.Anticheat.utils.AnticheatAlert;
import cc.slack.features.modules.impl.utilties.Anticheat.utils.NewBlocks;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class AutoblockCheck implements IMinecraft {

    private EntityPlayer lp = null;
    private EntityPlayer p = null;
    private int vl = 0;

    private int blockTicks = 0;
    private boolean justSwung = false;

    public AutoblockCheck(EntityPlayer player) {
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

        if (p.isBlocking()) {
            blockTicks ++;
        } else {
            blockTicks = 0;
        }

        if (justSwung) {
            if (blockTicks > 3) {
                vl ++;
                AnticheatAlert.flag("[AutoBlock] - " + player.getDisplayName().getFormattedText() + "- vl:" + vl);
            }
        }
        AnticheatAlert.debugMessage("[AutoBlock] "  + p.getDisplayName().getFormattedText() + " BlockTicks: " + blockTicks + " swung: " + justSwung);
        justSwung = false;
    }

    public void swing() {
        justSwung = true;
    }
}

