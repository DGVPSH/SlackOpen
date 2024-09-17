// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.player;

import cc.slack.events.impl.player.AttackEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.BlinkUtil;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "Blink",
        category = Category.PLAYER
)
public class Blink extends Module {


    private final BooleanValue outbound = new BooleanValue("Outbound", true);
    private final BooleanValue inbound = new BooleanValue("Inbound", false);
    private final BooleanValue pulse = new BooleanValue("Pulse", false);
    private final NumberValue<Integer> delayValue = new NumberValue<>("Delay", 300, 0, 2000, 25);
    private final BooleanValue disableOnAttack = new BooleanValue("Disable On Attack", false);
    private int delay;

    public Blink() {
        super();
        addSettings(outbound, inbound, pulse, delayValue, disableOnAttack);
    }

    @Listen
    public void onUpdate(UpdateEvent event) {
        if (!pulse.getValue()) return;
        if (++delay > delayValue.getValue() / 50) {
            BlinkUtil.releasePackets();
            delay = 0;
        }
    }

    @Listen
    public void onAttack(AttackEvent event) {
        if (disableOnAttack.getValue()) {
            toggle();
        }
    }

    @Override
    public void onEnable() {
        BlinkUtil.enable(inbound.getValue(), outbound.getValue());
    }

    @Override
    public void onDisable() {
        BlinkUtil.disable();
    }


}
