package cc.slack.features.modules.impl.render.hud.watermarks.impl;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.impl.render.Hud;
import cc.slack.features.modules.impl.render.hud.watermarks.IWatermarks;
import cc.slack.start.Slack;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.font.MCFontRenderer;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class BackgroundedWatermark implements IWatermarks {
    private double posX = -1D;
    private double posY = -1D;
    private int x = 0;
    private int y = 0;

    private boolean dragging = false;
    private double dragX = 0, dragY = 0;

    private String cachedFontName;
    private MCFontRenderer fontRenderer20 = Fonts.getFontRenderer("Apple", 20);
    private MCFontRenderer fontRenderer18 = Fonts.getFontRenderer("Apple", 20);




    @Override
    public void onRender(RenderEvent event) {
        renderBackgroundedRound(
                ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), 0.15).getRGB(),
                new Color(255, 255, 255, 255).getRGB(),
                new Color(1, 1, 1, 100).getRGB()
        );
    }

    @Override
    public void onUpdate(UpdateEvent event) {
    }

    private void renderBackgroundedRound(int themeColor, int whiteColor, int backgroundColor) {
        drawBackgroundedText(themeColor, whiteColor, backgroundColor);
    }

    private void drawBackgroundedText(int themeColor, int whiteColor, int backgroundColor) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (posX == -1 || posY == -1) {
            posX = 2;
            posY = 2;
        }

        int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

        if (dragging) {
            posX = mouseX - dragX;
            posY = mouseY - dragY;
        }

        x = (int) posX;
        y = (int) posY;

        String text1 = "S";
        String text2 = "lack ";
        String text3 = " | ";
        String text4 = (Minecraft.getMinecraft().isIntegratedServerRunning()) ? "SinglePlayer" : PlayerUtil.getRemoteIp();
        String text5 = " | ";
        String text6 = Minecraft.getDebugFPS() + " FPS";

        int width1 = fontRenderer20.getStringWidth(text1);
        int width2 = fontRenderer20.getStringWidth(text2);
        int width3 = fontRenderer18.getStringWidth(text3);
        int width4 = fontRenderer18.getStringWidth(text4);
        int width5 = fontRenderer18.getStringWidth(text5);
        int width6 = fontRenderer18.getStringWidth(text6);

        int totalWidth = width1 + width2 + width3 + width4 + width5 + width6 + 4;

        int rectWidth = totalWidth + 10;
        int rectHeight = 15;

        int rectX = x;
        int rectY = y;

        drawRect(rectX, rectY, rectX + rectWidth, rectY + rectHeight, backgroundColor);


        int textX = rectX + 4;
        int textY = rectY + 5;
        fontRenderer20.drawStringWithShadow(text1, textX, textY, themeColor);
        textX += width1 + 1;
        fontRenderer20.drawStringWithShadow(text2, textX, textY, whiteColor);
        textX += width2 + 1;
        fontRenderer18.drawStringWithShadow(text3, textX, textY, whiteColor);
        textX += width3 + 1;
        fontRenderer18.drawStringWithShadow(text4, textX, textY, whiteColor);
        textX += width4 + 1;
        fontRenderer18.drawStringWithShadow(text5, textX, textY, whiteColor);
        textX += width5 + 1;
        fontRenderer18.drawStringWithShadow(text6, textX, textY, whiteColor);

    }

    private void drawRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }
    @Override
    public String toString() {
        return "Backgrounded";
    }
}
