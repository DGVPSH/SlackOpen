package cc.slack.features.modules.impl.movement.flights.impl.vulcan;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.features.modules.impl.movement.Flight;
import cc.slack.features.modules.impl.movement.flights.IFlight;
import cc.slack.start.Slack;
import cc.slack.utils.other.PrintUtil;
import cc.slack.utils.player.MovementUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class VulcanGhostFlight implements IFlight {
    // WIP
    static boolean isFlagged;
    static boolean cancelS08;
    static boolean spoofGround;
    static boolean flag;
    @Override
    public void onEnable(){
        isFlagged = false;
        cancelS08 = false;
        spoofGround = false;
    }

    @Override
    public void onPacket(PacketEvent event){
        if (event.getDirection() == PacketDirection.OUTGOING){
            if (event.getPacket() instanceof S08PacketPlayerPosLook){
                event.cancel();
                cancelS08 = true;
            }
        }
    }
    @Override
    public void onMotion(MotionEvent event){
        if (!spoofGround){
            event.setGround(true);
        }
    }
    @Override
    public void onMove(MoveEvent event){
        if (cancelS08 && !isFlagged && spoofGround){
            event.setY((mc.gameSettings.keyBindJump.isKeyDown() ? 1 * 3.32 :
                    mc.gameSettings.keyBindSneak.isKeyDown() ? -1 * 3.32 : 0));
            MovementUtil.setSpeed(event, Slack.getInstance().getModuleManager().getInstance(Flight.class).vanillaspeed.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (!flag){
            PrintUtil.print("You havent flag Vulcan. There fore you cant turn off this module");
        }
    }
    public void flag(){
        // flag da anticheat
    }
}
