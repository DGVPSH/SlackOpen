// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.player.AttackEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.ItemSpoofUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.render.FreeLookUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;

import java.util.Random;

@ModuleInfo(
        name = "SilentScaffold",
        category = Category.GHOST
)
public class SilentScaffold extends Module {

    public final ModeValue<String> mode = new ModeValue<>(new String[]{"FastBridge", "Breezily", "Snap"}) ;

    private boolean shouldSneak = false;
    private boolean breezily = false;


    public SilentScaffold() {
        super();
        addSettings(mode);
    }

    @Override
    public void onEnable() {
        FreeLookUtil.enable();
        FreeLookUtil.cameraYaw += 180;
    }

    @Override
    public void onDisable() {
        mc.thePlayer.rotationYaw = FreeLookUtil.cameraYaw + 180;
        mc.thePlayer.rotationPitch = FreeLookUtil.cameraPitch;
        FreeLookUtil.setFreelooking(false);

        MovementUtil.updateBinds();
    }

    @Listen
    public void onUpdate(UpdateEvent e) {

        if (!pickBlock()) return;

        switch (mode.getValue().toLowerCase()) {
            case "fastbridge":

                if (Math.round(FreeLookUtil.cameraYaw/45) % 2 == 0) {
                    RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 135, 78.7f});
                    RotationUtil.strafeFixBinds(135 - MovementUtil.getBindsDirection(0));
                } else {
                    RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 180, 78.9f});
                    RotationUtil.strafeFixBinds(180 - MovementUtil.getBindsDirection(0));
                }

                if (PlayerUtil.isOverAir()) {
                    shouldSneak = true;
                } else {
                    shouldSneak = false;
                }

                mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || shouldSneak;

                if (shouldSneak) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                }
                break;

            case "breezily":
                if ((Math.round(FreeLookUtil.cameraYaw / 45)) % 2 == 0) {
                    RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 180, 79.6f});
                } else {
                    RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 180, 76.3f});
                }

                RotationUtil.strafeFixBinds(180);

                mc.gameSettings.keyBindRight.pressed = false;
                mc.gameSettings.keyBindLeft.pressed = false;
                if (PlayerUtil.isOverAir()) KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());

                if (PlayerUtil.isOverAir() && (Math.round(FreeLookUtil.cameraYaw / 45)) % 2 == 0) {
                    breezily = !breezily;
                    mc.gameSettings.keyBindRight.pressed = breezily;
                    mc.gameSettings.keyBindLeft.pressed = !breezily;
                    // zig zag jitter
                }
                break;
            case "snap":
                if (PlayerUtil.isOverAir()) {
                    if ((Math.round(FreeLookUtil.cameraYaw / 45)) % 2 == 0) {
                        RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 180, 79.6f});
                    } else {
                        RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180 + 180, 76.3f});
                    }

                    RotationUtil.strafeFixBinds(180);
                } else {
                    RotationUtil.setPlayerRotation(new float[]{FreeLookUtil.cameraYaw + 180, 79.6f});
                    MovementUtil.updateBinds();
                }

                mc.gameSettings.keyBindRight.pressed = false;
                mc.gameSettings.keyBindLeft.pressed = false;
                if (PlayerUtil.isOverAir()) KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());

                if (PlayerUtil.isOverAir() && (Math.round(FreeLookUtil.cameraYaw / 45)) % 2 == 0) {
                    breezily = !breezily;
                    mc.gameSettings.keyBindRight.pressed = breezily;
                    mc.gameSettings.keyBindLeft.pressed = !breezily;
                    // zig zag jitter
                }
                break;
        }
    }

    private boolean pickBlock() {
        int slot = InventoryUtil.pickHotarBlock(false);
        if (slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;
            return true;
        }
        return false;
    }

    @Override
    public String getMode() {
        return mode.getValue();
    }

}
