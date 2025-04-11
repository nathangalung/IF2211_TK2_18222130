package src.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtil {
    
    // Check file is image
    public static boolean isValidImageFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            
            return ImageIO.read(file) != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    // Get width and height
    public static int[] getImageDimensions(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return new int[] { image.getWidth(), image.getHeight() };
    }
    
    // Clone image
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }
    
    // Draw grid lines
    public static void drawQuadtreeGrid(BufferedImage image, int x, int y, int width, int height, Color color) {
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.drawRect(x, y, width, height);
        g.dispose();
    }
}