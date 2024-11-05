package cc.slack.utils.client;

import cc.slack.events.impl.player.MotionEvent;
import cc.slack.utils.other.MathTimerUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;

/**
 * @author nyoxy
 * @since 10/27/2024
 */
public final class CameraUtil {

    public static Minecraft mc = Minecraft.getMinecraft();

    public static double y;
    public static MathTimerUtil stopWatch = new MathTimerUtil();

    public static void setY(double y) {
        stopWatch.reset();
        CameraUtil.y = y;
    }

    public static void setY() {
        if (stopWatch.finished(80)) CameraUtil.y = mc.thePlayer.lastTickPosY;
        stopWatch.reset();
    }

    @Listen
    public void onMotion(MotionEvent event) {
        if (stopWatch.finished(80)) return;
        mc.thePlayer.cameraYaw = 0;
        mc.thePlayer.cameraPitch = 0;
    }
}