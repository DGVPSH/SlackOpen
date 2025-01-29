// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render;

import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.start.Slack;
import cc.slack.utils.drag.DragUtil;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import java.awt.*;
import java.util.ArrayList;

@ModuleInfo(
        name = "KeyStrokes",
        category = Category.RENDER
)
public class KeyStrokes extends Module {

    private final BooleanValue clientTheme = new BooleanValue("Client Theme", true);
    private final NumberValue<Integer> alphaValue = new NumberValue<>("Alpha", 90,0,255,1);
    public final BooleanValue resetPos = new BooleanValue("Reset Position", false);
    private final NumberValue<Double> posX = new NumberValue<>("Xpos", 70.0, 0.0, 300.0, 1.0);
    private final NumberValue<Double> posY = new NumberValue<>("Ypos", 80.0, 0.0, 300.0, 1.0);

    public KeyStrokes() {
        addSettings(clientTheme, alphaValue, resetPos, posX, posY);
    }

    private final ArrayList<Boolean> enabled = new ArrayList<>(5);
    private final ArrayList<TimeUtil> downTime = new ArrayList<>(5);
    private final ArrayList<TimeUtil> upTime = new ArrayList<>(5);
    private final ArrayList<KeyBinding> binds = new ArrayList<>(5);

    private Color c = new Color(40, 40, 40, 80);

    @Listen
    public void onRender(RenderEvent event) {
        if (resetPos.getValue()) {
            posX.setValue(70d);
            posY.setValue(80d);
            Slack.getInstance().getModuleManager().getInstance(KeyStrokes.class).resetPos.setValue(false);
        }
        if (event.getState() != RenderEvent.State.RENDER_2D) return;
        if (enabled.size() < 5) {
            for (int i = enabled.size(); i < 5; i++) {
                enabled.add(false);
                downTime.add(new TimeUtil());
                upTime.add(new TimeUtil());

                binds.clear();
                binds.add(mc.gameSettings.keyBindForward);
                binds.add(mc.gameSettings.keyBindRight);
                binds.add(mc.gameSettings.keyBindBack);
                binds.add(mc.gameSettings.keyBindLeft);
                binds.add(mc.gameSettings.keyBindJump);
            }
        }

        for (int i = 0; i < 5; i++) {
            KeyBinding k = binds.get(i);
            if (mc.currentScreen == null)
                if (GameSettings.isKeyDown(k)) {
                    if (!enabled.get(i)) {
                        downTime.get(i).reset();
                        enabled.remove(i);
                        enabled.add(i, true);
                    }
                } else {
                    if (enabled.get(i)) {
                        upTime.get(i).reset();
                        enabled.remove(i);
                        enabled.add(i, false);
                    }
                }
        }

        c = ColorUtil.getMaterial(true);
        if (clientTheme.getValue()) {
            c = ColorUtil.getColor();
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alphaValue.getValue());
        }

        litteSquare(0, 0, 1f);
        litteSquare(-35, 0, 1f);
        litteSquare(35, 0, 1f);
        litteSquare(0, -35, 1f);
        spaceBar(0, 35, 1f);

        c = ColorUtil.getMaterial(false);
        if (clientTheme.getValue()) {
            c = ColorUtil.getColor();
            c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alphaValue.getValue());
        }

        litteSquare(0, 0, getScale(2));
        litteSquare(-35, 0, getScale(3));
        litteSquare(35, 0, getScale(1));
        litteSquare(0, -35, getScale(0));
        spaceBar(0, 35, getScale(4));

        int h = Fonts.apple24.getHeight() / 2;

        Fonts.apple24.drawCenteredStringWithShadow("S", posX.getValue().floatValue(), posY.getValue().floatValue() - h, new Color(255,255,255).getRGB());
        Fonts.apple24.drawCenteredStringWithShadow("W", posX.getValue().floatValue(), posY.getValue().floatValue() - 35 - h, new Color(255,255,255).getRGB());
        Fonts.apple24.drawCenteredStringWithShadow("D", posX.getValue().floatValue() + 35, posY.getValue().floatValue() - h, new Color(255,255,255).getRGB());
        Fonts.apple24.drawCenteredStringWithShadow("A", posX.getValue().floatValue() - 35, posY.getValue().floatValue() - h, new Color(255,255,255).getRGB());
    }

    @Override
    public DragUtil getPosition() {
        double[] pos = DragUtil.setScaledPosition(posX.getValue(), posY.getValue());
        return new DragUtil(pos[0] - 50, pos[1] - 50, 100, 100, 1);
    }

    @Override
    public void setXYPosition(double x, double y) {
        posX.setValue(x + 50);
        posY.setValue(y + 50);
    }

    private void litteSquare(int x, int y, float scale) {
        RenderUtil.drawRoundedRect(
                posX.getValue().floatValue() + x - 15 * scale,
                posY.getValue().floatValue() + y - 15 * scale,
                posX.getValue().floatValue() + x + 15 * scale,
                posY.getValue().floatValue() + y + 15 * scale,
                2 + scale * 3,
               c.getRGB() );
    }

    private void spaceBar (int x, int y, float scale) {
        RenderUtil.drawRoundedRect(
                posX.getValue().floatValue() + x - 20 - 30 * scale,
                posY.getValue().floatValue() + y - 1 - 14 * scale,
                posX.getValue().floatValue() + x + 20 + 30 * scale,
                posY.getValue().floatValue() + y + 1 + 14 * scale,
                2 + scale * 3,
                c.getRGB() );
    }

    private float getScale(int i) {
        if (enabled.get(i)) {
            return Math.max(getScale(i, true), getScale(i, false));
        } else {
            return Math.min(getScale(i, true), getScale(i, false));
        }
    }

    private float getScale(int i, boolean enabled) {
        if (enabled) {
            if (downTime.get(i).hasReached(200)) {
                return 1f;
            } else {
                return 1 - easing(200 - downTime.get(i).elapsed());
            }
        } else {
            if (upTime.get(i).hasReached(200)) {
                return 0f;
            } else {
                return 1 - easing(upTime.get(i).elapsed());
            }
        }
    }

    private float easing(Long time) {
        return (float) Math.pow(time/250.0, 3);
    }

}
