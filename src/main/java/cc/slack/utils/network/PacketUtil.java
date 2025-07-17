package cc.slack.utils.network;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.other.PrintUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import net.minecraft.util.Vec3;

import java.sql.Types;

public final class PacketUtil implements IMinecraft {

    private static boolean swing;

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

    public static boolean handlePacket(PacketEvent event) {
        Packet packet = event.getPacket();

        if (ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_9)) {

            if (packet instanceof C02PacketUseEntity) {
                C02PacketUseEntity c02 = (C02PacketUseEntity) packet;
                switch (((C02PacketUseEntity) packet).getAction()) {
                    case ATTACK: {
                        return false;
                    }
                    case INTERACT_AT: {
                        Vec3 hitVec = c02.getHitVec();
                        Entity entity = c02.getEntityFromWorld(mc.theWorld);

                        if (hitVec == null || entity == null) {
                            break;
                        }

                        if (entity instanceof EntityItemFrame || entity instanceof EntityFireball) {
                            break;
                        }

                        float w = entity.width;
                        float h = entity.height;

                        ((C02PacketUseEntity) packet).setHitVec(
                            new Vec3(
                                    MathUtil.clamp(hitVec.xCoord, w / -2.0, w/ 2.0),
                                    MathUtil.clamp(hitVec.yCoord, 0, h),
                                    MathUtil.clamp(hitVec.zCoord, w / -2.0, w/ 2.0)
                            )
                        );
                        break;
                    }
                }
            }

            if (packet instanceof C09PacketHeldItemChange) {
                return false;
            }

            if (swing) {
                UserConnection c = Via.getManager().getConnectionManager().getConnections().iterator().next();
                PacketWrapper s = PacketWrapper.create(ServerboundPackets1_9.ANIMATION, c);
                s.write(Type.VAR_INT, 0);
                try {
                    s.sendToServer(Protocol1_9To1_8.class);
                } catch (Exception ignored) {
                    PrintUtil.printAndMessage("failed");
                }
                swing = false;
            }

            if (packet instanceof C0APacketAnimation) {
                swing = true;
                return true;
            }

            if (packet instanceof C08PacketPlayerBlockPlacement && ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_11)) {
                C08PacketPlayerBlockPlacement c08 = (C08PacketPlayerBlockPlacement) packet;
                if (c08.getPlacedBlockDirection() != 255) {
                    UserConnection c = Via.getManager().getConnectionManager().getConnections().iterator().next();
                    PacketWrapper s = PacketWrapper.create(ServerboundPackets1_12.USE_ITEM, c);
                    s.write(Type.POSITION, new Position(
                            c08.getPosition().getX(),
                            c08.getPosition().getY(),
                            c08.getPosition().getZ()
                    ));
                    s.write(Type.VAR_INT, c08.getPlacedBlockDirection());
                    s.write(Type.VAR_INT, 0);
                    s.write(Type.FLOAT, c08.facingX);
                    s.write(Type.FLOAT, c08.facingY);
                    s.write(Type.FLOAT, c08.facingZ);
                    try {
                        s.sendToServer(Protocol1_11To1_10.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
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
