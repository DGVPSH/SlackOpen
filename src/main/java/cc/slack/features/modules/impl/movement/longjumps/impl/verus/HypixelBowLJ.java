// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.longjumps.impl.verus;

import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.LongJump;
import cc.slack.features.modules.impl.movement.longjumps.ILongJump;
import cc.slack.start.Slack;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class HypixelBowLJ implements ILongJump {

    boolean back, bowd, receivedS12;
    int ticks, prevSlot;
    double moveSpeed = 0;
    double yMot;


    @Override
    public void onEnable() {
        ticks = 0;

        back = receivedS12 = bowd = false;

        ItemStack bow = null;
        prevSlot = mc.thePlayer.inventory.currentItem;
        int slot = -1;
        for(int i = 36; i < 45; ++i) {
            if(InventoryUtil.getSlot(i).getHasStack()) {
                ItemStack itemStack = InventoryUtil.getSlot(i).getStack();
                if (itemStack.getItem() instanceof ItemFishingRod) {
                    bow = itemStack;
                    slot = i - 36;
                }
            }
        }
        if (bow != null) {
            mc.thePlayer.inventory.currentItem = slot;
        } else {
            Slack.getInstance().getModuleManager().getInstance(LongJump.class).toggle();
            Slack.getInstance().addNotification("Need a rod to longjump", "", 3000L, Slack.NotificationStyle.WARN);
        }
    }

    @Override
    public void onDisable() {
        mc.thePlayer.inventory.currentItem = prevSlot;
    }

    @Override
    public void onUpdate (UpdateEvent event ) {
        if (receivedS12) {
            if (mc.thePlayer.offGroundTicks == 6) {
                BlinkUtil.disable();
                mc.thePlayer.motionY = yMot;
            } else if (mc.thePlayer.offGroundTicks == 7) {
                MovementUtil.strafe(Slack.getInstance().getModuleManager().getInstance(LongJump.class).speedValue.getValue().floatValue());
                Slack.getInstance().getModuleManager().getInstance(LongJump.class).toggle();
            }
        } else {
            if (!bowd) {
                mc.gameSettings.keyBindUseItem.pressed = true;
                bowd = true;
            } else {
                mc.gameSettings.keyBindUseItem.pressed = false;
            }
        }
    }

    @Override
    public void onMove(MoveEvent event) {
        if(!receivedS12) {
            event.setX(0);
            event.setZ(0);
            event.cancel();
        }
    }

    @Override
    public void onPacket(PacketEvent event) {
        if(event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = event.getPacket();
            if(packet.getEntityID() != mc.thePlayer.getEntityId()) return;
            if (!receivedS12) {
                receivedS12 = true;
                event.cancel();
                yMot = packet.getMotionY() / 8000.0;
                BlinkUtil.enable(true, false);
                mc.thePlayer.jump();
            }
        }
    }

    @Override
    public String toString() {
        return "Hypixel Bow";
    }
}
