// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.speeds.vanilla;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.utils.player.MovementUtil;

public class MMCSpeed implements ISpeed {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
            mc.thePlayer.jump();
        }

        if (mc.thePlayer.hurtTime == 8)
            MovementUtil.strafe();
    }

    @Override
    public String toString() {
        return "MMC";
    }
}
