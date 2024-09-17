 package cc.slack.features.modules.impl.movement.longjumps.impl.vulcan;

import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.movement.LongJump;
import cc.slack.features.modules.impl.movement.longjumps.ILongJump;
import cc.slack.start.Slack;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

 public class HypixelLJ implements ILongJump {

    private int jumps = 0;
    private boolean blink = false;
    private boolean wait = false;

    @Override
    public void onMotion(MotionEvent event) {
        switch (jumps) {
            case 0:
                BlinkUtil.enable(false, true);
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                MovementUtil.strafe(0);
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    jumps ++;
                }
                if (jumps > 0) MovementUtil.spoofNextC03(false);

                if (mc.thePlayer.offGroundTicks == 4) {
                    BlinkUtil.releasePackets();
                }
                break;
            case 6:
                if (mc.thePlayer.offGroundTicks == 4) {
                    BlinkUtil.disable();
                    BlinkUtil.enable(true, false);
                }
                MovementUtil.strafe(0);

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    MovementUtil.strafe(0.48f);
                    jumps ++;
                }

            case 7:
                if (mc.thePlayer.offGroundTicks == 8) {
                    BlinkUtil.disable();
                }
        }
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity packet = event.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                event.cancel();
                MovementUtil.strafe(0.3f);
                mc.thePlayer.motionY = packet.getMotionY() * Slack.getInstance().getModuleManager().getInstance(Velocity.class).vertical.getValue().doubleValue() / 100 / 8000.0;
                Slack.getInstance().getModuleManager().getInstance(LongJump.class).toggle();
            }
        }
    }

    @Override
    public void onDisable() {
        BlinkUtil.disable();
    }

    @Override
    public void onEnable() {
        blink = false;
        jumps = 0;
    }

    @Override
    public String toString() {
        return "Hypixel";
    }
}
