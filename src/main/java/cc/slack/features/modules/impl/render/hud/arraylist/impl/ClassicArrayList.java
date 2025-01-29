// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render.hud.arraylist.impl;

import cc.slack.start.Slack;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.impl.render.Hud;
import cc.slack.features.modules.impl.render.hud.arraylist.IArraylist;
import cc.slack.utils.font.Fonts;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClassicArrayList implements IArraylist {

    private class Pair {
        String first;
        String second;
    }

    private final Map<Module, Boolean> moduleStates = new HashMap<>();
    List<Pair> modules = new ArrayList<>();

    private boolean dragging = false;
    private double dragX = 0, dragY = 0;
    public float x = 0;
    public float y = 3;

    @Override
    public void onUpdate(UpdateEvent event) {

        modules.clear();
        for (Module module : Slack.getInstance().getModuleManager().getModules()) {
            if (!module.render) continue;
            boolean wasEnabled = moduleStates.getOrDefault(module, false);
            boolean isEnabled = module.isToggle();

            moduleStates.put(module, isEnabled);

            if (module.isToggle() || !module.disabledTime.hasReached(300)) {
                String displayName = module.getDisplayName();
                String mode = module.getMode();
                String key = Keyboard.getKeyName(module.getKey());
                if (mode != null && !mode.isEmpty() && Slack.getInstance().getModuleManager().getInstance(Hud.class).tags.getValue()) {
                    switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).tagsMode.getValue()) {
                        case "(Mode)":
                            displayName += "§7 (" + mode + ")";
                            break;
                        case "[Mode]":
                            displayName += "§7 [" + mode + "]";
                            break;
                        case "<Mode>":
                            displayName += "§7 <" + mode + ">";
                            break;
                        case "| Mode":
                            displayName += "§7 | " + mode;
                            break;
                        case "-> Mode":
                            displayName += "§7 -> " + mode;
                            break;
                        case "- Mode":
                            displayName += "§7 - " + mode;
                            break;
                    }
                }

                Pair pair = new Pair();
                pair.first = displayName;
                pair.second = module.getName();

                modules.add(pair);
            }
        }
        switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistFont.getValue()) {
            case "Apple":
                modules.sort((a, b) -> Integer.compare(Fonts.sfRoundedBold18.getStringWidth(b.first), Fonts.sfRoundedBold18.getStringWidth(a.first)));
                break;
            case "Poppins":
                modules.sort((a, b) -> Integer.compare(Fonts.poppins18.getStringWidth(b.first), Fonts.poppins18.getStringWidth(a.first)));
                break;
            case "Roboto":
                modules.sort((a, b) -> Integer.compare(Fonts.roboto18.getStringWidth(b.first), Fonts.sfRoundedBold18.getStringWidth(a.first)));
                break;
        }
    }

    @Override
    public void onRender(RenderEvent event) {
        if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistResetPos.getValue()) {
            x = 0;
            y = 3;
            Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistResetPos.setValue(false);
        }

        int currentY = (int) y;
        double c = 0;
        int stringLength = 0;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

        if (dragging) {
            x = (float) (mouseX - dragX);
            y = (float) (mouseY - dragY);
        }

        int longest = 0;

        for (Pair module : modules) {
            switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistFont.getValue()) {
                case "Apple":
                    stringLength = Fonts.sfRoundedBold18.getStringWidth(module.first);
                    break;
                case "Poppins":
                    stringLength = Fonts.poppins18.getStringWidth(module.first);
                    break;
                case "Roboto":
                    stringLength = Fonts.roboto18.getStringWidth(module.first);
                    break;
            }
            if (stringLength > longest) longest = stringLength;
            Module m = Slack.getInstance().getModuleManager().getModuleByName(module.second);
            double ease;

            if (m.isToggle()) {
                if (m.enabledTime.hasReached(250)) {
                    ease = 0;
                } else {
                    ease = Math.pow(1 - (m.enabledTime.elapsed() / 250.0), 1);
                }
            } else {
                ease = Math.pow(m.disabledTime.elapsed() / 250.0, 1);
            }

            ease = 1 - 1.2 * ease;

            switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistFont.getValue()) {
                case "Apple":
                    if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistBackground.getValue()) {
                        drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5), currentY - 2, stringLength + 5, Fonts.poppins18.getHeight(), 1.0f, 0x80000000);
                    }
                    Fonts.sfRoundedBold18.drawStringWithShadow(module.first, x + event.getWidth() - stringLength * ease - 3, currentY, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                    currentY += (int) ((Fonts.sfRoundedBold18.getHeight() + 3) * Math.max(0, (ease + 0.2) / 1.2));
                    c += 0.13;
                    break;
                case "Poppins":
                    if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistBackground.getValue()) {
                        drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5), currentY - 2, stringLength + 5, Fonts.poppins18.getHeight() + 3, 1.0f, 0x80000000);
                    }
                    Fonts.poppins18.drawStringWithShadow(module.first, x + event.getWidth() - stringLength * ease - 3, currentY, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                    currentY += (int) ((Fonts.poppins18.getHeight() + 3) * Math.max(0, (ease + 0.2) / 1.2));
                    c += 0.15;
                    break;
                case "Roboto":
                    if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistBackground.getValue()) {
                        drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5), currentY - 2, stringLength + 5, Fonts.roboto18.getHeight() + 3, 1.0f, 0x80000000);
                    }
                    Fonts.roboto18.drawStringWithShadow(module.first, x + event.getWidth() - stringLength * ease - 3, currentY, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                    currentY += (int) ((Fonts.roboto18.getHeight() + 3) * Math.max(0, (ease + 0.2) / 1.2));
                    c += 0.13;
                    break;
            }
        }

        handleMouseInput(mouseX, mouseY, event.getWidth() - longest - x, y, longest, currentY - y, event.getWidth() - longest);
    }

    private void handleMouseInput(int mouseX, int mouseY, float rectX, float rectY, float rectWidth, float rectHeight, float e) {
        if (Mouse.isButtonDown(0) && mc.currentScreen instanceof GuiChat) {
            if (!dragging) {
                if (mouseX >= rectX && mouseX <= rectX + rectWidth &&
                        mouseY >= rectY && mouseY <= rectY + rectHeight) {
                    dragging = true;
                    dragX = mouseX + rectX - e;
                    dragY = mouseY - rectY;
                }
            }
        } else {
            dragging = false;
        }
    }

    @Override
    public String toString() {
        return "Classic";
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        RenderUtil.drawRoundedRect(x, y, x + width, y + height, radius, color);
    }
}