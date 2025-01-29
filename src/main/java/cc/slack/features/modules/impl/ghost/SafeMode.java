// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.AttackEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.combat.KillAura;
import cc.slack.features.modules.impl.movement.CustomSpeed;
import cc.slack.features.modules.impl.player.AntiVoid;
import cc.slack.features.modules.impl.player.NoFall;
import cc.slack.features.modules.impl.utilties.Float;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.network.play.client.*;
import net.minecraft.util.MathHelper;

import java.util.Random;

@ModuleInfo(
        name = "Safe Mode",
        category = Category.GHOST
)
public class SafeMode extends Module {

    public final BooleanValue packets = new BooleanValue("Avoid Badpackets", true);
    public final BooleanValue ka = new BooleanValue("No killaura", true);
    public final BooleanValue movement = new BooleanValue("No Movement", true);

    public double combatReach = 3.0;

    public SafeMode() {
        super();
        addSettings(packets, ka, movement);
    }

    boolean c09 = false;
    boolean c08 = false;
    boolean c07 = false;

    @Listen
    public void onUpdate(UpdateEvent event) {
        if (ka.getValue()) {
            if (Slack.getInstance().getModuleManager().getInstance(KillAura.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(KillAura.class).toggle();
            }
        }

        if (movement.getValue()) {
            if (Slack.getInstance().getModuleManager().getInstance(NoFall.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(NoFall.class).toggle();
            }
            if (Slack.getInstance().getModuleManager().getInstance(AntiVoid.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(AntiVoid.class).toggle();
            }
            if (Slack.getInstance().getModuleManager().getInstance(CustomSpeed.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(CustomSpeed.class).toggle();
            }
            if (Slack.getInstance().getModuleManager().getInstance(Float.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(Float.class).toggle();
            }
            if (Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) {
                Slack.getInstance().getModuleManager().getInstance(Scaffold.class).toggle();
            }
        }

        c09 = false;
        c08 = false;
        c07 = false;
    }

    @Listen
    public void onPacket(PacketEvent event) {
        if (!packets.getValue()) return;
        if (event.getPacket() instanceof C09PacketHeldItemChange) {
            if (c09) event.cancel();
            c09 = true;
        }

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            if (c07) event.cancel();
            c08 = true;
        }

        if (event.getPacket() instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
            if (c07) event.cancel();
            c07 = true;
        }

        if (event.getPacket() instanceof C0APacketAnimation || event.getPacket() instanceof C02PacketUseEntity) {
            if (c07) event.cancel();
        }
    }
}
