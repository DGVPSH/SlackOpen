package cc.slack.features.modules.impl.render.hud.watermarks.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.impl.render.Hud;
import cc.slack.features.modules.impl.render.hud.watermarks.IWatermarks;
import cc.slack.start.Slack;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.render.ColorUtil;

public class ClassicWatermark implements IWatermarks {
    @Override
    public void onRender(RenderEvent event) {
        renderClassic(ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), 0.15).getRGB());
    }

    @Override
    public void onUpdate(UpdateEvent event) {

    }

    private void renderClassic(int themeColor) {
        Fonts.apple24.drawStringWithShadow("S", 3.4, 4, themeColor);
        Fonts.apple24.drawStringWithShadow("lack", 11, 4, -1);
    }

    @Override
    public String toString() {
        return "Classic";
    }
}
