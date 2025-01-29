// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.utilties.Anticheat.utils;
import cc.slack.utils.client.IMinecraft;
import cc.slack.utils.other.BlockUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;

public final class NewBlocks implements IMinecraft {

    private static ArrayList<BlockPos> newBlocks = new ArrayList<>();
    private static ArrayList<Long> newBlockTimes = new ArrayList<>();

    public static void reset() {
        newBlocks.clear();
        newBlockTimes.clear();
    }

    public static boolean isNewBlock (BlockPos blockPos) {
         while (!newBlocks.isEmpty() && newBlockTimes.get(0) < System.currentTimeMillis()) {
             newBlocks.remove(0);
             newBlockTimes.remove(0);
         }

         return newBlocks.contains(blockPos) || BlockUtils.isAir(blockPos);
    }

    public static void addBlock(BlockPos blockPos) {
        newBlocks.add(blockPos);
        newBlockTimes.add(System.currentTimeMillis() + 400L);
    }
}

