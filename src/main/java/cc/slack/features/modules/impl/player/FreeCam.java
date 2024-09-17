package cc.slack.features.modules.impl.player;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.MovementInput;

@ModuleInfo(
        name = "FreeCam",
        category = Category.PLAYER
)
public class FreeCam extends Module {

    private EntityOtherPlayerMP entity;
    private boolean sprinting;

    @Override
    public void onEnable() {
        entity = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        entity.rotationYawHead = mc.thePlayer.rotationYaw;
        entity.rotationPitch = mc.thePlayer.rotationPitch;
        entity.motionX = mc.thePlayer.motionX;
        entity.motionY = mc.thePlayer.motionY;
        entity.motionZ = mc.thePlayer.motionZ;
        sprinting = mc.gameSettings.keyBindSprint.isKeyDown();
        entity.copyLocationAndAnglesFrom(mc.thePlayer);
        mc.theWorld.addEntityToWorld(-6969, entity);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.setPosition(entity.posX, entity.posY, entity.posZ);
        mc.thePlayer.rotationYaw = entity.rotationYaw;
        mc.thePlayer.rotationPitch = entity.rotationPitch;
        mc.thePlayer.motionX = entity.motionX;
        mc.thePlayer.motionY = entity.motionY;
        mc.thePlayer.motionZ = entity.motionZ;
        mc.gameSettings.keyBindSprint.setPressed(sprinting);
        mc.theWorld.removeEntity(entity);
    }

    @Listen
    public void onPacket (PacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer p = event.getPacket();
            p.x = entity.posX;
            p.y = entity.posY;
            p.z = entity.posZ;
            p.yaw = entity.rotationYaw;
            p.pitch = entity.rotationPitch;
            event.setPacket(p);
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof C0APacketAnimation || packet instanceof C03PacketPlayer ||
                packet instanceof C02PacketUseEntity || packet instanceof C0BPacketEntityAction ||
                packet instanceof C08PacketPlayerBlockPlacement) {
            event.cancel();
        }
    }

    @Listen
    public void onStrafe (StrafeEvent event) {
        mc.thePlayer.motionY = 0;
        if(MovementUtil.isBindsMoving()) {
            MovementUtil.setMotionSpeed(1);
        } else {
            MovementUtil.setMotionSpeed(0);
        }
        if(mc.gameSettings.keyBindJump.isKeyDown()) MovementUtil.setVClip(0.5);
        if(mc.gameSettings.keyBindSneak.isKeyDown()) MovementUtil.setVClip(-0.5);
        if(mc.gameSettings.keyBindForward.isKeyDown()) MovementUtil.setHClip(0.5);
        if(mc.gameSettings.keyBindBack.isKeyDown()) MovementUtil.setHClip(-0.5);
    }

    @Listen
    public void onMovement (MovementInput event) {
        event.sneak = false;
    }

}
