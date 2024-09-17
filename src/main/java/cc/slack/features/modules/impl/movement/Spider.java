package cc.slack.features.modules.impl.movement;

import cc.slack.events.State;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.CollideEvent;
import cc.slack.events.impl.player.MotionEvent;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.movement.spiders.ISpider;
import cc.slack.features.modules.impl.movement.spiders.impl.*;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "Spider",
        category = Category.MOVEMENT
)
public class Spider extends Module {

    private final ModeValue<ISpider> mode = new ModeValue<>(new ISpider[]{

            new NormalSpider(),
            new JumpSpider(),
            new VulcanSpider(),
            new VerusSpider()

    });

    public final NumberValue<Double> spiderSpeedValue = new NumberValue<>("Speed", 0.2D, 0.0D, 5.0D, 0.1D);

    // Display
    private final ModeValue<String> displayMode = new ModeValue<>("Display", new String[]{"Simple", "Off"});


    public Spider() {
        super();
        addSettings(mode, spiderSpeedValue, displayMode);
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
    public void onMove(MoveEvent event) {
        mode.getValue().onMove(event);
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
    public void onCollide(CollideEvent event) {
        mode.getValue().onCollide(event);
    }

    @Listen
    public void onMotion (MotionEvent event) {
        if (event.getState() != State.PRE) return;

        mode.getValue().onMotion(event);

    }

    @Override
    public String getMode() {
        switch (displayMode.getValue()) {
            case "Simple":
                return mode.getValue().toString();
        }
        return null;
    }



}
