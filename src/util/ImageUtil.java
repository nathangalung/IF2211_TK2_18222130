package src.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Utility class for image operations
 */
public class ImageUtil {
    
    /**
     * Check if a file is a valid image
     * 
     * @param path Path to the image file
     * @return true if valid, false otherwise
     */
    public static boolean isValidImageFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            
            BufferedImage image = ImageIO.read(file);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get image dimensions
     * 
     * @param path Path to the image file
     * @return int array with [width, height]
     * @throws IOException If there is an error reading the image
     */
    public static int[] getImageDimensions(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return new int[] { image.getWidth(), image.getHeight() };
    }
    
    /**
     * Create a deep copy of a BufferedImage
     * 
     * @param source Source image
     * @return New copy of the image
     */
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return copy;
    }
    
    /**
     * Draw quadtree grid lines on an image for visualization
     * 
     * @param image Image to draw on
     * @param x X position
     * @param y Y position
     * @param width Block width
     * @param height Block height
     * @param color Line color
     */
    public static void drawQuadtreeGrid(BufferedImage image, int x, int y, int width, int height, Color color) {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.drawRect(x, y, width, height);
        g2d.dispose();
    }
}