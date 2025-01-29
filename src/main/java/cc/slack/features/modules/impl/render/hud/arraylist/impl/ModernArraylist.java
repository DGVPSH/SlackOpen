// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render.hud.arraylist.impl;

import cc.slack.features.modules.impl.render.Hud;
import cc.slack.start.Slack;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.impl.render.hud.arraylist.IArraylist;
import cc.slack.utils.font.Fonts;

import java.awt.*;
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

public class ModernArraylist implements IArraylist {

    private class Pair {
        String first;
        String second;
    }


    private final Map<Module, Boolean> moduleStates = new HashMap<>();
    List<ModernArraylist.Pair> modules = new ArrayList<>();

    private boolean dragging = false;
    private double dragX = 0, dragY = 0;
    public float x = -5;
    public float y = 7;

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
                            displayName += "§8 (§7" + mode + "§8)";
                            break;
                        case "[Mode]":
                            displayName += "§8 [§7" + mode + "§8]";
                            break;
                        case "<Mode>":
                            displayName += "§8 <§7" + mode + "§8>";
                            break;
                        case "| Mode":
                            displayName += "§8 | §7" + mode;
                            break;
                        case "-> Mode":
                            displayName += "§8 -> §7" + mode;
                            break;
                        case "- Mode":
                            displayName += "§8 - §7" + mode;
                            break;
                    }
                }


                ModernArraylist.Pair pair = new ModernArraylist.Pair();
                pair.first = displayName;
                pair.second = module.getName();

                modules.add(pair);
            }
        }

        switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).modernArraylistMode.getValue()) {
            case "Normal":
                modules.sort((a, b) -> Integer.compare(Fonts.sfRoundedBold18.getStringWidth(b.first), Fonts.sfRoundedBold18.getStringWidth(a.first)));
                break;
            case "Minimalist":
                modules.sort((a, b) -> Integer.compare(Fonts.sfRoundedBold18.getStringWidth(b.first), Fonts.sfRoundedBold18.getStringWidth(a.first)));
                break;
        }

    }

    @Override
    public void onRender(RenderEvent event) {
        if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistResetPos.getValue()) {
            x = -5;
            y = 7;
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

        for (ModernArraylist.Pair module : modules) {

            String displayName = module.first;


            switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).modernArraylistMode.getValue()) {
                case "Normal":
                    stringLength = Fonts.sfRoundedBold18.getStringWidth(displayName);
                    break;
                case "Minimalist":
                    stringLength = Fonts.sfRoundedBold18.getStringWidth(displayName);
                    break;
            }


            if (stringLength > longest) longest = stringLength;
            Module m = Slack.getInstance().getModuleManager().getModuleByName(module.second);
            double anim;

            if (m.isToggle()) {
                if (m.enabledTime.hasReached(250)) {
                    anim = 0;
                } else {
                    anim = 1 - (m.enabledTime.elapsed() / 250.0);
                }
            } else {
                anim = m.disabledTime.elapsed() / 250.0;
            }

            if (!m.isToggle() && m.disabledTime.hasReached(250)) {
                continue;
            }

            double ease = 0.0;
            if (m.isToggle()) {
                ease =  Math.pow(2 * Math.min(0.5, anim), 3);
            } else {
                ease = Math.pow(anim * 2, 0.4);
            }
            ease = 1 - Math.max(0, Math.min(1, ease)) * 1.4;

            switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).modernArraylistMode.getValue()) {
                case "Normal":
                    if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistBackground.getValue()) {
                        if (c > 0)
                            drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5) - 1, currentY - 5, stringLength + 8, Fonts.sfRoundedBold18.getHeight() , 0f, ColorUtil.getMaterial(true).getRGB());
                        drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5) - 1, currentY - 3, stringLength + 8, Fonts.sfRoundedBold18.getHeight() + 5, 4.0f, ColorUtil.getMaterial(true).getRGB());
                    }

                    switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistsidebar.getValue()) {
                        case "Modern":
                            drawSidebar((float) (x + event.getWidth() - stringLength * ease - 0 + stringLength ) + 1, currentY - 2, 1.5F, Fonts.sfRoundedBold18.getHeight() + 4, 1.0f, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                            break;
                        case "Classic":
                            if (c > 0)
                                drawSidebar((float) (x + event.getWidth() - stringLength * ease - 0 + stringLength ) + 1, currentY -5, 1.5F, Fonts.sfRoundedBold18.getHeight() + 5, 1.0f, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                            drawSidebar((float) (x + event.getWidth() - stringLength * ease - 0 + stringLength ) + 1, currentY -3, 1.5F, Fonts.sfRoundedBold18.getHeight() + 5, 1.0f, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                            break;
                        default:
                    }
                    Fonts.sfRoundedBold18.drawStringWithShadow(displayName, x + event.getWidth() - stringLength * ease - 3, currentY, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());

                    if (!m.isToggle()) {
                        ease =  Math.pow(2 * Math.max(0.5, anim) - 1, 2);
                    } else {
                        ease = Math.pow(anim, 3);
                    }
                    ease = Math.max(0, Math.min(1, ease));
                    currentY += (int) ((Fonts.sfRoundedBold18.getHeight() + 5) * (1 - ease));
                    c += 0.13;
                    break;
                case "Minimalist":
                    if (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistBackground.getValue()) {
                        drawRoundedRect((float) (x + event.getWidth() - stringLength * ease - 5), currentY - 2, stringLength + 7, Fonts.sfRoundedBold18.getHeight() + 3, 1.0f, ColorUtil.getMaterial(true).getRGB());
                    }

                    switch (Slack.getInstance().getModuleManager().getInstance(Hud.class).arraylistsidebar.getValue()) {
                        case "Modern":
                            drawSidebar((float) (x + event.getWidth() - stringLength * ease - 0 + stringLength ), currentY, 1.5F, Fonts.sfRoundedBold18.getHeight(), 1.0f, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                            break;
                        case "Classic":
                            drawSidebar((float) (x + event.getWidth() - stringLength * ease - 0 + stringLength ), currentY -2, 1.5F, Fonts.sfRoundedBold18.getHeight() + 3, 1.0f, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());                    break;
                        default:
                    }
                    Fonts.sfRoundedBold18.drawStringWithShadow(displayName, x + event.getWidth() - stringLength * ease - 3, currentY, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), c).getRGB());
                    currentY += (int) ((Fonts.sfRoundedBold18.getHeight() + 3) * Math.pow(Math.max(0, (ease + 0.2) / 1.2), 3));
                    c += 0.13;
                    break;
            }

            handleMouseInput(mouseX, mouseY, event.getWidth() - longest - x, y, longest, currentY - y, event.getWidth() - longest);
        }
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
        return "Modern";
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        RenderUtil.drawRoundedRect(x, y, x + width, y + height, radius, color);
    }

    private void drawSidebar(float x, float y, float width, float height, float radius, int color) {
        RenderUtil.drawRoundedRect(x, y, x + width, y + height, radius, color);
    }
}