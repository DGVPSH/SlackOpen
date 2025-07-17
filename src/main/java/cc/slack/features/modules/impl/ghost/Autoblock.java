// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.game.TickEvent;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.network.BlinkUtil;
import cc.slack.utils.player.AttackUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

@ModuleInfo(
        name = "AutoBlock",
        category = Category.GHOST
)
public class Autoblock extends Module {

    public NumberValue<Integer> ticksSetting = new NumberValue<>("Blink ticks", 1, 1, 10, 1);
    public BooleanValue onlyCombat = new BooleanValue("Only Combat", true);

    private int ticks;
    public boolean blinking = false;

    public Autoblock() {
        addSettings(ticksSetting, onlyCombat);
    }

    @Override
    public void onDisable() {
        BlinkUtil.disable();
    }

    @Listen
    public void onPacket(PacketEvent e) {
        Packet packet = e.getPacket();
        if (packet instanceof C07PacketPlayerDigging) {
            if (mc.thePlayer.getHeldItem() != null) {
                if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    if (((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                        if (!onlyCombat.getValue() || AttackUtil.inCombat) {
                            BlinkUtil.enable(false, true);
                            ticks = 0;
                            blinking = true;
                        }
                    }
                }
            }
        }
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            if (mc.thePlayer.getHeldItem() != null) {
                if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    BlinkUtil.disable();
                    blinking = false;
                }
            }
        }
    }

    @Listen
    public void onTick(TickEvent e) {
        if (!blinking) return;
        if (ticks >= ticksSetting.getValue()) {
            BlinkUtil.disable();
            blinking = false;
        }
        ticks++;
    }
}
