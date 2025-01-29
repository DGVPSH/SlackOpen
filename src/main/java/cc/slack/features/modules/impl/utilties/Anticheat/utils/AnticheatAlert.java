// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.utilties.Anticheat.utils;
import cc.slack.features.modules.impl.utilties.AntiCheat;
import cc.slack.start.Slack;
import cc.slack.utils.client.IMinecraft;
import net.minecraft.util.ChatComponentText;

public final class AnticheatAlert implements IMinecraft {

    public static void debugMessage(String message) {
        if (Slack.getInstance().getModuleManager().getInstance(AntiCheat.class).debug.getValue())
            mc.thePlayer.addChatMessage(new ChatComponentText("§f[§cANTICHEAT DEBUG§f] §e" + message));
    }

    public static void flag(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§cANTICHEAT » §f" + message));
    }
}

