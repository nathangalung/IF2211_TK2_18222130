package src.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Utility class for generating GIFs from image sequences
 */
public class GifGenerator {
    
    /**
     * Create a GIF from a list of BufferedImages
     * 
     * @param frames List of image frames
     * @param outputPath Path for output GIF
     * @throws IOException If there is an error writing the GIF
     */
    public static void createGif(List<BufferedImage> frames, String outputPath) throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("No frames provided for GIF creation");
        }
        
        // Get GIF writer
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        
        // Create output stream
        File outputFile = new File(outputPath);
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);
        
        // Set up parameters
        ImageWriteParam params = writer.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        
        // Prepare metadata for animation
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params);
        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
        
        // Set GIF parameters
        IIOMetadataNode graphicsControlExtension = getNode(root, "GraphicControlExtension");
        graphicsControlExtension.setAttribute("disposalMethod", "none");
        graphicsControlExtension.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtension.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtension.setAttribute("delayTime", "50"); // 0.5 seconds delay
        graphicsControlExtension.setAttribute("transparentColorIndex", "0");
        
        IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appExtension = new IIOMetadataNode("ApplicationExtension");
        appExtension.setAttribute("applicationID", "NETSCAPE");
        appExtension.setAttribute("authenticationCode", "2.0");
        
        // Set loop count (0 for infinite)
        byte[] loopBytes = new byte[] { 1, 0, 0 };
        appExtension.setUserObject(loopBytes);
        appExtensions.appendChild(appExtension);
        
        metadata.setFromTree(metaFormat, root);
        
        // Write the first image with metadata
        writer.prepareWriteSequence(null);
        
        // Write all frames
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            writer.writeToSequence(new IIOImage(frame, null, metadata), params);
        }
        
        // Finish and close
        writer.endWriteSequence();
        ios.close();
        writer.dispose();
    }
    
    /**
     * Helper method to get or create a node in the metadata tree
     */
    private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
        IIOMetadataNode node = null;
        
        int numNodes = root.getLength();
        for (int i = 0; i < numNodes; i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                node = (IIOMetadataNode) root.item(i);
                break;
            }
        }
        
        if (node == null) {
            node = new IIOMetadataNode(nodeName);
            root.appendChild(node);
        }
        
        return node;
    }
}