package cc.slack.features.modules.impl.player.nofalls.specials;

import cc.slack.events.State;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.features.modules.impl.player.nofalls.INoFall;
import cc.slack.utils.network.PacketUtil;
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

            final double fallDistance = calculateFallDistance();

            updateDistance(fallDistance);

            if (isGrounded(event)) {
                resetDistance();
            }

            float distance = distancecheck;

            if (shouldSendPackets(distance)) {
                sendPackets(event);
                distance = 0;
            }

            distancecheck = distance;
        }
    }

    private boolean isPreState(MotionEvent event) {
        return event.getState() == State.PRE;
    }

    private double calculateFallDistance() {
        return mc.thePlayer.lastTickPosY - mc.thePlayer.posY;
    }

    private void updateDistance(double fallDistance) {
        if (fallDistance > 0) {
            distancecheck += fallDistance;
        }
    }

    private boolean isGrounded(MotionEvent event) {
        return event.isGround();
    }

    private void resetDistance() {
        distancecheck = 0;
    }

    private boolean shouldSendPackets(float distance) {
        return distance > 3;
    }

    private void sendPackets(MotionEvent event) {
        PacketUtil.send(new C03PacketPlayer.C06PacketPlayerPosLook(event.getX(), event.getY(), event.getZ(), event.getYaw(), event.getPitch(), true));
        PacketUtil.send(new C08PacketPlayerBlockPlacement(getCurrentStack()));
        mc.timer.timerSpeed = 0.5f;
        timer = true;
    }

    private ItemStack getCurrentStack() {
        return mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack();
    }

    public int getItemIndex() {
        return mc.thePlayer.inventory.currentItem;
    }

    private boolean isOverVoid() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 40, mc.thePlayer.posZ),
                true, true, false) == null;
    }

    @Override
    public String toString() {
        return "Hypixel2";
    }
}