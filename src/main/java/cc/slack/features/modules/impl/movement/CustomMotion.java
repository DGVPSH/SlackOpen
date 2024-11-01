package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "Custom Motion",
        category = Category.MOVEMENT
)
public class CustomMotion extends Module {

    private final NumberValue<Float> strength = new NumberValue<>("Speed", 0.3F, 0F, 1F, 0.01F);

    public CustomMotion() {
        addSettings(strength);
    }

    @SuppressWarnings("unused")
    @Listen
    public void onStrafe (StrafeEvent event) {
        MovementUtil.strafe(strength.getValue());
    }

}
