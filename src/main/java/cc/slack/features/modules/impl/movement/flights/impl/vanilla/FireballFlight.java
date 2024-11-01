// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement.flights.impl.vanilla;

import cc.slack.start.Slack;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.Flight;
import cc.slack.features.modules.impl.movement.flights.IFlight;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class FireballFlight implements IFlight {

    private boolean sent = false;
    private boolean reset = false;
    private boolean gotVelo = false;

    private int ticks = 0;

    private float speed = 0f;
    private float yaw = 0f;

    private int fireballSlot = 0;

    @Override
    public void onEnable() {
        ticks = 0 ;
        sent = false;
        reset = false;
        gotVelo = false;
        fireballSlot = InventoryUtil.findFireball();
        if (fireballSlot == -1) {
            Slack.getInstance().addNotification("Fireball needed to fly", "", 3000L, Slack.NotificationStyle.WARN);
            Slack.getInstance().getModuleManager().getInstance(Flight.class).setToggle(false);
        }
        fireballSlot -= 36;
    }

    @Override
    public void onDisable() {
        if (sent && !reset) {
            if (mc.thePlayer.inventory.currentItem != fireballSlot)
                PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }


    @Override
    public void onUpdate(UpdateEvent event) {
        switch (ticks) {
            case 0:
                if (mc.thePlayer.inventory.currentItem != fireballSlot)
                    PacketUtil.send(new C09PacketHeldItemChange(fireballSlot));
            case 1:
            case 2:
                MovementUtil.resetMotion();
                RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw + (ticks + 1) * 60, Slack.getInstance().getModuleManager().getInstance(Flight.class).fbpitch.getValue()});
                break;
            case 3:
                RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw + 180, Slack.getInstance().getModuleManager().getInstance(Flight.class).fbpitch.getValue()});
                PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(InventoryUtil.getSlot(fireballSlot).getStack()));
                break;
            case 4:
                RotationUtil.setClientRotation(new float[]{mc.thePlayer.rotationYaw + 80, Slack.getInstance().getModuleManager().getInstance(Flight.class).fbpitch.getValue()});
                PacketUtil.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            default:
                if (gotVelo && mc.thePlayer.onGround && mc.thePlayer.ticksSinceLastDamage > 20) {
                    Slack.getInstance().getModuleManager().getInstance(Flight.class).setToggle(false);
                }

                if (gotVelo && mc.thePlayer.hurtTime == 9) {
                    MovementUtil.strafe(MovementUtil.getSpeed() * 1.04f);
                    speed = Slack.getInstance().getModuleManager().getInstance(Flight.class).fbspeed.getValue();
                    yaw = MovementUtil.getDirection();
                } else if (gotVelo && mc.thePlayer.hurtTime > 1 && mc.thePlayer.hurtTime < 9) {
                    speed *= 0.96f;
                    MovementUtil.strafe(speed, yaw);
                } else if (mc.thePlayer.ticksSinceLastDamage <  Slack.getInstance().getModuleManager().getInstance(Flight.class).fbflat.getValue()) {
                    mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, 0);
                }

        }

        ticks ++;
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                gotVelo = true;
                BlinkUtil.disable();
            }
        }
    }

    @Override
    public String toString() {
        return "Fireball Flight";
    }
}
