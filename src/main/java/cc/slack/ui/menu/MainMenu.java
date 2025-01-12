package cc.slack.ui.menu;

import cc.slack.start.Slack;
import cc.slack.ui.altmanager.gui.GuiAccountManager;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.other.FileUtil;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import okhttp3.*;

public class MainMenu extends GuiScreen {
    private List<Particle> particles = new ArrayList<>();
    private final int particlesDensity = 2500;

    private final ResourceLocation imageResource = new ResourceLocation("slack/menu/menulogo.png");

    String debugMessage = "";
    TimeUtil dmTimer = new TimeUtil();

    public static String discordId = "";
    public static String idid = "";

    private static boolean lgi = false;

    public static int animY;
    private TimeUtil animTimer = new TimeUtil();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(new ResourceLocation("slack/menu/mainmenu.jpg"));
        drawModalRectWithCustomSizedTexture(0, 0,0,0, this.width, this.height, this.width, this.height);


        if (!animTimer.hasReached(700)) {
            animY = (int) (Math.pow(1 - (animTimer.elapsed() / 700.0), 4) * this.height * 0.7);
        } else {
            animY = 0;
        }

        Fonts.poppins18.drawString("Made by Dg636 and others with <3",
                width - 7 - Fonts.poppins18.getStringWidth("Made by Dg636 and others with <3"),
                height - 13, new Color(255, 255, 255, 150).getRGB());
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        RenderUtil.drawImage(imageResource, width / 2 - 23, height / 2 - 95 + animY, 46, 80);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        for (Particle particle : particles) {
            particle.update();
            particle.render(mc);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

    }



    @Override
    public void initGui() {

        int numberOfParticles = (this.width * this.height) / particlesDensity;
        particles.clear();
        for (int i = 0; i < numberOfParticles; i++) {
            particles.add(new Particle(this.width, this.height));
        }

        addButtons();
        super.initGui();
    }

    @Override
    protected void actionPerformedMenu(MainMenuButton buttonMenu) throws IOException {
        if (lgi) return;
        super.actionPerformedMenu(buttonMenu);

        switch (buttonMenu.id) {
            case 1:
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;

            case 2:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;

            case 3:
                mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 4:
                mc.displayGuiScreen(new GuiAccountManager(this));
                break;

            case 6:
                mc.shutdown();
                break;

            case 7:
                mc.displayGuiScreen(new MenuInfo());
                break;

            case 8:
                if (FileUtil.fetchHwid() == "f") {
                    setMsg(decodes("RmFpbGVkIHRvIGZldGNoIGh3aWQ="));
                    return;
                }
                setMsg(decodes("Q29waWVkIHRvIGNsaXBib2FyZA=="));
                GuiScreen.setClipboardString(FileUtil.fetchHwid());
                break;

            case 10:
                discordId = GuiScreen.getClipboardString();
                setMsg(decodes("U2V0IGRpc2NvcmQgSWQgdG86IA==") + discordId);
                return;

            case 12:
                FileUtil.showURL(Slack.getInstance().Website);
                break;

            case 13:
                FileUtil.showURL(Slack.getInstance().DiscordServer);
                break;
        }

        if (buttonMenu.id == 951) {
            lgi = true;
            animTimer.reset();
        } else {
            lgi = false;
        }
    }


    private void setMsg(String m) {
        dmTimer.reset();
        debugMessage = m;
    }

    private String decodes(String encodedInput) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedInput);
        return new String(decodedBytes);
    }

    private void addButtons() {
        this.menuList.add(new MainMenuButton(1, width/2 - 120, height / 2 + 10, 240, 20, decodes("U2luZ2xlUGxheWVy")));
        this.menuList.add(new MainMenuButton(2, width/2 - 120, height / 2 + 35, 240, 20, decodes("TXVsdGlQbGF5ZXI=")));


        this.menuList.add(new MainMenuButton(3, width/2 - 120, height / 2 + 60, 117, 20, decodes("U2V0dGluZ3M=")));
        this.menuList.add(new MainMenuButton(4, width/2 + 3, height / 2 + 60, 117, 20, decodes("QWx0IE1hbmFnZXI=")));
        this.menuList.add(new MainMenuButton(6, 5, height - 25, 60, 20, decodes("U2h1dGRvd24="), new Color(255, 0, 0)));
        this.menuList.add(new MainMenuButton(13, 70, height - 25, 100, 20, decodes("Sm9pbiBPdXIgRGlzY29yZA=="), new Color(86, 105, 247)));
        this.menuList.add(new MainMenuButton(7, 175, height - 25, 20, 20, "i"));
    }

}
