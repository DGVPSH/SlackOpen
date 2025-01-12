// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.other;

import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.utils.other.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ModuleInfo(
        name = "AntiBot",
        category = Category.OTHER
)
public class AntiBot extends Module {


    public final BooleanValue colored = new BooleanValue("Colored name", true);
    public final BooleanValue npc = new BooleanValue("NPC (hasn't moved)", true);
    Map<UUID, Double> map = new HashMap<UUID, Double>();


    public AntiBot() {
        super();
        addSettings(colored, npc);
    }

    public boolean isBot(EntityLivingBase e) {
        if (colored.getValue() && e.getCustomNameTag().contains("\u00A7")) return true;
        if (npc.getValue()) {
            if (map.containsKey(e.getUniqueID())) {
                map.put(e.getUniqueID(), map.get(e.getUniqueID()) + e.motionX + e.motionZ);
                if (map.get(e.getUniqueID()) > 4) {
                    return true;
                }
            } else {
                if (e.ticksExisted > 4) {
                    map.put(e.getUniqueID(), 0d);
                }
            }
        }
        return false;
    }
}
