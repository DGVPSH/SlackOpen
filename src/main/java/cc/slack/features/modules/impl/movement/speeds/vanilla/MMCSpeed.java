// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.speeds.vanilla;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.speeds.ISpeed;
import cc.slack.utils.other.BlockUtils;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.ItemSpoofUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public class MMCSpeed implements ISpeed {

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
            mc.thePlayer.jump();
            MovementUtil.strafe(0.62f);
        }
        BlockPos blockPlace = new BlockPos(mc.thePlayer.posX, Math.ceil(mc.thePlayer.posY) + 2, mc.thePlayer.posZ);

        if (pickBlock() && mc.thePlayer.onGround && BlockUtils.isReplaceable(blockPlace)) {
            RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw, -90f});
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), blockPlace, EnumFacing.DOWN, new Vec3(mc.thePlayer.posX, Math.ceil(mc.thePlayer.posY) + 2, mc.thePlayer.posZ));
            mc.thePlayer.swingItem();
        }
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
