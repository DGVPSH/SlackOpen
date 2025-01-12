package cc.slack.utils.network;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.other.PrintUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;

public final class PacketUtil implements IMinecraft {

    public static void send(Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void send(Packet<?> packet, int iterations) {
        for (int i = 0; i < iterations; i++) send(packet);
    }

    public static void sendNoEvent(Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
    }
    public static void sendNoEvent(Packet<?> packet, int iterations) {
        for (int i = 0; i < iterations; i++) sendNoEvent(packet);
    }

    public static void receive(Packet<?> packet) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketDirection.INCOMING);
        if (packetEvent.call().isCanceled()) return;
        if (BlinkUtil.handlePacket(packetEvent)) return;

        packetEvent.getPacket().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
    }

    public static void receiveNoEvent(Packet<?> packet) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketDirection.INCOMING);
        try {
            packetEvent.getPacket().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
        } catch (Exception e) {
            try {
                PrintUtil.printAndMessage("Failed to process packet " + packet.toString());
            } catch (Exception ignored) {

            }
        }
    }

    public static void sendBlocking(boolean callEvent, boolean place) {
        C08PacketPlayerBlockPlacement packet;
        packet = place ? new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0.0f, 0.0f, 0.0f) : new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem());
        if (callEvent) {
            PacketUtil.send(packet);
        } else {
            PacketUtil.sendNoEvent(packet);
        }
    }

    public static void sendCriticalPacket(double yOffset, boolean ground) {
        PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + yOffset, mc.thePlayer.posZ, ground));
    }

    public static void releaseUseItem(boolean callEvent) {
        C07PacketPlayerDigging packet = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        if (callEvent) {
            PacketUtil.send(packet);
        } else {
            PacketUtil.sendNoEvent(packet);
        }
    }

    public static void switchItemToOffhand() {
        try {
            ConnectionManager connectionManager = Via.getManager().getConnectionManager();
            UserConnection userConnection = connectionManager.getConnectedClient(mc.thePlayer.getUniqueID());
            final PacketWrapper packet = PacketWrapper.create(ServerboundPackets1_9.PLAYER_DIGGING, userConnection);
            packet.write(Type.VAR_INT, 6); // Action ID for swap item with offhand
            packet.write(Type.POSITION, new Position(0, 0, 0)); // Placeholder for position
            packet.write(Type.BYTE, (byte) 0); // Face
            packet.sendToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void block1_9() {
        try {
            ConnectionManager connectionManager = Via.getManager().getConnectionManager();
            UserConnection userConnection = connectionManager.getConnectedClient(mc.thePlayer.getUniqueID());
            final PacketWrapper packet = PacketWrapper.create(ServerboundPackets1_12.USE_ITEM, userConnection);
            packet.write(Type.VAR_INT, 1);
            packet.sendToServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
