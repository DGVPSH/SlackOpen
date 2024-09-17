package cc.slack.features.modules.impl.utilties;

import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;

import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "TNTHelper",
        category = Category.UTILITIES
)
public class TNTHelper extends Module {

    double damage = 0;

    @Override
    public void onEnable() {
        damage = 0;
    }

    @Listen
    public void onRender (RenderEvent event) {
        if (event.getState() == RenderEvent.State.RENDER_3D) {
            damage = 0;
            RenderUtil.drawTNTExplosionRange(damage);
        }
    }

}
