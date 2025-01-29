// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render.hud.watermarks.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.impl.render.Hud;
import cc.slack.features.modules.impl.render.hud.watermarks.IWatermarks;
import cc.slack.start.Slack;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;

import java.awt.*;

public class BackgroundedWatermark implements IWatermarks {
    @Override
    public void onRender(RenderEvent event) {
        renderClassic(ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), 0.15).getRGB());
    }

    @Override
    public void onUpdate(UpdateEvent event) {

    }

    private void renderClassic(int themeColor) {

        RenderUtil.drawRoundedRect(5, 5, 9 + Fonts.sfRoundedBold20.getStringWidth("Slack Client") + 4, 9 + Fonts.sfRoundedBold24.getHeight() + 4, 2f, ColorUtil.getMaterial(true).getRGB());
        RenderUtil.drawRoundedRect(5, 5, 9 + Fonts.sfRoundedBold20.getStringWidth("Slack Client") + 4, 9 + 2, 2f, ColorUtil.getMaterial(false).getRGB());
        Fonts.sfRoundedBold20.drawString("Slack Client", 9, 11, -1);
    }

    @Override
    public String toString() {
        return "New";
    }
}
