// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.utils.player.MovementUtil;

public class MMCVelocity implements IVelocity {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.hurtTime < 8 && mc.thePlayer.hurtTime > 2 && mc.thePlayer.onGround) {
            MovementUtil.strafe();
        }
    }

    @Override
    public String toString() {
        return "MMC";
    }
}
