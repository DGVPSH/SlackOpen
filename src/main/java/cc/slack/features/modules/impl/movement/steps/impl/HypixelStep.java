package cc.slack.features.modules.impl.movement.steps.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.exploit.Disabler;
import cc.slack.features.modules.impl.movement.steps.IStep;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;

public class HypixelStep implements IStep {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
            }
            if (Slack.getInstance().getModuleManager().getInstance(Disabler.class).disabled) {
                switch (mc.thePlayer.offGroundTicks) {
                    case 3:
                        mc.thePlayer.motionY -= -0.0025;
                        break;
                    case 4:
                        mc.thePlayer.motionY -= 0.04;
                        break;
                    case 5:
                        mc.thePlayer.motionY -= 0.1905189780583944;
                        break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Hypixel Lowhop";
    }
}
