package src.compression;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import src.error.ErrorMethod;
import src.model.Quadtree;
import src.util.GifGenerator;

public class ImageCompressor {
    private String inputPath;
    private String outputPath;
    private String gifPath;
    private ErrorMethod errorMethod;
    private double threshold;
    private int minBlockSize;
    private double targetCompressionRatio;
    private boolean generateGif;
    private Quadtree quadtree;
    
    public ImageCompressor(
            String inputPath, 
            String outputPath, 
            String gifPath,
            ErrorMethod errorMethod, 
            double threshold, 
            int minBlockSize,
            double targetCompressionRatio) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.gifPath = gifPath;
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.targetCompressionRatio = targetCompressionRatio;
        this.generateGif = gifPath != null && !gifPath.isEmpty();
    }
    
    public CompressionStats compress() throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Load the input image
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputPath);
        }
        
        BufferedImage original = ImageIO.read(inputFile);
        
        // Auto-scale very large images to prevent memory issues
        if (original.getWidth() * original.getHeight() > 10000000) { // > 10MP
            double scale = Math.sqrt(10000000.0 / (original.getWidth() * original.getHeight()));
            int newWidth = (int)(original.getWidth() * scale);
            int newHeight = (int)(original.getHeight() * scale);
            System.out.println("Image is very large, scaling down for processing...");
            original = scaleImage(original, newWidth, newHeight);
        }
        
        // Auto-adjust threshold if needed
        if (targetCompressionRatio > 0) {
            threshold = findOptimalThreshold(original, targetCompressionRatio);
        }
        
        // Create quadtree and compress the image
        this.quadtree = new Quadtree(original, minBlockSize, threshold, errorMethod, generateGif);
        BufferedImage compressed = quadtree.compressImage();
        
        // Save the compressed image
        File outputFile = new File(outputPath);
        String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
        ImageIO.write(compressed, format, outputFile);
        
        // Generate GIF if requested
        if (generateGif && quadtree.getCompressionSteps() != null) {
            GifGenerator.createGif(quadtree.getCompressionSteps(), gifPath);
        }
        
        // Return compression stats
        long endTime = System.currentTimeMillis();
        return new CompressionStats(
            inputFile.length(),
            outputFile.length(),
            quadtree.getDepth(),
            quadtree.getNodeCount(),
            endTime - startTime
        );
    }
    
    private double findOptimalThreshold(BufferedImage original, double targetRatio) {
        // Setup threshold search range
        double minThreshold = 0;
        double maxThreshold = 1000;
        double currentThreshold = (minThreshold + maxThreshold) / 2;
        double currentRatio;
        int maxIterations = 8; // Reduced from 10 to improve performance
        
        // Scale down image for faster testing
        BufferedImage testImage = scaleDown(original, 2);
        int testBlockSize = Math.max(1, minBlockSize / 2);
        
        for (int i = 0; i < maxIterations; i++) {
            // Create a test quadtree
            Quadtree testTree = new Quadtree(testImage, testBlockSize, currentThreshold, errorMethod, false);
            
            // Check compression ratio
            currentRatio = 1.0 - (double) testTree.getNodeCount() / (testImage.getWidth() * testImage.getHeight());
            
            // If close enough, we're done
            if (Math.abs(currentRatio - targetRatio) < 0.05) {
                break;
            } 
            // Otherwise adjust the threshold
            else if (currentRatio < targetRatio) {
                minThreshold = currentThreshold;
            } else {
                maxThreshold = currentThreshold;
            }
            
            currentThreshold = (minThreshold + maxThreshold) / 2;
            
            // Help avoid memory issues
            System.gc();
        }
        
        return currentThreshold;
    }
    
    private BufferedImage scaleDown(BufferedImage original, int factor) {
        int width = original.getWidth() / factor;
        int height = original.getHeight() / factor;
        return scaleImage(original, width, height);
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        // For large images, use a more memory-efficient image type
        int imageType = (width * height > 4000000) ? 
                        BufferedImage.TYPE_3BYTE_BGR : 
                        BufferedImage.TYPE_INT_RGB;
                        
        BufferedImage scaled = new BufferedImage(width, height, imageType);
        
        Graphics2D g = scaled.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        
        return scaled;
    }
}