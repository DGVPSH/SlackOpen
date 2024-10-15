package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "CombatStrafe",
        category = Category.MOVEMENT
)
public class CombatStrafe extends Module {

    public CombatStrafe() {

    }

}
