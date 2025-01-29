// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.utilties.Anticheat.impl;

import cc.slack.features.modules.impl.utilties.Anticheat.utils.AnticheatAlert;
import cc.slack.features.modules.impl.utilties.Anticheat.utils.NewBlocks;
import cc.slack.utils.client.IMinecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public final class TowerLimitCheck implements IMinecraft {

    private EntityPlayer lp = null;
    private EntityPlayer p = null;
    private int vl = 0;

    private ArrayList<Double> motions = new ArrayList<>();

    public TowerLimitCheck(EntityPlayer player) {
        p = player;
        vl = 0;
        motions.clear();
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

        if (player.ticksSinceLastDamage > 10 || player.isPotionActive(Potion.jump)) {
            motions.clear();
        }

        // if over 0.3 motiony average then flag
        if (NewBlocks.isNewBlock(new BlockPos(player.posX, player.posY - 1, player.posZ)) || !(player.motionY <= 0 && player.posY % 1 < 0.04)) {
            motions.add(p.motionY);
            if (motions.size() > 10) {
                motions.remove(0);
                double dist = 0.0;
                for (double m : motions) {
                    dist += m/10;
                }
                if (dist > 0.25) {
                    vl ++;
                    AnticheatAlert.flag("[Tower Limit] - " + player.getDisplayName().getFormattedText() + "- vl:" + vl);
                }
            }
        }
        AnticheatAlert.debugMessage("[Tower Limit] "  + p.getDisplayName().getFormattedText() + " Motions: " + motions.toString());
    }
}

