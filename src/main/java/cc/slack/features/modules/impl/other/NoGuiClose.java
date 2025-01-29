// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.other;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.other.PrintUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(
        name = "NoGuiClose",
        category = Category.OTHER
)
public class NoGuiClose extends Module {
    public BooleanValue chatOnly = new BooleanValue("Chat Only", false);

    public NoGuiClose() {
        addSettings(chatOnly);
    }
    @Listen
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S2EPacketCloseWindow && (mc.currentScreen instanceof GuiChat || !chatOnly.getValue())) {
            event.cancel();
        }
    }

}
