package cc.slack.utils.render;
import cc.slack.utils.client.IMinecraft;

public class FreeCamUtil implements IMinecraft {

    public static float cameraX;
    public static float cameraZ;
    public static float cameraY;
    public static float lastCameraX;
    public static float lastCameraZ;
    public static float lastCameraY;
    public static int lastTick = 0;
    public static boolean freelooking;

    public static void setFreecam(boolean setFreelook) {
        freelooking = setFreelook;
    }

    public static void setPos(float x, float y,float z) {
        cameraX = x;
        cameraY = y;
        cameraZ = z;
    }

    public static void pushLastTick() {
        lastCameraX = cameraX;
        lastCameraY = cameraY;
        lastCameraZ = cameraZ;
        if (mc.thePlayer != null)
            lastTick = mc.thePlayer.ticksExisted;
    }

    public static void setPos(double x, double y,double z) {
        if (mc.thePlayer != null) {
            if (mc.thePlayer.ticksExisted > lastTick) {
                pushLastTick();
            }
        }
        cameraX = (float) x;
        cameraY = (float) y;
        cameraZ = (float) z;

    }
}
