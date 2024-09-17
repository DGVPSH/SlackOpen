package cc.slack.features.modules.impl.utilties;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.WorldEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.impl.combat.KillAura;
import cc.slack.features.modules.impl.movement.Flight;
import cc.slack.features.modules.impl.player.TimerModule;
import cc.slack.features.modules.impl.world.InvManager;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.features.modules.impl.world.Stealer;
import cc.slack.start.Slack;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;


@ModuleInfo(
        name = "Float",
        category = Category.UTILITIES
)

public class Float extends Module {

    private final BooleanValue spoofGround = new BooleanValue("spoofGround", true);

    public Float() {
        addSettings(spoofGround);
    }

    @SuppressWarnings("unused")
    @Listen
    public void onPacket (PacketEvent event) {
        Packet packet = event.getPacket();
        if (mc.thePlayer.onGround && packet instanceof C03PacketPlayer) {
            ((C03PacketPlayer) packet).onGround = spoofGround.getValue();
            ((C03PacketPlayer) packet).y += 0.000000000001;
            event.setPacket(packet);
        }
    }
}
