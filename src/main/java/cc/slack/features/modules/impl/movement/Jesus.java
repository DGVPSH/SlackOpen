// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.impl.movement.jesus.IJesus;
import cc.slack.features.modules.impl.movement.jesus.impl.VanillaJesus;
import cc.slack.features.modules.impl.movement.jesus.impl.VerusJesus;
import cc.slack.features.modules.impl.movement.jesus.impl.VulcanJumpJesus;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "Jesus",
        category = Category.MOVEMENT
)

public class Jesus extends Module {

    private final ModeValue<IJesus> mode = new ModeValue<>(new IJesus[]{
            // Vanilla
            new VanillaJesus(),

            // Vulcan
            new VulcanJumpJesus(),

            // Verus
            new VerusJesus()
    });

    public Jesus() {
        super();
        addSettings(mode);
    }

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
        mode.getValue().onDisable();
    }

    @Listen
    public void onUpdate(UpdateEvent event) {
        mode.getValue().onUpdate(event);
    }

    @Listen
    public void onPacket(PacketEvent event) {
        mode.getValue().onPacket(event);
    }

    @Listen
    public void onMotion(MotionEvent event) {
        mode.getValue().onMotion(event);
    }

    @Listen
    public void onMove(MoveEvent event) {
        if (!mc.thePlayer.isInWater()) return;

        mode.getValue().onMove(event);
    }

    @Listen
    public void onStrafe(StrafeEvent event) {
        mode.getValue().onStrafe(event);
    }

    @Override
    public String getMode() {
        return mode.getValue().toString();
    }
}
