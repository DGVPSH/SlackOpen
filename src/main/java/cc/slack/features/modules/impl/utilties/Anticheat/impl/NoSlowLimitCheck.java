package cc.slack.features.modules.impl.utilties.Anticheat.impl;

import cc.slack.features.modules.impl.utilties.Anticheat.utils.AnticheatAlert;
import cc.slack.utils.client.IMinecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public final class NoSlowLimitCheck implements IMinecraft {

    private EntityPlayer lp = null;
    private EntityPlayer p = null;
    private int vl = 0;

    private int blockTicks = 0;

    public NoSlowLimitCheck(EntityPlayer player) {
        p = player;
        vl = 0;
    }

    public void runCheck(EntityPlayer player) {
        if (lp == null) {
            lp = p;
            p = player;
            return;
        } else {
            lp = (EntityPlayer) ((EntityLivingBase) p);
            p = player;
        }

        if (p.isBlocking() || p.isUsingItem()) {
            blockTicks ++;
        } else {
            blockTicks = 0;
        }

        if (Math.abs(Math.hypot(p.motionX, p.motionZ)) > 0.11) {
            if (blockTicks > 10 && p.ticksSinceLastDamage > 10) {
                vl ++;
                AnticheatAlert.flag("[Noslow Limit] - " + player.getDisplayName().getFormattedText() + "- vl:" + vl);
            }
        }
        AnticheatAlert.debugMessage("[Noslow Limit] "  + p.getDisplayName().getFormattedText() + " BlockTicks: " + blockTicks + " Motion: " + Math.abs(Math.hypot(p.motionX, p.motionZ)) + p.motionX);
    }
}

