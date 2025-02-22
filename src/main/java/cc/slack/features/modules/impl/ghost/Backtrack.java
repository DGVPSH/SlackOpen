// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.PingSpoofUtil;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;

import java.awt.*;

@ModuleInfo(
        name = "Backtrack",
        category = Category.GHOST
)
public class Backtrack extends Module {

    private final NumberValue<Integer> maxDelay = new NumberValue<>("Max Delay", 4, 1, 60, 1);
    private final NumberValue<Integer> backtrackTime = new NumberValue<>("Backtrack ticks", 20, 10, 60, 1);

    private int ticksSinceAttack = 0;
    private int backtrackTicks = 0;
    // “enabled” now means that ping spoofing is active (and will be turned on when the distance threshold is crossed)
    private boolean enabled = false;
    public EntityPlayer player;

    private int comboCounter = 0;
    private boolean sentHit = false;

    // These track the target’s “recorded” position from packets.
    private Vec3 realPos = new Vec3(0, 0, 0);
    private Vec3 lastPos = new Vec3(0, 0, 0);
    private Entity target;

    public Backtrack() {
        addSettings(maxDelay, backtrackTime);
    }

    @Override
    public void onEnable() {
        ticksSinceAttack = 0;
        backtrackTicks = 0;
        enabled = false;
    }

    @Listen
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            enabled = false;
            PingSpoofUtil.disable();
            return;
        }

        // --- New: Check for the distance transition ---
        if (target != null) {
            // Here we use lastPos as the “previous” recorded position and realPos as the “current”
            double previousDistance = mc.thePlayer.getDistance(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord);
            double currentDistance = mc.thePlayer.getDistance(realPos.xCoord, realPos.yCoord, realPos.zCoord);
            // When the target goes from under 3 blocks (previousDistance < 3) to at least 3 blocks away…
            if (previousDistance < 3 && currentDistance >= 3 && !enabled) {
                enabled = true;
                ticksSinceAttack = 0;
                backtrackTicks = backtrackTime.getValue();
                PingSpoofUtil.enableInbound(true, ticksSinceAttack * 20);
            }
        }
        // --- End new code ---

        if (enabled) {
            if (ticksSinceAttack < maxDelay.getValue()) {
                ticksSinceAttack++;
            }
            PingSpoofUtil.enableInbound(true, ticksSinceAttack * 20);
        }

        if (backtrackTicks > 0) {
            backtrackTicks--;
        } else {
            if (enabled) {
                PingSpoofUtil.disable();
                enabled = false;
            }
        }

        // Optionally, if you want to “cancel” ping spoofing when the target gets too close again:
        if (enabled && target != null) {
            double currentDistance = mc.thePlayer.getDistance(realPos.xCoord, realPos.yCoord, realPos.zCoord);
            if (currentDistance < 3) {
                PingSpoofUtil.disable();
                enabled = false;
            }
        }
    }

    @Listen
    public void onPacket(PacketEvent event) {
        final Packet packet = event.getPacket();

        if (event.getDirection() == PacketDirection.OUTGOING) {
            if (packet instanceof C02PacketUseEntity) {
                C02PacketUseEntity wrapper = (C02PacketUseEntity) packet;
                if (wrapper.getEntityFromWorld(mc.theWorld) instanceof EntityPlayer &&
                        wrapper.getAction() == C02PacketUseEntity.Action.ATTACK) {

                    Entity potentialTarget = wrapper.getEntityFromWorld(mc.theWorld);
                    // Instead of enabling ping spoof immediately, we update our target info.
                    if (target == null || !potentialTarget.getUniqueID().equals(target.getUniqueID())) {
                        target = potentialTarget;
                        realPos = target.getPositionVector();
                        lastPos = target.getPositionVector();
                        ticksSinceAttack = 0;
                        backtrackTicks = backtrackTime.getValue();
                    } else {
                        ticksSinceAttack = 0;
                    }
                    // Manage hit combo counter (unchanged from before)
                    if (potentialTarget.hurtResistantTime == 0 && !sentHit) {
                        sentHit = true;
                        comboCounter++;
                    } else if (potentialTarget.hurtResistantTime > 2) {
                        sentHit = false;
                    }

                    player = (EntityPlayer) potentialTarget;
                }
            }
        } else {
            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId())
                    ticksSinceAttack /= 3;
            } else if (packet instanceof S14PacketEntity) {
                S14PacketEntity entityPacket = (S14PacketEntity) packet;
                if (target != null && entityPacket.getEntity(mc.theWorld).getEntityId() == target.getEntityId()) {
                    lastPos = realPos;
                    realPos = new Vec3(
                            realPos.xCoord + entityPacket.getPosX() / 32D,
                            realPos.yCoord + entityPacket.getPosY() / 32D,
                            realPos.zCoord + entityPacket.getPosZ() / 32D
                    );
                }
            } else if (packet instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport teleportPacket = (S18PacketEntityTeleport) packet;
                if (target != null && teleportPacket.getEntityId() == target.getEntityId()) {
                    lastPos = realPos;
                    realPos = new Vec3(
                            teleportPacket.getX() / 32D,
                            teleportPacket.getY() / 32D,
                            teleportPacket.getZ() / 32D
                    );
                }
            }
        }
    }

    @Listen
    public void onRender(RenderEvent event) {
        if (event.state != RenderEvent.State.RENDER_3D) return;

        if (enabled) {
            RenderUtil.drawFilledAABB(
                    mc.thePlayer.getEntityBoundingBox()
                            .offset(-mc.thePlayer.posX, -mc.thePlayer.posY, -mc.thePlayer.posZ)
                            .offset(
                                    MathUtil.interpolate(realPos.xCoord, lastPos.xCoord, mc.timer.renderPartialTicks),
                                    MathUtil.interpolate(realPos.yCoord, lastPos.yCoord, mc.timer.renderPartialTicks),
                                    MathUtil.interpolate(realPos.zCoord, lastPos.zCoord, mc.timer.renderPartialTicks)
                            ),
                    new Color(255, 255, 255, 60).getRGB()
            );
        }
    }

    @Override
    public void onDisable() {
        PingSpoofUtil.disable(true, true);
    }

    @Override
    public String getMode() {
        return PingSpoofUtil.inboundDelay + "ms";
    }
}
