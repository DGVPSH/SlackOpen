package cc.slack.features.modules.impl.utilties;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.start.Slack;
import cc.slack.utils.other.PrintUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;


@ModuleInfo(
        name = "SumoBot",
        category = Category.UTILITIES
)

public class SumoBot extends Module {

    private final ModeValue<String> mode = new ModeValue<>("Server", new String[]{"Hypixel"});

    public SumoBot() {
        addSettings(mode);
    }

    private int games = 0;
    private int wins = 0;

    private int state = 0;
    // 0 - pregame
    // 1 - ingame

    @Listen
    public void onUpdate(UpdateEvent e) {
        if (state == 0) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = PlayerUtil.getJumpHeight();
            }
        } else {
            mc.gameSettings.keyBindForward.setPressed(true);
            EntityLivingBase target = AttackUtil.getTarget(20, "FOV");
            if (target != null) {
                float[] lr = RotationUtil.getLimitedRotation(
                        RotationUtil.getPlayerRotation(),
                        RotationUtil.getTargetRotations(target.getEntityBoundingBox(), RotationUtil.TargetRotation.INNER, 0.1),
                        40
                );

                RotationUtil.setPlayerRotation(lr);
            }
        }
    }

    @SuppressWarnings("unused")
    @Listen
    public void onPacket (PacketEvent event) {
        if (!(event.getPacket() instanceof S02PacketChat)) return;

        IChatComponent chatComponent = ((S02PacketChat) event.getPacket()).getChatComponent();
        String unformattedText = chatComponent.getUnformattedText();

        if (unformattedText.contains("Reward Summary")) {
            games += 1;
            if (mc.thePlayer.posY < 67 && mc.thePlayer.posY > 64) {
                wins += 1;
            }
            mc.thePlayer.sendChatMessage("/play duels_sumo_duel");
            state = 0;
            PrintUtil.message("Joining new game - " + wins + "/" + games);
        } else if (unformattedText.contains("Eliminate your opponents")) {
            state = 1;
            PrintUtil.message("Game started.");
        }
    }
}
