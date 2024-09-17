// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.player.nofalls.specials;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.impl.player.nofalls.INoFall;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.AttackUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

public class HypixelBlinkNofall implements INoFall {


    boolean spoof;

    @Override
    public void onEnable() {

        spoof = false;
    }

    @Override
    public void onDisable() {
        BlinkUtil.disable();
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer.isDead) {
            BlinkUtil.disable();
        }

        if (mc.thePlayer.onGround && spoof) {
            spoof = false;
            BlinkUtil.disable();
        } else if (mc.thePlayer.offGroundTicks == 1 && mc.thePlayer.motionY < 0 && mc.thePlayer.fallDistance > 0 && !isOverVoid() && isDamageFall() && !AttackUtil.inCombat) {
            spoof = true;
            BlinkUtil.enable(false, true);
        }

    }

    @Override
    public void onPacket(PacketEvent event) {
        if (spoof && event.getPacket() instanceof C03PacketPlayer) {
            ((C03PacketPlayer) event.getPacket()).onGround = true;
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void onRender(RenderEvent e) {
        if (e.state != RenderEvent.State.RENDER_2D) return;
        if (!spoof) return;

        ScaledResolution sr = mc.getScaledResolution();
        Fonts.apple18.drawStringWithShadow("Blinking " + BlinkUtil.getSize(), (float) sr.getScaledWidth() / 2 + 10, (float) sr.getScaledHeight() / 2 - 10,  0xffffff);
    }

    private boolean isOverVoid() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 40, mc.thePlayer.posZ),
                true, true, false) == null;
    }

    private boolean isDamageFall() {
        return mc.theWorld.rayTraceBlocks(
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 3, mc.thePlayer.posZ),
                true, true, false) == null;
    }

    public String toString() {
        return "Hypixel Blink";
    }
}
