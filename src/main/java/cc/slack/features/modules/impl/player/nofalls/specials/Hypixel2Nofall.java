package cc.slack.features.modules.impl.player.nofalls.specials;

import cc.slack.events.State;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.features.modules.impl.player.nofalls.INoFall;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.Vec3;

public class Hypixel2Nofall implements INoFall {

    public static float distancecheck;
    private boolean timer = false;

    @Override
    public void onDisable() {
        if (timer) {
            timer = false;
            mc.timer.timerSpeed = 1;
        }
    }

    @Override
    public void onMotion(MotionEvent event) {
        if (isPreState(event)) {
            if (isOverVoid()) return;
            if (timer) {
                timer = false;
                mc.timer.timerSpeed = 1;
            }

            if (mc.thePlayer.fallDistance > 2.9) {
                PacketUtil.send(new C03PacketPlayer(true));
                mc.timer.timerSpeed = 0.5f;
                timer = true;
            }
        }
    }

    private boolean isPreState(MotionEvent event) {
        return event.getState() == State.PRE;
    }

    private boolean isOverVoid() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 40, mc.thePlayer.posZ),
                true, true, false) == null;
    }

    @Override
    public String toString() {
        return "Hypixel Packet";
    }
}