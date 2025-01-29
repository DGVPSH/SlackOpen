// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.start.Slack;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "Sprint",
        category = Category.MOVEMENT
)
public class Sprint extends Module {

    private final BooleanValue omniSprint = new BooleanValue("OmniSprint", false);

    public Sprint() {
        addSettings(omniSprint);
    }


    @Listen
    public void onUpdate(UpdateEvent e) {
        if (Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) return;
        if (Slack.getInstance().getModuleManager().getInstance(Speed.class).isToggle()) return;
        if (MovementUtil.isMoving()) mc.thePlayer.setSprinting(omniSprint.getValue());
    }

}
