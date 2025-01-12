package cc.slack.features.commands.impl;

import cc.slack.features.commands.api.CMD;
import cc.slack.features.commands.api.CMDInfo;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.other.PrintUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.play.client.*;

@CMDInfo(
        name = "send",
        alias = "send",
        description = "Sends a packet"
)
public class sendCMD extends CMD {

    @Override
    public void onCommand(String[] args, String command) {
        if (args.length != 1) {
            PrintUtil.message("§cInvalid use of arguments. Format: .send c__");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "c0a":
                PacketUtil.send(new C0APacketAnimation());
                break;
            case "c0b":
                PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.OPEN_INVENTORY));
                break;
            case "c0c":
                PacketUtil.send(new C0CPacketInput());
                break;
            case "c0d":
                PacketUtil.send(new C0DPacketCloseWindow());
                break;
            case "c0e":
                PacketUtil.send(new C0EPacketClickWindow());
                break;
            case "c0f":
                PacketUtil.send(new C0FPacketConfirmTransaction());
                break;
            case "c00":
                PacketUtil.send(new C00PacketKeepAlive());
                break;
            case "c08":
                PacketUtil.send(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                break;
            case "block1_9":
                PacketUtil.block1_9();
                break;
            case "c07":
                PacketUtil.releaseUseItem(true);
        }
    }

}
