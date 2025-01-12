package cc.slack.utils.other;

import cc.slack.utils.client.IMinecraft;
import net.minecraft.util.ChatComponentText;

public final class PrintUtil implements IMinecraft {

    public static void print(Object message) {
        System.out.println("[Slack] " + message.toString());
    }

    public static void debugMessage(Object message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§f[§cDEBUG§f] §e" + message.toString()));
    }

    public static void message(Object message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§cSlack » §f" + message.toString()));
    }

    public static void msgNoPrefix(Object message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§f" + message.toString()));
    }

    public static void printAndMessage(Object message) {
        print(message.toString());
        message(message.toString());
    }
}
