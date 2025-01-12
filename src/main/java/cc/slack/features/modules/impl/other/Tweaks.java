// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.other;

import cc.slack.start.Slack;
import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "Tweaks",
        category = Category.OTHER
)
public class Tweaks extends Module {
    public final BooleanValue autoRespawn = new BooleanValue("Auto Respawn", true);
    public final BooleanValue biggerChat = new BooleanValue("Bigger Chat", true);
    public final BooleanValue customTitle = new BooleanValue("Custom Title", true);
    private final BooleanValue fullbright = new BooleanValue("Full Bright", true);
    public final BooleanValue modernKeybinding = new BooleanValue("Modern Keybindings", true);
    public final BooleanValue noachievement = new BooleanValue("No Achievement", true);
    public final BooleanValue noChatBack = new BooleanValue("No Chat Background", true);
    public final BooleanValue noClickDelay = new BooleanValue("No Click Delay", true);
    public final BooleanValue noExpBar = new BooleanValue("No XP Bar", false);
    public final BooleanValue noPumpkin = new BooleanValue("No Pumpkin", true);
    public final BooleanValue nobosshealth = new BooleanValue("No Boss Health", false);
    public final BooleanValue noSkinValue = new BooleanValue("No Custom Skins", false);
    public final BooleanValue noTickInvisValue = new BooleanValue("Don't Tick invisibles", false);
    public final NumberValue<Integer> jumpDelay = new NumberValue<>("Jump Delay", 3, 0, 6, 1);

    float prevGamma = -1F;
    boolean wasGUI = false;

    public String status = "";

    public Tweaks() {
        super();
        addSettings(autoRespawn, biggerChat, customTitle, fullbright, modernKeybinding, noachievement, noChatBack, noClickDelay, noExpBar, noPumpkin, nobosshealth, noSkinValue, noTickInvisValue, jumpDelay);
    }

    @Override
    public void onEnable() {prevGamma = mc.gameSettings.gammaSetting;}

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate (UpdateEvent event) {

        if (autoRespawn.getValue()) {
            if (!mc.thePlayer.isEntityAlive()) {
                mc.thePlayer.respawnPlayer();
            }
        }

        if (fullbright.getValue()) {
            if (mc.gameSettings.gammaSetting <= 1000f) mc.gameSettings.gammaSetting++;
        } else {
            if (mc.gameSettings.gammaSetting >= 1f) mc.gameSettings.gammaSetting--;
            if (prevGamma != -1f) {
                mc.gameSettings.gammaSetting = prevGamma;
                prevGamma = -1f;
            }
        }

        if (modernKeybinding.getValue()) {
            if (mc.getCurrentScreen() == null) {
                if (wasGUI) {
                    MovementUtil.updateBinds();
                }
                wasGUI = false;
            } else {
                wasGUI = true;
            }
        }

        if (noClickDelay.getValue()) mc.leftClickCounter = 0;
    }

    @SuppressWarnings("unused")
    @Listen
    public void onTick (TickEvent event) {
        if (Slack.getInstance().getModuleManager().getInstance(RichPresence.class).started.get()) {
            status = "ON";
        } else {
            status = "OFF";
        }
    }

    @Override
    public void onDisable() {
        if (prevGamma == -1f) return;
        mc.gameSettings.gammaSetting = prevGamma;
        prevGamma = -1f;
        if (mc.gameSettings.gammaSetting > 1) {
            mc.gameSettings.gammaSetting = 1;
        }
    }
}
