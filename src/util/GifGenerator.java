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

public class GifGenerator {
    
    public static void createGif(List<BufferedImage> frames, String outputPath) throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("No frames provided");
        }
        
        // Get GIF writer
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        
        // Setup output
        File outputFile = new File(outputPath);
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);
        
        // Configure GIF parameters
        ImageWriteParam params = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpec = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        
        // Setup animation metadata
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpec, params);
        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
        
        // Configure frame display settings
        IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "FALSE");
        gce.setAttribute("delayTime", "50");  // 0.5 sec delay
        gce.setAttribute("transparentColorIndex", "0");
        
        // Set up animation looping
        IIOMetadataNode appExtensions = getNode(root, "ApplicationExtensions");
        IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
        appExt.setAttribute("applicationID", "NETSCAPE");
        appExt.setAttribute("authenticationCode", "2.0");
        appExt.setUserObject(new byte[] { 1, 0, 0 });  // Loop count
        appExtensions.appendChild(appExt);
        
        metadata.setFromTree(metaFormat, root);
        
        // Write all frames
        writer.prepareWriteSequence(null);
        for (BufferedImage frame : frames) {
            writer.writeToSequence(new IIOImage(frame, null, metadata), params);
        }
        
        // Clean up resources
        writer.endWriteSequence();
        ios.close();
        writer.dispose();
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