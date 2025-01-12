package cc.slack.features.modules.impl.render;

import cc.slack.start.Slack;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.utilties.NameProtect;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.font.MCFontRenderer;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S40PacketDisconnect;
import org.lwjgl.input.Mouse;

import java.awt.*;

@ModuleInfo(
        name = "SessionInfo",
        category = Category.RENDER
)
public class SessionInfo extends Module {

    private final NumberValue<Float> xValue = new NumberValue<>("Xpos", 8.0F, 1.0F, 300.0F, 1F);
    private final NumberValue<Float> yValue = new NumberValue<>("Ypos", 160F, 1.0F, 300.0F, 1F);
    private final NumberValue<Integer> alphaValue = new NumberValue<>("Alpha", 170, 0, 255, 1);
    private final BooleanValue roundedValue = new BooleanValue("Rounded", false);
    public final BooleanValue resetPos = new BooleanValue("Reset Position", false);

    private final ModeValue<String> fontValue = new ModeValue<>("Font", new String[]{"Apple", "Poppins"});

    private boolean dragging = false;
    private float dragX = 0, dragY = 0;

    public long gameJoined;
    public long killAmount;
    public long currentTime;
    public long timeJoined;

    public SessionInfo() {
        addSettings(xValue, yValue, alphaValue, roundedValue, resetPos,fontValue);
    }

    @Override
    public void onEnable() {
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        currentTime = 0L;
        timeJoined = System.currentTimeMillis();
    }

    @Listen
    public void onPacket(PacketEvent event) {
        try {
            if (event.getPacket() instanceof S02PacketChat) {
                S02PacketChat packet = event.getPacket();
                String message = packet.getChatComponent().getUnformattedText();
                if (message.contains(mc.getSession().getUsername() + " wants to fight!") ||
                        message.contains(mc.getSession().getUsername() + " has joined") ||
                        message.contains(mc.getSession().getUsername() + " se ha unido") ||
                        message.contains(mc.getSession().getUsername() + " ha entrado")) {
                    ++gameJoined;
                }
                if (message.contains("by " + mc.getSession().getUsername()) ||
                        (message.contains(mc.thePlayer.getNameClear()) && message.contains("fue brutalmente asesinado por") ||
                                message.contains(mc.thePlayer.getNameClear()) && message.contains("fue empujado al vacío por") ||
                                message.contains(mc.thePlayer.getNameClear()) && message.contains("no resistió los ataques de") ||
                                message.contains(mc.thePlayer.getNameClear()) && message.contains("pensó que era un buen momento de morir a manos de") ||
                                message.contains(mc.thePlayer.getNameClear()) && message.contains("ha sido asesinado por"))) {
                    ++killAmount;
                }
            }
            if (event.getPacket() instanceof S40PacketDisconnect) {
                currentTime = 0L;
                timeJoined = System.currentTimeMillis();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public String getSessionLengthString() {
        long totalSeconds = (System.currentTimeMillis() - timeJoined) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;
        return (hours > 0L ? hours + "h " : "") + (minutes > 0L ? minutes + "m " : "") + seconds + "s";
    }

    @SuppressWarnings("unused")
    @Listen
    public void onRender(RenderEvent event) {
        if (resetPos.getValue()) {
            xValue.setValue(8F);
            yValue.setValue(160F);
            Slack.getInstance().getModuleManager().getInstance(SessionInfo.class).resetPos.setValue(false);
        }

        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        int x = xValue.getValue().intValue();
        int y = yValue.getValue().intValue();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        if (dragging) {
            xValue.setValue((float) (mouseX - dragX));
            yValue.setValue((float) (mouseY - dragY));
        }

        int width = 150;
        int height = 69;

        RenderUtil.drawRoundedRect(x, y, x + width, y + 17, 5, ColorUtil.getMaterial(false).getRGB()); // Top rectangle
        RenderUtil.drawRoundedRect(x, y + 16, x + width, y + 61, 0, ColorUtil.getMaterial(true).getRGB()); // Overlapping rectangle
        RenderUtil.drawRoundedRect(x, y + 55, x + width, y + 69, 5, ColorUtil.getMaterial(true).getRGB()); // Overlapping rectangle

        String sessionInfoText = "Session Info";
        float sessionInfoWidth = Fonts.sfRoundedBold20.getStringWidth(sessionInfoText);
        Fonts.sfRoundedBold20.drawString(sessionInfoText, x + (width - sessionInfoWidth) / 2, y + 5, -1);

        String timeElapsedText = getSessionLengthString();
        float contentStartX = x + 10;
        float timeY = y + 25;
        Fonts.sfRoundedBold28.drawString(timeElapsedText, contentStartX, timeY, -1);

        String killsText = "You have " + killAmount + " kills";
        float killsY = timeY + Fonts.sfRoundedBold24.getHeight() + 5;
        Fonts.sfRoundedBold18.drawString(killsText, contentStartX, killsY + 2, new Color(106, 106, 106).getRGB());

        String username = Slack.getInstance().getModuleManager().getInstance(NameProtect.class).isToggle() ? "Slack User" : mc.getSession().getUsername();
        String gamesWonText = "Username: " + username;
        Fonts.sfRoundedBold18.drawString(gamesWonText, contentStartX, killsY + Fonts.sfRoundedBold18.getHeight() + 5, new Color(106, 106, 106).getRGB());

        handleMouseInput(mouseX, mouseY, x, y, width, height);
    }

    private void handleMouseInput(int mouseX, int mouseY, int rectX, int rectY, int rectWidth, int rectHeight) {
        if (mc.currentScreen instanceof GuiChat) {
            if (Mouse.isButtonDown(0)) {
                if (!dragging) {
                    if (mouseX >= rectX && mouseX <= rectX + rectWidth &&
                            mouseY >= rectY && mouseY <= rectY + rectHeight) {
                        dragging = true;
                        dragX = mouseX - xValue.getValue();
                        dragY = mouseY - yValue.getValue();
                    }
                }
            } else {
                dragging = false;
            }
        }
    }
}
