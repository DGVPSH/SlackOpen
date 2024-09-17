package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "RealLag",
        category = Category.GHOST
)
public class RealLag extends Module {

    private final NumberValue<Integer> duration = new NumberValue<>("Lag Ticks", 5, 0, 40, 1);
    private final BooleanValue reversed = new BooleanValue("Reversed", false);
    private final BooleanValue reverseBlink = new BooleanValue("Reversed Blink", false);

    private int ticks = 0;

    public RealLag() {
        addSettings(duration,  reversed, reverseBlink);
    }

    @Override
    public void onEnable() {
        if (!reversed.getValue()) {
            PlayerUtil.lag(duration.getValue() * 50);
            toggle();
        } else {
            ticks = -1;
            if (reverseBlink.getValue()) BlinkUtil.enable(false, true);
        }
    }

    @Listen
    public void onTick(TickEvent e) {
        if (!reversed.getValue()) return;

        if (ticks == -1) {
            mc.timer.elapsedTicks += duration.getValue();
        }
        ticks ++;

        if (ticks > duration.getValue()) {
            mc.gameSettings.keyBindJump.pressed = false;
            mc.gameSettings.keyBindSprint.pressed = false;
            mc.gameSettings.keyBindForward.pressed = false;
            mc.gameSettings.keyBindRight.pressed = false;
            mc.gameSettings.keyBindBack.pressed = false;
            mc.gameSettings.keyBindLeft.pressed = false;
            e.cancel();
        }

        if (ticks > duration.getValue() * 2) {
            BlinkUtil.disable();
            MovementUtil.updateBinds();
            toggle();
        }
    }

    @Listen
    public void onRender(RenderEvent e) {
        if (!reversed.getValue()) return;

        if (ticks > duration.getValue() + 1) {
            mc.timer.renderPartialTicks = 1;
        }
    }

}
