package src.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class GifGenerator {
    
    public static void createGif(List<BufferedImage> frames, String outputPath) throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("No frames provided");
        }
        
        System.out.println("Creating GIF with " + frames.size() + " frames...");
        
        // Get image size
        BufferedImage firstFrame = frames.get(0);
        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();
        
        // Scale large images
        double scale = 1.0;
        if (width * height > 1000000) { // > 1MP
            scale = Math.sqrt(1000000.0 / (width * height));
            System.out.println("Scaling GIF to " + (int)(scale * 100) + "% to fit memory constraints");
        }
        
        // Process frames
        List<BufferedImage> processedFrames = preprocessFrames(frames, scale);
        System.out.println("Processed " + processedFrames.size() + " frames for GIF");
        
        // Get GIF writer
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        
        // Setup output
        File outputFile = new File(outputPath);
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);
        
        // Set GIF params
        ImageWriteParam params = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpec = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        
        // Setup animation
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpec, params);
        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
        
        // Set frame timing
        IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", "30");  // 0.3 sec
        gce.setAttribute("transparentColorIndex", "0");
        
        // Enable looping
        IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
        appExt.setAttribute("applicationID", "NETSCAPE");
        appExt.setAttribute("authenticationCode", "2.0");
        appExt.setUserObject(new byte[] { 1, 0, 0 });
        appExtensions.appendChild(appExt);
        
        metadata.setFromTree(metaFormat, root);
        
        // Final frame timing
        IIOMetadata finalFrameMetadata = writer.getDefaultImageMetadata(typeSpec, params);
        IIOMetadataNode finalRoot = (IIOMetadataNode) finalFrameMetadata.getAsTree(metaFormat);
        IIOMetadataNode finalGce = getNode(finalRoot, "GraphicControlExtension");
        finalGce.setAttribute("disposalMethod", "none");
        finalGce.setAttribute("userInputFlag", "FALSE");
        finalGce.setAttribute("transparentColorFlag", "FALSE");
        finalGce.setAttribute("delayTime", "300");  // 3 sec
        finalGce.setAttribute("transparentColorIndex", "0");
        
        // Final frame looping
        IIOMetadataNode finalAppExtensions = getNode(finalRoot, "ApplicationExtensions");
        IIOMetadataNode finalAppExt = new IIOMetadataNode("ApplicationExtension");
        finalAppExt.setAttribute("applicationID", "NETSCAPE");
        finalAppExt.setAttribute("authenticationCode", "2.0");
        finalAppExt.setUserObject(new byte[] { 1, 0, 0 });
        finalAppExtensions.appendChild(finalAppExt);
        
        finalFrameMetadata.setFromTree(metaFormat, finalRoot);
        
        // Start sequence
        writer.prepareWriteSequence(null);
        
        System.out.println("Writing frames to GIF...");
        // Write normal frames
        for (int i = 0; i < processedFrames.size() - 1; i++) {
            writer.writeToSequence(new IIOImage(processedFrames.get(i), null, metadata), params);
            
            // Clear memory
            if (i % 5 == 0 && width * height > 500000) {
                processedFrames.set(i, null);
                System.gc();
            }
        }
        
        // Write final frame
        if (processedFrames.size() > 0) {
            writer.writeToSequence(
                new IIOImage(processedFrames.get(processedFrames.size() - 1), null, finalFrameMetadata), 
                params
            );
        }
        
        // Clean up
        writer.endWriteSequence();
        ios.close();
        writer.dispose();
        
        System.out.println("GIF created successfully at: " + outputPath);
    }
    
    private static List<BufferedImage> preprocessFrames(List<BufferedImage> originalFrames, double scale) {
        if (originalFrames.size() <= 2) {
            return originalFrames; // Too few
        }
        
        // Get key frames
        BufferedImage firstFrame = originalFrames.get(0);
        BufferedImage lastFrame = originalFrames.get(originalFrames.size() - 1);
        
        // Create result list
        List<BufferedImage> result = new ArrayList<>();
        
        // Limit frame count
        int maxFrames = Math.min(25, originalFrames.size());
        int step = originalFrames.size() / maxFrames;
        if (step < 1) step = 1;
        
        // Add first frame
        result.add(scaleImage(firstFrame, scale));
        
        // Add middle frames
        for (int i = step; i < originalFrames.size() - 1; i += step) {
            result.add(scaleImage(originalFrames.get(i), scale));
        }
        
        // Add final frame
        result.add(scaleImage(lastFrame, scale));
        
        // Add frame info
        return addFrameInfo(result);
    }
    
    private static BufferedImage scaleImage(BufferedImage source, double scale) {
        // Skip if no scaling
        if (scale >= 0.99 || scale <= 0) {
            return source;
        }
        
        int newWidth = (int)(source.getWidth() * scale);
        int newHeight = (int)(source.getHeight() * scale);
        
        // Choose image type
        int imageType = (newWidth * newHeight > 1000000) ? 
                        BufferedImage.TYPE_3BYTE_BGR : 
                        BufferedImage.TYPE_INT_RGB;
                        
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, imageType);
        
        Graphics2D g = scaled.createGraphics();
        g.drawImage(source, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return scaled;
    }
    
    private static List<BufferedImage> addFrameInfo(List<BufferedImage> frames) {
        // Add overlays
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            Graphics2D g = frame.createGraphics();
            
            // Info box
            int boxWidth = 200;
            int boxHeight = 50;
            int boxX = 10;
            int boxY = 10;
            
            // Draw background
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(boxX, boxY, boxWidth, boxHeight);
            
            // Draw frame info
            g.setColor(Color.WHITE);
            String frameInfo = "Frame " + (i+1) + " of " + frames.size();
            g.drawString(frameInfo, boxX + 10, boxY + 20);
            
            // Show progress
            String progressLabel = i == 0 ? "Original Image" : 
                                  i == frames.size()-1 ? "Final Compression" : 
                                  "Quadtree Formation " + Math.round((i/(float)(frames.size()-1))*100) + "%";
            g.drawString(progressLabel, boxX + 10, boxY + 40);
            
            // Progress bar
            int barWidth = boxWidth - 20;
            int barHeight = 8;
            int barX = boxX + 10;
            int barY = boxY + boxHeight - barHeight - 5;
            
            // Bar background
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);
            
            // Progress fill
            g.setColor(Color.GREEN);
            int fillWidth = (int)(barWidth * (i / (float)(frames.size() - 1)));
            g.fillRect(barX, barY, fillWidth, barHeight);
            
            g.dispose();
        }
        
        return frames;
    }
    
    private static IIOMetadataNode getNode(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}