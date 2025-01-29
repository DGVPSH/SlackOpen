// Slack Client (discord.gg/paGUcq2UTb)

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.util.Random;

@ModuleInfo(
        name = "SilentScaffold",
        category = Category.GHOST
)
public class SilentScaffold extends Module {

    public final ModeValue<String> mode = new ModeValue<>(new String[]{"FastBridge", "Breezily", "Snap", "Glitch", "Godbridge"}) ;

    private boolean shouldSneak = false;
    private boolean breezily = false;
    private int gbCount = 0;

    public SilentScaffold() {
        super();
        addSettings(mode);
    }

    @Override
    public void onEnable() {
        gbCount = 0;
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
            case "glitch":
                RotationUtil.setPlayerRotation(new float[]{
                        Math.round(FreeLookUtil.cameraYaw / 90) * 90 + 0.005491f + 180,
                        46f
                });

                EnumFacing facing = mc.thePlayer.getHorizontalFacing();
                double offset = 0.00019;

                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());

                switch (facing) {
                    case NORTH:
                        mc.thePlayer.motionX = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posX) - mc.thePlayer.posX - offset));
                        break;
                    case SOUTH:
                        mc.thePlayer.motionX = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posX) - mc.thePlayer.posX + offset));
                        break;
                    case EAST:
                        mc.thePlayer.motionZ = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posZ) - mc.thePlayer.posZ - offset));
                        break;
                    case WEST:
                        mc.thePlayer.motionZ = Math.max(-0.2, Math.min(0.2, Math.round(mc.thePlayer.posZ) - mc.thePlayer.posZ + offset));
                        break;
                    default:
                        break;
                }
                break;
            case "godbridge":
                if (Math.round(FreeLookUtil.cameraYaw/45) % 2 == 0) {
                    RotationUtil.setPlayerRotation(RotationUtil.getLimitedRotation(RotationUtil.getPlayerRotation(), new float[]{Math.round(FreeLookUtil.cameraYaw/45)*45 + 180 + 135, 75.9f}, 85));
                    RotationUtil.strafeFixBinds(135 - MovementUtil.getBindsDirection(0));
                } else {
                    RotationUtil.setPlayerRotation(RotationUtil.getLimitedRotation(RotationUtil.getPlayerRotation(), new float[]{Math.round(FreeLookUtil.cameraYaw/45)*45 + 180 + 180, 75.6f}, 85));
                    RotationUtil.strafeFixBinds(180 - MovementUtil.getBindsDirection(0));
                }
                if (mc.thePlayer.ticksExisted % 2 == 0) KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                if (PlayerUtil.isOverAir() && mc.thePlayer.onGround) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    gbCount ++;
                }
                if (gbCount > 7 && mc.thePlayer.onGround && Math.round(FreeLookUtil.cameraYaw/45) % 2 == 0) {
                    gbCount = 0;
                    mc.thePlayer.jump();
                }
                break;
            case "fastbridge":

                if (Math.round(FreeLookUtil.cameraYaw/45) % 2 == 0) {
                    RotationUtil.setPlayerRotation(RotationUtil.getLimitedRotation(RotationUtil.getPlayerRotation(), new float[]{FreeLookUtil.cameraYaw + 180 + 135, 78.7f}, 85));
                    RotationUtil.strafeFixBinds(135 - MovementUtil.getBindsDirection(0));
                } else {
                    RotationUtil.setPlayerRotation(RotationUtil.getLimitedRotation(RotationUtil.getPlayerRotation(), new float[]{FreeLookUtil.cameraYaw + 180 + 180, 78.9f}, 85));
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
