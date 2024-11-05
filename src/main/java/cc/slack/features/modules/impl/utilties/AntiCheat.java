package cc.slack.features.modules.impl.utilties;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.player.WorldEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.utilties.Anticheat.impl.AutoblockCheck;
import cc.slack.features.modules.impl.utilties.Anticheat.impl.NoSlowLimitCheck;
import cc.slack.features.modules.impl.utilties.Anticheat.impl.ScaffoldLimitCheck;
import cc.slack.features.modules.impl.utilties.Anticheat.impl.TowerLimitCheck;
import cc.slack.features.modules.impl.utilties.Anticheat.utils.NewBlocks;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;

import java.util.ArrayList;


@ModuleInfo(
        name = "AntiCheat",
        category = Category.UTILITIES
)

public class AntiCheat extends Module {
    private final BooleanValue scaffoldLimitCheck =     new BooleanValue("Check - Scaffold Limit", true);
    private final BooleanValue towerLimitCheck =        new BooleanValue("Check - Tower Limit", true);
    private final BooleanValue autoblockCheck =         new BooleanValue("Check - Autoblock", true);
    private final BooleanValue noslowLimitCheck =       new BooleanValue("Check - NoSlow Limit", true);
    private final BooleanValue onlySuspect =            new BooleanValue("Target - Only Suspect", false);
    private final NumberValue<Integer> vlLimit =        new NumberValue<>("Alert - VL Limit", 40, 0, 300, 5);
    private final BooleanValue allFlags =               new BooleanValue("Alert - All Flags", true);
    public final BooleanValue debug =                   new BooleanValue("Alert - Debug", false);

    public AntiCheat() {
        addSettings(scaffoldLimitCheck, towerLimitCheck, autoblockCheck, noslowLimitCheck, onlySuspect, vlLimit, allFlags, debug);
    }

    private final ArrayList<Integer> players = new ArrayList<>();
    private final ArrayList<ScaffoldLimitCheck> ScaffoldLimitChecks = new ArrayList<>();
    private final ArrayList<TowerLimitCheck> TowerLimitChecks = new ArrayList<>();
    private final ArrayList<AutoblockCheck> AutoblockChecks = new ArrayList<>();
    private final ArrayList<NoSlowLimitCheck> NoslowLimitChecks = new ArrayList<>();


    @Listen
    public void onWorld(WorldEvent e) {
        players.clear();
        ScaffoldLimitChecks.clear();
        TowerLimitChecks.clear();
        AutoblockChecks.clear();
        NoslowLimitChecks.clear();
    }

    @Listen
    public void onUpdate(UpdateEvent event) {
        for (Entity e : mc.theWorld.loadedEntityList) {
                if (e instanceof EntityPlayer) {
                    if (onlySuspect.getValue() && !e.getDisplayName().getUnformattedText().contains("Suspect")) {
                        continue;
                    }
                    if (players.contains(e.getEntityId())) {
                        if (scaffoldLimitCheck.getValue()) {
                            ScaffoldLimitChecks.get(players.indexOf(e.getEntityId())).runCheck((EntityPlayer) e);
                        }
                        if (towerLimitCheck.getValue()) {
                            TowerLimitChecks.get(players.indexOf(e.getEntityId())).runCheck((EntityPlayer) e);
                        }
                        if (autoblockCheck.getValue()) {
                            AutoblockChecks.get(players.indexOf(e.getEntityId())).runCheck((EntityPlayer) e);
                        }
                        if (noslowLimitCheck.getValue()) {
                            NoslowLimitChecks.get(players.indexOf(e.getEntityId())).runCheck((EntityPlayer) e);
                        }
                    } else {
                        players.add(e.getEntityId());
                        ScaffoldLimitChecks.add(new ScaffoldLimitCheck((EntityPlayer) e));
                        TowerLimitChecks.add(new TowerLimitCheck((EntityPlayer) e));
                        AutoblockChecks.add(new AutoblockCheck((EntityPlayer) e));
                        NoslowLimitChecks.add(new NoSlowLimitCheck((EntityPlayer) e));
                    }
                }

        }
    }



    @SuppressWarnings("unused")
    @Listen
    public void onPacket (PacketEvent p) {
        Packet packet = p.getPacket();

        if (packet instanceof S22PacketMultiBlockChange) {
            for ( S22PacketMultiBlockChange.BlockUpdateData d : ((S22PacketMultiBlockChange) packet).getChangedBlocks()) {
                NewBlocks.addBlock(d.getPos());
            }
        } else if (packet instanceof S23PacketBlockChange) {
            NewBlocks.addBlock(((S23PacketBlockChange) packet).getBlockPosition());
        }

        if (packet instanceof S0BPacketAnimation) {
            if (((S0BPacketAnimation) packet).getAnimationType() == 0) {
                if (players.contains(((S0BPacketAnimation) packet).getEntityID())) {
                    AutoblockChecks.get(players.indexOf(((S0BPacketAnimation) packet).getEntityID())).swing();
                }
            }
        }
    }
}
