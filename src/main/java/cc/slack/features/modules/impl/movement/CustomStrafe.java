// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.player.StrafeEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;

@ModuleInfo(
        name = "CustomStrafe",
        category = Category.MOVEMENT
)
public class CustomStrafe extends Module {

    private final NumberValue<Float> strength = new NumberValue<>("Strength", 100F, 1F, 100F, 1F);
    private final BooleanValue ground = new BooleanValue("Ground", true);

    private final BooleanValue t1 = new BooleanValue("Tick1", true);
    private final BooleanValue t2 = new BooleanValue("Tick2", true);
    private final BooleanValue t3 = new BooleanValue("Tick3", true);
    private final BooleanValue t4 = new BooleanValue("Tick4", true);
    private final BooleanValue t5 = new BooleanValue("Tick5", true);
    private final BooleanValue t6 = new BooleanValue("Tick6", true);
    private final BooleanValue t7 = new BooleanValue("Tick7", true);
    private final BooleanValue t8 = new BooleanValue("Tick8", true);
    private final BooleanValue t9 = new BooleanValue("Tick9", true);
    private final BooleanValue t10 = new BooleanValue("Tick10", true);
    private final BooleanValue t11 = new BooleanValue("Tick11", true);



    public CustomStrafe() {
        addSettings(strength, ground, t1, t2, t3, t4, t5, t6,t7, t8, t9, t10, t11);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
    }

    @SuppressWarnings("unused")
    @Listen
    public void onStrafe (StrafeEvent event) {
        if (mc.thePlayer.onGround && ground.getValue()) {
            MovementUtil.customStrafeStrength(strength.getValue());
        } else {
            switch (mc.thePlayer.offGroundTicks) {
                case 1: if (t1.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 2: if (t2.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 3: if (t3.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 4: if (t4.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 5: if (t5.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 6: if (t6.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 7: if (t7.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 8: if (t8.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 9: if (t9.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 10: if (t10.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                case 11: if (t11.getValue()) MovementUtil.customStrafeStrength(strength.getValue()); break;
                default:
                    break;
            }
        }
    }

}
