// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render.hud.arraylist;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.utils.client.IMinecraft;
import net.minecraft.client.Minecraft;

public interface IArraylist extends IMinecraft {
    Minecraft mc = IMinecraft.mc;
    void onRender(RenderEvent event);
    void onUpdate(UpdateEvent event);
}
