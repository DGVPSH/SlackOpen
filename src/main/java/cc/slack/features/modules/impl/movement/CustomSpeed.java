// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.start.Slack;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import io.github.nevalackin.radbus.Listen;


@ModuleInfo(
        name = "CustomSpeed",
        category = Category.MOVEMENT
)
public class CustomSpeed extends Module {

    private final NumberValue<Double> move = new NumberValue<>("Move", 0.2, 0.0, 1.0, 0.01);
    private final NumberValue<Double> mult = new NumberValue<>("Mult", 1.0, 0.0, 2.0, 0.01);
    private final NumberValue<Double> move5 = new NumberValue<>("Move5", 0.05, 0.0, 1.0, 0.01);
    private final NumberValue<Double> mult5 = new NumberValue<>("Mult5", 1.0, 0.0, 2.0, 0.01);

    private final NumberValue<Double> y1 = new NumberValue<>("Y0", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y2 = new NumberValue<>("Y1", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y3 = new NumberValue<>("Y2", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y4 = new NumberValue<>("Y3", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y5 = new NumberValue<>("Y4", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y6 = new NumberValue<>("Y5", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y7 = new NumberValue<>("Y6", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y8 = new NumberValue<>("Y7", 0.0, -0.2, 0.2, 0.0000001);
    private final NumberValue<Double> y9 = new NumberValue<>("Y8", 0.0, -0.2, 0.2, 0.0000001);
    private final BooleanValue notOnDmg = new BooleanValue("Not On Damage", true);

    private boolean dmg = false;

    public CustomSpeed() {
        addSettings(move, mult, move5, mult5, y1, y2, y3, y4, y5, y6 ,y7 ,y8, y9, notOnDmg);
    }


    @Listen
    public void onUpdate(UpdateEvent e) {

        if (mc.thePlayer.onGround) {
            dmg = false;
            mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
            mc.thePlayer.motionX *= mult.getValue();
            mc.thePlayer.motionZ *= mult.getValue();
            MovementUtil.move(move.getValue().floatValue());
            mc.thePlayer.motionY += y1.getValue();
        } else {
            if (dmg) return;
            switch (mc.thePlayer.offGroundTicks) {
                case 1:
                    mc.thePlayer.motionY += y2.getValue();
                    break;
                case 2:
                    mc.thePlayer.motionY += y3.getValue();
                    break;
                case 3:
                    mc.thePlayer.motionY += y4.getValue();
                    break;
                case 4:
                    mc.thePlayer.motionY += y5.getValue();
                    break;
                case 5:
                    mc.thePlayer.motionY += y6.getValue();
                    mc.thePlayer.motionX *= mult5.getValue();
                    mc.thePlayer.motionZ *= mult5.getValue();
                    MovementUtil.move(move5.getValue().floatValue());
                    break;
                case 6:
                    mc.thePlayer.motionY += y7.getValue();
                    break;
                case 7:
                    mc.thePlayer.motionY += y8.getValue();
                    break;
                case 8:
                    mc.thePlayer.motionY += y9.getValue();
                    break;
            }
        }
    }

}
