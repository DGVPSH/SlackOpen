package cc.slack.features.modules.impl.movement.steps.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.impl.movement.steps.IStep;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;

public class TestStep implements IStep {

    @Override
    public void onUpdate(UpdateEvent event) {
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.4196f + Math.random() * 0.000095f, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.4197f + 0.3328 + Math.random() * 0.000095, mc.thePlayer.posZ, false));
        mc.thePlayer.stepHeight = 1f;
    }

    @Override
    public String toString() {
        return "Test";
    }
}
