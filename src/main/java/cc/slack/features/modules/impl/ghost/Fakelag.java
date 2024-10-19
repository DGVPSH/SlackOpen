package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.player.AttackEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.network.PingSpoofUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.util.Vec3;

@ModuleInfo(
        name = "FakeLag",
        category = Category.GHOST
)
public class Fakelag extends Module {

    private final NumberValue<Integer> duration = new NumberValue<>("Lag Amount", 200, 0, 500, 20);
    private final ModeValue<String> mode = new ModeValue<>("FakeLag", new String[]{"Manual", "Always", "Dynamic"});

    private int ticks = 0;

    public Fakelag() {
        addSettings(duration, mode);
    }

    @Override
    public void onEnable() {
        PingSpoofUtil.enableOutbound(true, duration.getValue());
    }

    @Override
    public void onDisable() {
        PingSpoofUtil.disable();
    }

    @Listen
    public void onUpdate(UpdateEvent e) {
        switch (mode.getValue().toLowerCase()) {
            case "always":
                if (AttackUtil.inCombat) {
                    PingSpoofUtil.disable(false ,true);
                } else {
                    PingSpoofUtil.enableOutbound(true, duration.getValue());
                }
                break;
            case "dynamic":
                if (AttackUtil.inCombat || AttackUtil.getTarget(6.0, "FOV") == null) {
                    PingSpoofUtil.disable(false ,true);
                } else {
                    PingSpoofUtil.enableOutbound(true, duration.getValue());
                }
                break;
            default:
                break;
        }
    }

    @Listen
    public void onAttack(AttackEvent event) {
        if (mode.getValue().equalsIgnoreCase("manual")) {
            toggle();
        }
    }
}