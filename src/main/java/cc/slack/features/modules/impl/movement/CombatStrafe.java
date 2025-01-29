// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "CombatStrafe",
        category = Category.MOVEMENT
)
public class CombatStrafe extends Module {

    public final NumberValue<Integer> offset = new NumberValue<>("Offset", 0, -90, 90, 5);
    public final BooleanValue dynamic = new BooleanValue("Dynamic", true);

    public CombatStrafe() {
        addSettings(offset, dynamic);
    }

    public Integer getOffset() {
        if (dynamic.getValue()) {
            if (AttackUtil.inCombat) {
                if (mc.thePlayer.getDistanceSqToEntity(AttackUtil.combatTarget) < 1.5) {
                    return 100;
                } else if (mc.thePlayer.getDistanceSqToEntity(AttackUtil.combatTarget) < 2.2) {
                    return 60;
                } else {
                    return 30;
                }
            }
        } else{
            return  offset.getValue();
        }
        return offset.getValue();
    }
}
