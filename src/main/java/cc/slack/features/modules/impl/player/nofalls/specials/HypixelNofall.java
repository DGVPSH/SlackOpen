// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.player.nofalls.specials;


import cc.slack.events.State;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.player.Blink;
import cc.slack.features.modules.impl.player.nofalls.INoFall;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;

public class HypixelNofall implements INoFall {

    private Boolean dmgFall = false;
    private Boolean blink = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.fallDistance > 4 && mc.thePlayer.ticksSinceLastTeleport > 3) {
            dmgFall = true;
        }
    }

    @Override
    public void onMotion(MotionEvent event) {
        if (event.getState() != State.PRE) return;

        if (mc.thePlayer.onGround && blink) {
            BlinkUtil.disable();
        }

        if (mc.thePlayer.onGround && dmgFall) {
            BlinkUtil.enable(false, true);
            blink = true;
            event.setGround(false);
            mc.thePlayer.fallDistance = 0;
            mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
            dmgFall = false;
        }

    }

    public String toString() {
        return "Hypixel";
    }
}
