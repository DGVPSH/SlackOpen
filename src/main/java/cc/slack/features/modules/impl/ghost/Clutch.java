package cc.slack.features.modules.impl.ghost;

import cc.slack.events.impl.player.HitSlowDownEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.utils.other.BlockUtils;
import cc.slack.utils.player.InventoryUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(
        name = "Clutch",
        category = Category.GHOST
)
public class Clutch extends Module {

    private final NumberValue<Double> searchDist = new NumberValue<>("Search Distance", 4.0, 1.0, 6.0, 0.5);
    private final ModeValue<String> mode = new ModeValue<>("Toggle Mode", new String[]{"Auto Disable", "Manual", "Predictive"});

    public Clutch() { addSettings(searchDist, mode); }

    @Listen
    public void onUpdate(UpdateEvent e) {
        if (mc.thePlayer.onGround && mode.getValue().equalsIgnoreCase("auto disable")) {
            toggle();
            return;
        }

        if (mode.getValue().equalsIgnoreCase("predictive")) {
            if (!(PlayerUtil.isOverVoid() && PlayerUtil.isOverVoid(
                    mc.thePlayer.posX + mc.thePlayer.motionX * 7,
                    mc.thePlayer.posY + MovementUtil.predictedSumMotion(mc.thePlayer.motionY, 7),
                    mc.thePlayer.posZ + mc.thePlayer.motionZ * 7
            )) || mc.thePlayer.onGround) {
                return;
            }
        }

        if (startSearch() && pickBlock()) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
        }
    }

    private boolean startSearch() {
        BlockPos below = new BlockPos(
                mc.thePlayer.posX,
                mc.thePlayer.posY - 1,
                mc.thePlayer.posZ);
        if(!BlockUtils.isReplaceable(below)) return false;

        List<BlockPos> searchQueue = new ArrayList<>();

        searchQueue.add(below.down());
        int dist = searchDist.getValue().intValue();
        for (int x = -dist; x <= dist; x++) {
            for (int z = -dist; z <= dist; z++) {
                searchQueue.add(below.add(x,0, z));
            }
        }

        searchQueue.sort(Comparator.comparingDouble(BlockUtils::getClutchPriority));

        for (int i = 0; i < searchQueue.size(); i++)
        {
            if (searchBlock(searchQueue.get(i))) {
                return true;
            }
        }

        for (int i = 0; i < searchQueue.size(); i++)
        {
            if (searchBlock(searchQueue.get(i).down())) {
                return true;
            }
        }
        for (int i = 0; i < searchQueue.size(); i++)
        {
            if (searchBlock(searchQueue.get(i).down().down())) {
                return true;
            }
        }
        return false;
    }

    private boolean searchBlock(BlockPos block) {
        if (!BlockUtils.isReplaceable(block)) {
            EnumFacing placeFace = BlockUtils.getHorizontalFacingEnum(block, mc.thePlayer.posX, mc.thePlayer.posZ);
            if (block.getY() <= mc.thePlayer.posY - 3) {
                placeFace = EnumFacing.UP;
            }
            BlockPos blockPlacement = block.add(placeFace.getDirectionVec());
            if (!BlockUtils.isReplaceable(blockPlacement)) {
                return false;
            }
            mc.thePlayer.posX += mc.thePlayer.motionX;
            mc.thePlayer.posY += mc.thePlayer.motionY;
            mc.thePlayer.posZ += mc.thePlayer.motionZ;

            RotationUtil.setPlayerRotation(BlockUtils.getFaceRotation(placeFace, block));

            mc.thePlayer.posX -= mc.thePlayer.motionX;
            mc.thePlayer.posY -= mc.thePlayer.motionY;
            mc.thePlayer.posZ -= mc.thePlayer.motionZ;
            return true;
        } else {
            return false;
        }
    }

    private boolean pickBlock() {
        int slot = InventoryUtil.pickHotarBlock(true);
        if (slot != -1) {
            mc.thePlayer.inventory.currentItem = slot;
            return true;
        }
        return false;
    }

}
