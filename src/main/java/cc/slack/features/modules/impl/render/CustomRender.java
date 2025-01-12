package cc.slack.features.modules.impl.render;

import cc.slack.start.Slack;
import cc.slack.events.impl.render.RenderEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.utils.font.Fonts;
import cc.slack.utils.font.MCFontRenderer;
import cc.slack.utils.render.RenderUtil;
import cc.slack.utils.other.FileUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "CustomRender",
        category = Category.RENDER
)
public class CustomRender extends Module {

    private final File directory = new File(Minecraft.getMinecraft().mcDataDir, "/" + "SlackClient" + "/configs");
    private final String fileName = "custom_render.txt"; // Configuration file name.

    public CustomRender() {
        if (!directory.exists()) {
            directory.mkdirs(); // Ensure the directory exists.
        }
    }

    @Listen
    public void onRender(RenderEvent event) {
        List<RectangleObject> rectangles = new ArrayList<>();
        List<TextObject> texts = new ArrayList<>();

        // Read the configuration file.
        File configFile = new File(directory, fileName);
        if (!configFile.exists()) {
            return; // Exit if the configuration file is missing.
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 9) {
                    // Rectangle: x, y, width, height, round, r, g, b, a
                    rectangles.add(new RectangleObject(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            new Color(
                                    Integer.parseInt(parts[5]),
                                    Integer.parseInt(parts[6]),
                                    Integer.parseInt(parts[7]),
                                    Integer.parseInt(parts[8])
                            )
                    ));
                } else if (parts.length == 8) {
                    // Text: x, y, r, g, b, a, content
                    texts.add(new TextObject(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]),
                            new Color(
                                    Integer.parseInt(parts[3]),
                                    Integer.parseInt(parts[4]),
                                    Integer.parseInt(parts[5]),
                                    Integer.parseInt(parts[6])
                            ),
                            parts[7]
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle malformed configurations gracefully.
        }

        // Draw rectangles.
        for (RectangleObject rectangle : rectangles) {
            rectangle.draw();
        }

        // Draw texts.
        for (TextObject text : texts) {
            text.draw();
        }
    }

    private static class RectangleObject {
        private final int x, y, width, height, round;
        private final Color color;

        public RectangleObject(int x, int y, int width, int height, int round, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.round = round;
            this.color = color;
        }

        public void draw() {
            RenderUtil.drawRoundedRect(x, y, x + width, y + height, round,  color.getRGB());
        }
    }

    private static class TextObject {
        private final int x, y, size;
        private final Color color;
        private final String content;

        public TextObject(int x, int y, int size, Color color, String content) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.content = content;
        }

        public void draw() {
            Fonts.getFontRenderer("Modern", size).drawString(content, x, y, color.getRGB());
        }
    }
}
