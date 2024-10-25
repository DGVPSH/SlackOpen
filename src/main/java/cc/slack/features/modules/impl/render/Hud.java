// Slack Client (discord.gg/slackclient)

package cc.slack.features.modules.impl.render;

import cc.slack.features.modules.impl.render.hud.watermarks.IWatermarks;
import cc.slack.features.modules.impl.render.hud.watermarks.impl.BackgroundedWatermark;
import cc.slack.features.modules.impl.render.hud.watermarks.impl.ClassicWatermark;
import cc.slack.features.modules.impl.render.hud.watermarks.impl.LogoWatermark;
import cc.slack.start.Slack;
import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.ModeValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.render.hud.arraylist.IArraylist;
import cc.slack.features.modules.impl.render.hud.arraylist.impl.*;
import cc.slack.features.modules.impl.world.Scaffold;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.player.ItemSpoofUtil;
import cc.slack.utils.player.MovementUtil;
import cc.slack.utils.render.ColorUtil;
import cc.slack.utils.render.RenderUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.round;
import static net.minecraft.client.gui.Gui.drawRect;

@ModuleInfo(name = "Hud", category = Category.RENDER)
public class Hud extends Module {

	// Arraylist
	private final BooleanValue arraylist = new BooleanValue("Arraylist", true);
	private final ModeValue<IArraylist> arraylistMode = new ModeValue<>("Arraylist Mode", new IArraylist[] {

			new ModernArraylist(),
			new ClassicArrayList(),
			new RavenArrayList()

	});
	public final BooleanValue arraylistResetPos = new BooleanValue("Array Reset Position", false);
	public final ModeValue<String> modernArraylistMode = new ModeValue<>("Modern Style", new String[]{"Normal", "Minimalist"});
	public final ModeValue<String> arraylistsidebar = new ModeValue<>("Modern Sidebar", new String[]{"Classic", "Modern", "Off"});
	public final ModeValue<String> arraylistFont = new ModeValue<>("Classic Arraylist Font", new String[]{"Apple", "Poppins", "Roboto"});
	public final BooleanValue arraylistBackground = new BooleanValue("Arraylist Background", true);
	public final BooleanValue tags = new BooleanValue("Tags", true);
	public final ModeValue<String> tagsMode = new ModeValue<>("Tags Style", new String[]{"[Mode]","(Mode)", "<Mode>", "| Mode", "-> Mode", "- Mode"});

	// Watermark
	public final BooleanValue watermark = new BooleanValue("Watermark", true);
	public final ModeValue<IWatermarks> watermarksmodes = new ModeValue<>("WaterMark", new IWatermarks[] {

			new BackgroundedWatermark(),
			new ClassicWatermark(),
			new LogoWatermark()

	});

	// Notifications
	public final BooleanValue notification = new BooleanValue("Notifications", true);
	public final BooleanValue roundednotification = new BooleanValue("Rounded Notifications", true);


	// Counters
	private final BooleanValue fpsdraw = new BooleanValue("FPS Counter", true);
	private final BooleanValue bpsdraw = new BooleanValue("BPS Counter", true);


	// Scaffold HUD
	private final BooleanValue scaffoldDraw = new BooleanValue("Scaffold Counter", true);

	private final BooleanValue itemSpoofDraw = new BooleanValue("ItemSpoof indicator", true);

	// Sound

	public final BooleanValue sound = new BooleanValue("Toggle Sound", false);

	// Client Theme

	public final ModeValue<ColorUtil.themeStyles> theme = new ModeValue<>("Client Theme", ColorUtil.themeStyles.values());

	public final NumberValue<Integer> r1 = new NumberValue<>("Custom Start R", 0, 0, 255, 5);
	public final NumberValue<Integer> g1 = new NumberValue<>("Custom Start G", 0, 0, 255, 5);
	public final NumberValue<Integer> b1 = new NumberValue<>("Custom Start B", 255, 0, 255, 5);

	public final NumberValue<Integer> r2 = new NumberValue<>("Custom End R", 0, 0, 255, 5);
	public final NumberValue<Integer> g2 = new NumberValue<>("Custom End G", 255, 0, 255, 5);
	public final NumberValue<Integer> b2 = new NumberValue<>("Custom End B", 255, 0, 255, 5);

	private int scaffoldTicks = 0;
	private int itemSpoofTicks = 0;
	private String displayString = " ";
	private ArrayList<String> notText = new ArrayList<>();
	private ArrayList<Long> notEnd = new ArrayList<>();
	private ArrayList<Long> notStart = new ArrayList<>();
	private ArrayList<String> notDetailed = new ArrayList<>();
	private ArrayList<Slack.NotificationStyle> notStyle = new ArrayList<>();

	public Hud() {
		addSettings(arraylist, arraylistMode, arraylistResetPos, modernArraylistMode,arraylistsidebar,arraylistFont, arraylistBackground ,tags, tagsMode, // arraylist
				watermark,watermarksmodes, // watermark
				notification, roundednotification, // notification
				fpsdraw, bpsdraw, scaffoldDraw, itemSpoofDraw, // draws
				sound, // things
				theme, r1, g1, b1, r2, g2, b2 // client theme
		);
	}

	@Listen
	public void onUpdate(UpdateEvent e) {

		arraylistMode.getValue().onUpdate(e);
	}

	@Listen
	public void onRender(RenderEvent e) {
		if (e.state != RenderEvent.State.RENDER_2D) return;

		if (arraylist.getValue()) {
			arraylistMode.getValue().onRender(e);
		}

		if (watermark.getValue()) {
			watermarksmodes.getValue().onRender(e);
		}

		if (fpsdraw.getValue()) {
			Fonts.sfReg18.drawStringWithShadow("FPS:  ", 4, mc.getScaledResolution().getScaledHeight() - 10, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), 0.15).getRGB());
			Fonts.sfReg18.drawStringWithShadow("" + Minecraft.getDebugFPS(), 25, mc.getScaledResolution().getScaledHeight() - 10, -1);
		}

		if (bpsdraw.getValue()) {
			Fonts.sfReg18.drawStringWithShadow("BPS:  ", 50, mc.getScaledResolution().getScaledHeight() - 10, ColorUtil.getColor(Slack.getInstance().getModuleManager().getInstance(Hud.class).theme.getValue(), 0.15).getRGB());
			Fonts.sfReg18.drawStringWithShadow(getBPS(), 71, mc.getScaledResolution().getScaledHeight() - 10, -1);

		}

		if (scaffoldDraw.getValue()) {
			if (Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) {
				if (mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack() != null  && mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack().isStackable()) {
					displayString = mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack().stackSize + " blocks";
				} else {
					displayString = "No blocks";
				}
				if (scaffoldTicks < 10)
					scaffoldTicks++;
			} else {
				if (scaffoldTicks > 0)
					scaffoldTicks--;
			}

			if (scaffoldTicks != 0) {
				ScaledResolution sr = mc.getScaledResolution();
				if (mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack() != null) {
					int y = (int) ((1 - Math.pow(1 - (scaffoldTicks / 10.0), 3)) * 20);
					RenderUtil.drawRoundedRect(
							((sr.getScaledWidth() -  Fonts.apple18.getStringWidth(displayString)) / 2f) - 5,
							sr.getScaledHeight() * 3f / 4F - 5f - y,
							((sr.getScaledWidth() +  Fonts.apple18.getStringWidth(displayString)) / 2f) + 5,
							sr.getScaledHeight() * 3f / 4F + Fonts.apple18.getHeight() + 5f - y,
							3, 0x80000000);
					Fonts.apple18.drawStringWithShadow(displayString, (sr.getScaledWidth() - Fonts.apple18.getStringWidth(displayString)) / 2f, sr.getScaledHeight() * 3f / 4F - y, new Color(255,255,255).getRGB());
				}
			}
		}

		if (itemSpoofDraw.getValue()) {
			if (ItemSpoofUtil.isEnabled) {
				if (itemSpoofTicks < 10)
					itemSpoofTicks++;
			} else {
				if (itemSpoofTicks > 0)
					itemSpoofTicks--;
			}

			if (itemSpoofTicks != 0) {
				ScaledResolution sr = mc.getScaledResolution();
				if (mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack() != null) {
					int y = (int) ((1 - Math.pow(1 - (itemSpoofTicks / 10.0), 3)) * 20);
					RenderUtil.drawRoundedRect(
							sr.getScaledWidth() / 2f - 14,
							sr.getScaledHeight() * 3f / 4F - 14 - y + 30,
							sr.getScaledWidth() / 2f + 14,
							sr.getScaledHeight() * 3f / 4F + 14 - y + 30,
							4, 0x80000000);
					GuiIngame g = new GuiIngame(Minecraft.getMinecraft());
					GL11.glPushMatrix();
					GlStateManager.enableRescaleNormal();
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					RenderHelper.enableGUIStandardItemLighting();
					g.renderHotbarItem(mc.thePlayer.inventory.currentItem, (int) (sr.getScaledWidth() / 2f - 8), (int) (sr.getScaledHeight() * 3f / 4F - 8 - y + 30), 1, mc.thePlayer);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.disableRescaleNormal();
					GlStateManager.disableBlend();
					GL11.glPopMatrix();
				}
			}
		}

		if (notification.getValue()) {
			int y = mc.getScaledResolution().getScaledHeight() - 10;
			for (int i = 0; i < notText.size(); i++) {
				double x = getXpos(notStart.get(i), notEnd.get(i));
				renderNotification((int) (mc.getScaledResolution().getScaledWidth() - 10 + 160 * x), y, notText.get(i), notDetailed.get(i), notStyle.get(i));
				if (roundednotification.getValue()) {
					y -= (int) (Math.pow((1 - x), 0.5) * 23);
				} else {
					y -= (int) (Math.pow((1 - x), 0.5) * 19);
				}
			}

			ArrayList<Integer> removeList = new ArrayList();

			for (int i = 0; i < notText.size(); i++) {
				if (System.currentTimeMillis() > notEnd.get(i)) {
					removeList.add(i);
				}
			}

			Collections.reverse(removeList);

			for (int i : removeList) {
				notText.remove(i);
				notEnd.remove(i);
				notStart.remove(i);
				notDetailed.remove(i);
				notStyle.remove(i);
			}
		} else {
			notText.clear();
			notEnd.clear();
			notStart.clear();
			notDetailed.clear();
			notStyle.clear();
		}
	}

	private String getBPS() {
		double currentBPS = ((double) round((MovementUtil.getSpeed() * 20) * 100)) / 100;
		return String.format("%.2f", currentBPS);
	}

	private void renderNotification(int x, int y, String bigText, String smallText, Slack.NotificationStyle style) {
		int color = new Color(50, 50, 50, 120).getRGB();
		switch (style) {
			case GRAY:
				break;
			case SUCCESS:
				color = new Color(23, 138, 29, 120).getRGB();
				break;
			case FAIL:
				color = new Color(148, 36, 24, 120).getRGB();
				break;
			case WARN:
				color = new Color(156, 128, 37, 120).getRGB();
				break;
		}
		if (roundednotification.getValue()) {
			RenderUtil.drawRoundedRect(
					x - 10 - Fonts.apple18.getStringWidth(bigText),
					y - 10 - Fonts.apple18.getHeight(), x, y,
					2, color);
			Fonts.apple18.drawStringWithShadow(bigText, x - 5 - Fonts.apple18.getStringWidth(bigText),
					y - 5 -Fonts.apple18.getHeight(), new Color(255, 255, 255).getRGB());
			Fonts.apple18.drawStringWithShadow(bigText, x - 5 - Fonts.apple18.getStringWidth(bigText),
					y - 5 - Fonts.apple18.getHeight(), new Color(255, 255, 255).getRGB());
		} else {
			drawRect(x - 6 - Fonts.apple18.getStringWidth(bigText), y - 6 - Fonts.apple18.getHeight(), x, y,
					color);
			Fonts.apple18.drawStringWithShadow(bigText, x - 3 - Fonts.apple18.getStringWidth(bigText),
					y - 3 - Fonts.apple18.getHeight(), new Color(255, 255, 255).getRGB());
		}
	}

	private double getXpos(Long startTime, Long endTime) {
		if (endTime - System.currentTimeMillis() < 300L) {
			return Math.pow(1 - (endTime - System.currentTimeMillis()) / 300f, 3);
		} else if (System.currentTimeMillis() - startTime < 300L) {
			return Math.pow( 1- (System.currentTimeMillis() - startTime) / 300f, 3);
		} else {
			return 0.0;
		}
	}

	public void addNotification(String bigText, String smallText, Long duration, Slack.NotificationStyle style) {
		if (!notification.getValue()) return;
		notText.add(bigText);
		notEnd.add(System.currentTimeMillis() + duration);
		notStart.add(System.currentTimeMillis());
		notDetailed.add(smallText);
		notStyle.add(style);
	}
}
