// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement.flights.impl.vanilla;

import cc.slack.start.Slack;
import cc.slack.events.impl.player.MoveEvent;
import cc.slack.features.modules.impl.movement.Flight;
import cc.slack.features.modules.impl.movement.flights.IFlight;
import cc.slack.utils.player.MovementUtil;

public class VanillaFlight implements IFlight {

    @Override
    public void onDisable() {
        MovementUtil.resetMotion(true);
    }

    @Override
    public void onMove(MoveEvent event) {
        event.setY((mc.gameSettings.keyBindJump.isKeyDown() ? 1 * Slack.getInstance().getModuleManager().getInstance(Flight.class).vanillaspeed.getValue() :
                mc.gameSettings.keyBindSneak.isKeyDown() ? -1 * Slack.getInstance().getModuleManager().getInstance(Flight.class).vanillaspeed.getValue() : 0));
        MovementUtil.setSpeed(event, Slack.getInstance().getModuleManager().getInstance(Flight.class).vanillaspeed.getValue());
    }

    @Override
    public String toString() {
        return "Vanilla";
    }
}
