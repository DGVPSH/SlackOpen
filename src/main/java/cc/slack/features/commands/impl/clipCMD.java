package cc.slack.features.commands.impl;

import cc.slack.features.commands.api.CMD;
import cc.slack.features.commands.api.CMDInfo;
import cc.slack.utils.other.PrintUtil;
import net.minecraft.client.Minecraft;

@CMDInfo(
        name = "clip",
        alias = "vclip",
        description = "Clips you vertically."
)
public class clipCMD extends CMD {

    @Override
    public void onCommand(String[] args, String cmd) {
        try {
            if (args.length != 1) {
                PrintUtil.message("§cUsage: .vclip <blocks>");
                return;
            }

            double blocks = Double.parseDouble(args[0]);
            Minecraft.getMinecraft().thePlayer.setPosition(
                Minecraft.getMinecraft().thePlayer.posX,
                Minecraft.getMinecraft().thePlayer.posY + blocks,
                Minecraft.getMinecraft().thePlayer.posZ
            );
            
            PrintUtil.message("§fClipped §a" + blocks + " §fblocks " + 
                (blocks > 0 ? "up" : "down") + ".");
        } catch (NumberFormatException e) {
            PrintUtil.message("§cPlease enter a valid number.");
        } catch (Exception e) {
            PrintUtil.message("§cAn error occurred while attempting to clip.");
        }
    }
}
