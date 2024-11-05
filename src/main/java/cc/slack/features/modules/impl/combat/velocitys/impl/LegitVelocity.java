package cc.slack.features.modules.impl.combat.velocitys.impl;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.features.modules.impl.combat.Velocity;
import cc.slack.features.modules.impl.combat.velocitys.IVelocity;
import cc.slack.start.Slack;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public class LegitVelocity implements IVelocity {
    @Override
    public void onPacket(PacketEvent event){
        if (event.getPacket() instanceof S12PacketEntityVelocity){
            S12PacketEntityVelocity s12PacketEntityVelocity = new S12PacketEntityVelocity();
            int hurtTimeValue = Slack.getInstance().getModuleManager().getInstance(Velocity.class).hurtTimeDelay.getValue();
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()){
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), mc.thePlayer.hurtTime == hurtTimeValue);
            }
        }
    }
    @Override
    public String toString() {
        return "Legit Velocity";
    }
}
