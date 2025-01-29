// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.input.onMoveInputEvent;
import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.*;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.movement.flights.IFlight;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.impl.movement.flights.impl.others.*;
import cc.slack.features.modules.impl.movement.flights.impl.vanilla.CreativeFly;
import cc.slack.features.modules.impl.movement.flights.impl.vanilla.FireballFlight;
import cc.slack.features.modules.impl.movement.flights.impl.vanilla.VanillaFlight;
import cc.slack.features.modules.impl.movement.flights.impl.verus.VerusDamageFlight;
import cc.slack.features.modules.impl.movement.flights.impl.verus.VerusFloatFlight;
import cc.slack.features.modules.impl.movement.flights.impl.verus.VerusJumpFlight;
import cc.slack.features.modules.impl.movement.flights.impl.verus.VerusPortFlight;
import cc.slack.features.modules.impl.movement.flights.impl.vulcan.VulcanGhostFlight;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;


@ModuleInfo(
        name = "Flight",
        category = Category.MOVEMENT
)
public class Flight extends Module {

    public final ModeValue<IFlight> mode = new ModeValue<>(new IFlight[]{

            // Vanilla
            new VanillaFlight(),
            new FireballFlight(),
            new CreativeFly(),

            // Verus
            new VerusJumpFlight(),
            new VerusDamageFlight(),
            new VerusPortFlight(),
            new VerusFloatFlight(),
            // Others
            new ChunkFlight(),
            new CollideFlight(),
            new AirJumpFlight(),
            new AirPlaceFlight(),
            new MMCFlight()
    });


    public final NumberValue<Float> vanillaspeed = new NumberValue<>("Fly Vanilla Speed", 3F, 0F, 10F, 1F);
    public final NumberValue<Float> fbpitch = new NumberValue<>("Fireball Fly Pitch", 70f, 30f,90f, 3f);
    public final ModeValue<String> fbmode= new ModeValue<>("Fireball Fly Mode", new String[]{"Legit", "Simple", "Flat", "High"});
    public final NumberValue<Float> fbspeed = new NumberValue<>("Fireball Fly Speed", 1.6f, 0f,3f, 0.05f);
    public final NumberValue<Float> fbfriction = new NumberValue<>("Fireball Fly Friction", 1.0f, 1.0f,1.1f, 0.005f);
    public final NumberValue<Float> fbhigh = new NumberValue<>("Fireball Fly High Start", 0.7f, 0.3f,1f, 0.05f);
    public final NumberValue<Float> fbgravity = new NumberValue<>("Fireball Fly High Gravity", 0.018f, 0f,0.1f, 0.001f);


    public Flight() {
        super();
        addSettings(mode, vanillaspeed,fbpitch, fbmode, fbspeed, fbfriction, fbhigh, fbgravity);
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
    public void onMotion(MotionEvent event) {
        mode.getValue().onMotion(event);
    }

    @Listen
    public void onMoveInput(onMoveInputEvent event) {
        mode.getValue().onMoveInput(event);
    }

    @Listen
    public void onPostStrafe(PostStrafeEvent event) { mode.getValue().onPostStrafe(event);}

    @Listen
    public void onAttack(AttackEvent event) {
        mode.getValue().onAttack(event);
    }

    @Override
    public String getMode() {
        return mode.getValue().toString();
    }

}
