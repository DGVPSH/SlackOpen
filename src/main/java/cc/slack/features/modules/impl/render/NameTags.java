// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.render;

import cc.slack.events.impl.render.LivingLabelEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.utils.other.MathUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.PlayerUtil;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector4d;
import java.awt.*;


@ModuleInfo(
        name = "NameTags",
        category = Category.RENDER
)
public class NameTags extends Module {

    private final BooleanValue drawArmor = new BooleanValue("Draw Armor", true);
    private final BooleanValue drawHealth = new BooleanValue("Draw Health", true);

    public NameTags() {
        addSettings(drawArmor, drawHealth);
    }

    @Listen
    public void onRender (RenderEvent e) {
        if (e.getState() == RenderEvent.State.RENDER_2D) {
            mc.theWorld.loadedEntityList.forEach(entity -> {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer ent = (EntityPlayer) entity;
                    if (AttackUtil.isTarget(entity) && RenderUtil.isInViewFrustrum(ent.getEntityBoundingBox())) {
                        double posX = RenderUtil.interpolate(ent.posX, ent.lastTickPosX, 1 - e.getPartialTicks());
                        double posY = RenderUtil.interpolate(ent.posY, ent.lastTickPosY, 1 - e.getPartialTicks());
                        double posZ = RenderUtil.interpolate(ent.posZ, ent.lastTickPosZ, 1 - e.getPartialTicks());
                        double height = (ent.height + (ent.isSneaking() ? -0.3 : 0.2)) ;
                        Vector4d position = RenderUtil.getProjectedCoord(posX, posY + height, posZ, e.getPartialTicks());
                        mc.entityRenderer.setupOverlayRendering();
                        if (position != null) {
                            GL11.glPushMatrix();
                            float size = .5f;
                            if (drawArmor.getValue()) { RenderUtil.drawArmor(ent, (int) (position.x + ((position.z - position.x) / 2)), (int) position.y - 4 - mc.MCfontRenderer.FONT_HEIGHT * 2, size); }
                            GlStateManager.scale(size, size, size);
                            float x = (float) position.x / size;
                            float x2 = (float) position.z / size;
                            float y = (float) position.y / size;
                            final String nametext = entity.getDisplayName().getFormattedText() + (drawHealth.getValue() ? " §7(§f" + MathUtil.round(((EntityPlayer) entity).getHealth(), 1) + " §c❤§7)" : "");
                            RenderUtil.drawRoundedRect((x + (x2 - x) / 2) - (mc.MCfontRenderer.getStringWidth(nametext) >> 1) - 5, y - mc.MCfontRenderer.FONT_HEIGHT - 8, (x + (x2 - x) / 2) + (mc.MCfontRenderer.getStringWidth(nametext) >> 1) + 5, y + 5, 8F, ColorUtil.getMaterial(true).getRGB());

                            mc.MCfontRenderer.drawStringWithShadow(nametext, (x + ((x2 - x) / 2)) - (mc.MCfontRenderer.getStringWidth(nametext) / 2F), y - mc.MCfontRenderer.FONT_HEIGHT - 2, PlayerUtil.getNameColor(ent));
                            GL11.glPopMatrix();
                        }
                    }
                }
            });
        }

    }

    @Listen
    public void onLivingLabel (LivingLabelEvent event) {
        if(event.getEntity() instanceof EntityPlayer && AttackUtil.isTarget(event.getEntity())) {
            event.cancel();
        }
    }


}
