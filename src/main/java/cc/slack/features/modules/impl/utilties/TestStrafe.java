package cc.slack.features.modules.impl.utilties;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.utils.network.PacketUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Random;


@ModuleInfo(
        name = "TestStrafe",
        category = Category.UTILITIES
)

public class TestStrafe extends Module {

    private final BooleanValue cobweb = new BooleanValue("Cobweb", false);

    public TestStrafe() {
        addSettings(cobweb);
    }

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate (UpdateEvent event) {
        if (Math.random()<0.2) {
            if (cobweb.getValue()) {
                PacketUtil.send(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), EnumFacing.UP.getIndex(), new ItemStack(Blocks.web), (float) Math.random(), 1.0F, (float) Math.random()));
            } else {
                PacketUtil.send(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), EnumFacing.UP.getIndex(), new ItemStack(Items.water_bucket), (float) Math.random(), 1.0F, (float) Math.random()));
            }
        }
    }
}
