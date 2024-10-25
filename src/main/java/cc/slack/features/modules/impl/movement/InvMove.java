// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.movement;

import cc.slack.events.impl.network.PacketEvent;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.rotations.RotationUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;

@ModuleInfo(
        name = "InvMove",
        category = Category.MOVEMENT
)
public class InvMove extends Module {

    private final BooleanValue noOpen = new BooleanValue("Cancel Inventory Open", false);
    public final BooleanValue hypixelTest = new BooleanValue("Hypixel", false);
    private final BooleanValue noBadPacket = new BooleanValue("No Bad Packets", true);

    boolean c16 = false;
    boolean c0d = false;
    boolean lastC0d= false;

    public InvMove() {
        super();
        addSettings(noOpen, hypixelTest, noBadPacket);
    }

    @SuppressWarnings("unused")
    @Listen
    public void onUpdate (UpdateEvent event) {
        c16 = false;
        c0d = false;

        if (!hypixelTest.getValue() || mc.currentScreen instanceof GuiInventory) {
            MovementUtil.updateBinds(false);
            RotationUtil.updateStrafeFixBinds();
        }
        if (mc.currentScreen instanceof GuiInventory && hypixelTest.getValue()) {
            if (mc.thePlayer.ticksExisted % (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 3 : 4) == 0) {
                PacketUtil.send(new C0DPacketCloseWindow());
            } else if (mc.thePlayer.ticksExisted % (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 3 : 4) == 1) {
                PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            }
        }
    }

    @Listen
    public void onPacket (PacketEvent event) {
        if (event.getPacket() instanceof C16PacketClientStatus && noOpen.getValue()) {
            if (event.getPacket() == new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                event.cancel();
            }
        }

        if (noBadPacket.getValue()) {
            if (event.getPacket() instanceof C16PacketClientStatus) {
                if (c16) {
                    event.cancel();
                }
                c16 = true;
            }

            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                if (c0d) {
                    event.cancel();
                }
                c0d = true;
            }
        }
    }

    @Override
    public String getMode() {
        if (hypixelTest.getValue()) {
            return "Hypixel";
        } else {
            return "";
        }
    }

}
