// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.flights.impl.others;

import cc.slack.events.impl.player.CollideEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.flights.IFlight;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.ItemSpoofUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;


public class MMCFlight implements IFlight {


    double startY;
    boolean jump;

    @Override
    public void onEnable() {
        startY = Math.floor(mc.thePlayer.posY);
    }


    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && MovementUtil.isBindsMoving()) {
            mc.thePlayer.jump();
            MovementUtil.strafe(0.45f);
            jump = !jump;
        }

        if (mc.gameSettings.keyBindJump.isKeyDown() || mc.thePlayer.posY < startY) {
            startY = Math.floor(mc.thePlayer.posY);
        }

        if (!mc.thePlayer.onGround && jump) {
            if (mc.thePlayer.posY + mc.thePlayer.motionY < startY && pickBlock()) {
                RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw, 90f});
                BlockPos blockPlace = new BlockPos(mc.thePlayer.posX, startY - 1, mc.thePlayer.posZ);
                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), blockPlace, EnumFacing.UP, new Vec3(mc.thePlayer.posX, startY, mc.thePlayer.posZ));
                mc.thePlayer.swingItem();
            }
        }
    }

    @Override
    public void onCollide(CollideEvent event) {
        if (jump) return;
        if (event.getBlock() instanceof BlockAir && event.getY() <= startY)
            event.setBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, startY, event.getZ() + 1));
    }

    private boolean pickBlock() {
        int slot = InventoryUtil.pickHotarBlock(false);
        if (slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;

            return true;
        }
        ItemSpoofUtil.stopSpoofing();
        return false;
    }

    @Override
    public String toString() {
        return "MMC";
    }
}
