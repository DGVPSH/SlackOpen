package cc.slack.features.commands.impl;

import cc.slack.features.commands.api.CMD;
import cc.slack.features.commands.api.CMDInfo;
import cc.slack.utils.other.PrintUtil;
import net.minecraft.client.Minecraft;

@CMDInfo(
    name = "session", 
    alias = "token", 
    description = "Copies session token to clipboard."
)
public class sessionCMD extends CMD {

    @Override
    public void onCommand(String[] args, String cmd) {
        try {
            String sessionID = Minecraft.getMinecraft().getSession().getToken();
            setClipboardString(sessionID);
            PrintUtil.message("§aSession ID copied to clipboard!");
        } catch (Exception e) {
            PrintUtil.message("§cAn error occurred while copying session ID.");
        }
    }

    private void setClipboardString(String text) {
        try {
            java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
        } catch (Exception e) {
            PrintUtil.message("§cFailed to copy to clipboard.");
        }
    }
}
