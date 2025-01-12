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
import cc.slack.utils.other.TimeUtil;
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
import net.minecraft.item.ItemStack;
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
	public final BooleanValue roundednotification = new BooleanValue("Old Notifications", true);


	// Counters
	private final BooleanValue fpsdraw = new BooleanValue("FPS Counter", true);
	private final BooleanValue bpsdraw = new BooleanValue("BPS Counter", true);


	// Scaffold HUD
	private final BooleanValue scaffoldDraw = new BooleanValue("Scaffold Counter", true);

	private final BooleanValue itemSpoofDraw = new BooleanValue("ItemSpoof indicator", true);

	private final BooleanValue centerNotification = new BooleanValue("Center Notification", true);

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
	private double scaffoldTicks = 0;
	private double itemSpoofTicks = 0;

	private double centerTicks = 0;
	private String centerTitle = " ";
	private int centerMode = 0; // 0 = text, 1 = bar
	private String centerContent = " ";
	private double centerProgress = 0;
	private int centerTimeout = 0;
	private TimeUtil centerTimer = new TimeUtil();

	private String displayString = " ";
	private ArrayList<String> notText = new ArrayList<>();
	private ArrayList<Long> notEnd = new ArrayList<>();
	private ArrayList<Long> notStart = new ArrayList<>();
	private ArrayList<String> notDetailed = new ArrayList<>();
	private ArrayList<Slack.NotificationStyle> notStyle = new ArrayList<>();
	private int oldStack;

	public Hud() {
		addSettings(arraylist, arraylistMode, arraylistResetPos, modernArraylistMode,arraylistsidebar,arraylistFont, arraylistBackground ,tags, tagsMode, // arraylist
				watermark,watermarksmodes, // watermark
				notification, roundednotification, // notification
				fpsdraw, bpsdraw, scaffoldDraw, itemSpoofDraw, centerNotification, // draws
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
					scaffoldTicks += 130.0/Minecraft.getDebugFPS();
				scaffoldTicks = Math.min(10, scaffoldTicks);
			} else {
				if (scaffoldTicks > 0)
					scaffoldTicks -= 130.0/Minecraft.getDebugFPS();
			}

			if (scaffoldTicks > 0) {
				ScaledResolution sr = mc.getScaledResolution();
				if (mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack() != null) {
					int y = (int) ((1 - Math.pow(1 - (scaffoldTicks / 10.0), 3)) * 20);
					RenderUtil.drawRoundedRect(
							((sr.getScaledWidth() -  Fonts.sfRoundedBold18.getStringWidth(displayString)) / 2f) - 5,
							sr.getScaledHeight() * 3f / 4F - 5f - y,
							((sr.getScaledWidth() +  Fonts.sfRoundedBold18.getStringWidth(displayString)) / 2f) + 5,
							sr.getScaledHeight() * 3f / 4F + Fonts.sfRoundedBold18.getHeight() + 5f - y,
							3, 0x80000000);
					Fonts.sfRoundedBold18.drawStringWithShadow(displayString, (sr.getScaledWidth() - Fonts.sfRoundedBold18.getStringWidth(displayString)) / 2f, sr.getScaledHeight() * 3f / 4F - y, new Color(255,255,255).getRGB());
				}
			}
		}

		if (itemSpoofDraw.getValue()) {
			if (ItemSpoofUtil.isEnabled) {
				if (itemSpoofTicks < 10)
					itemSpoofTicks += 130.0/Minecraft.getDebugFPS();
				itemSpoofTicks = Math.min(10, itemSpoofTicks);
			} else {
				if (itemSpoofTicks > 0)
					itemSpoofTicks -= 130.0/Minecraft.getDebugFPS();
			}

			if (itemSpoofTicks > 0) {
				ScaledResolution sr = mc.getScaledResolution();
				if (mc.thePlayer.inventoryContainer.getSlot(mc.thePlayer.inventory.currentItem + 36).getStack() != null) {
					if (ItemSpoofUtil.isEnabled) {
						oldStack = mc.thePlayer.inventory.currentItem;
					}
					int y = (int) ((1 - Math.pow(1 - (itemSpoofTicks / 10.0), 3)) * 20);
					RenderUtil.drawRoundedRect(
							sr.getScaledWidth() / 2f - 14,
							sr.getScaledHeight() * 3f / 4F - 14 - y + 30,
							sr.getScaledWidth() / 2f + 14,
							sr.getScaledHeight() * 3f / 4F + 14 - y + 30,
							4, new Color(30, 30, 30, 2 + (int) (253 * itemSpoofTicks / 10.0)).getRGB());
					GuiIngame g = new GuiIngame(Minecraft.getMinecraft());
					GL11.glPushMatrix();
					GlStateManager.enableRescaleNormal();
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					RenderHelper.enableGUIStandardItemLighting();
					g.renderHotbarItem(oldStack, (int) (sr.getScaledWidth() / 2f - 8), (int) (sr.getScaledHeight() * 3f / 4F - 8 - y + 30), 1, mc.thePlayer);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.disableRescaleNormal();
					GlStateManager.disableBlend();
					GL11.glPopMatrix();
				}
			}
		}

		if (centerNotification.getValue()) {
			if (!centerTimer.hasReached(centerTimeout)) {
				if (centerTicks < 10)
					centerTicks += 130.0/Minecraft.getDebugFPS();
				centerTicks = Math.min(10, centerTicks);
			} else {
				if (centerTicks > 0)
					centerTicks -= 130.0/Minecraft.getDebugFPS();
			}

			if (centerTicks > 0) {
				ScaledResolution sr = mc.getScaledResolution();
				int w = sr.getScaledWidth() / 2;
				double y = 30;
				switch (centerMode) {
					case 0:
						double width = Math.max(Fonts.sfRoundedBold18.getStringWidth(centerTitle), Fonts.sfRoundedBold20.getStringWidth(centerContent))/2.0 + 5;

						RenderUtil.drawRoundedRect(w - width, y, w + width, y + 20, 5, ColorUtil.getMaterial(false).getRGB());
						RenderUtil.drawRoundedRect(w - width, y + 14, w + width, y + 20, 0, ColorUtil.getMaterial(true).getRGB()); // Overlapping rectangle
						RenderUtil.drawRoundedRect(w - width, y + 15, w + width, y + 28, 5, ColorUtil.getMaterial(true).getRGB());

						Fonts.sfRoundedBold18.drawCenteredString(centerTitle, w, y + 4, -1);
						Fonts.sfRoundedBold20.drawCenteredString(centerContent, w, y + 14 + 5, -1);
						break;
					case 1:
						width = Fonts.sfRoundedBold18.getStringWidth(centerTitle);

						RenderUtil.drawRoundedRect(w - width, y, w + width, y + 20, 5, ColorUtil.getMaterial(false).getRGB());
						RenderUtil.drawRoundedRect(w - width, y + 14, w + width, y + 17, 0, ColorUtil.getMaterial(true).getRGB()); // Overlapping rectangle
						RenderUtil.drawRoundedRect(w - width, y + 15, w + width, y + 22, 5, ColorUtil.getMaterial(true).getRGB());

						Fonts.sfRoundedBold18.drawCenteredString(centerTitle, w, y + 4, -1);
						RenderUtil.drawRoundedRect(w - width + 2, y + 16, w + width - 2, y + 20, 3, ColorUtil.getMaterial(false).getRGB());
						RenderUtil.drawRoundedRect(w - width + 2, y + 16, w - width + 2 + (centerProgress * (width * 2 - 4)), y + 20, 3, new Color(55, 55, 55, 255).getRGB());

				}
			}
		}

		if (notification.getValue()) {
			int y = mc.getScaledResolution().getScaledHeight() - 10;
			for (int i = 0; i < notText.size(); i++) {
				double x = getXpos(notStart.get(i), notEnd.get(i));
				double progress = (System.currentTimeMillis() - notStart.get(i))/(notEnd.get(i) - notStart.get(i) * 1.0);
				renderNotification((int) (mc.getScaledResolution().getScaledWidth() - 5 + 160 * x), y, notText.get(i), notDetailed.get(i), notStyle.get(i), progress);
				if (roundednotification.getValue()) {
					y -= (int) (Math.pow((1 - x), 0.4) * 32);
				} else {
					y -= (int) (Math.pow((1 - x), 0.4) * 32);
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

	private void renderNotification(int x, int y, String bigText, String smallText, Slack.NotificationStyle style, double progress) {
		int color = ColorUtil.getMaterial(false).getRGB();
		if (roundednotification.getValue()) {
			switch (style) {
				case GRAY:
					break;
				case SUCCESS:
					color = new Color(24, 37, 23, 255).getRGB();
					break;
				case FAIL:
					color = new Color(39, 25, 25, 255).getRGB();
					break;
				case WARN:
					color = new Color(35, 32, 22, 255).getRGB();
					break;
			}
			RenderUtil.drawRoundedRect(
					x - 20 - Fonts.sfRoundedBold18.getStringWidth(bigText),
					y - 15 - Fonts.sfRoundedBold18.getHeight(), x, y + 5,
					3, color);
			Fonts.sfRoundedBold18.drawStringWithShadow(bigText, x - 14 - Fonts.sfRoundedBold18.getStringWidth(bigText),
					y - 5 - Fonts.sfRoundedBold18.getHeight(), new Color(255, 255, 255).getRGB());
		} else {
			switch (style) {
				case GRAY:
					break;
				case SUCCESS:
					color = new Color(9, 151, 0, 255).getRGB();
					break;
				case FAIL:
					color = new Color(142, 0, 0, 255).getRGB();
					break;
				case WARN:
					color = new Color(129, 102, 0, 255).getRGB();
					break;
			}
			RenderUtil.drawRoundedRect(
					x - 20 - Fonts.sfRoundedBold18.getStringWidth(bigText),
					y - 15 - Fonts.sfRoundedBold18.getHeight(), x, y + 5,
					3, new Color(34, 34, 34, 255).getRGB());
			RenderUtil.drawRoundedRect(
					x - (20 - Fonts.sfRoundedBold18.getStringWidth(bigText)),
					y + 4, x - ((20 - Fonts.sfRoundedBold18.getStringWidth(bigText)) * progress), y + 5,
					2, color);
			Fonts.sfRoundedBold18.drawStringWithShadow(bigText, x - 14 - Fonts.sfRoundedBold18.getStringWidth(bigText),
					y - 5 - Fonts.sfRoundedBold18.getHeight(), new Color(255, 255, 255).getRGB());
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
