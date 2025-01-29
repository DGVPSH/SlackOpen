// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.other;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.other.PrintUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.ArrayList;

@ModuleInfo(
        name = "Test",
        category = Category.OTHER
)
public class Test extends Module {

    public final NumberValue<Integer> delay = new NumberValue<>("Delay", 40, 1, 100, 1);
    public ArrayList<String> messages = new ArrayList<>();

    public Test() {
        addSettings(delay);
    }

    @Listen
    public void onUpdate (UpdateEvent event) {
        if (messages.isEmpty()) return;

        for (int i = 0; i < messages.size(); i++) {
            if (mc.thePlayer.ticksExisted % (messages.size() * delay.getValue()) == delay.getValue() * i) {
                mc.thePlayer.sendChatMessage(messages.get(i));
            }
        }
    }

}
