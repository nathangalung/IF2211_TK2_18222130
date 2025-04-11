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
    
    // Constructor
    public ImageCompressor(
            String inputName, 
            String outputName, 
            String gifName,
            ErrorMethod errorMethod, 
            double threshold, 
            int minBlockSize,
            double targetCompressionRatio) {
        
        // Use paths as provided directly
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.gifPath = gifPath;
        
        // Ensure directory exists for output path
        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
        
        // Ensure directories exist for gif path
        if (gifPath != null && !gifPath.isEmpty()) {
            File gifFile = new File(gifPath);
            if (gifFile.getParentFile() != null) {
                gifFile.getParentFile().mkdirs();
            }
        }
        
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.targetCompressionRatio = targetCompressionRatio;
        this.generateGif = gifPath != null && !gifPath.isEmpty();
    }
    
    // Main compression process
    public CompressionStats compress() throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Load image
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputPath);
        }
        
        BufferedImage original = ImageIO.read(inputFile);
        
        // Scale large images
        if (original.getWidth() * original.getHeight() > 10000000) { // > 10MP
            double scale = Math.sqrt(10000000.0 / (original.getWidth() * original.getHeight()));
            int newWidth = (int)(original.getWidth() * scale);
            int newHeight = (int)(original.getHeight() * scale);
            System.out.println("Image is very large, scaling down for processing...");
            original = scaleImage(original, newWidth, newHeight);
        }
        
        // Auto-adjust threshold
        if (targetCompressionRatio > 0) {
            threshold = findOptimalThreshold(original, targetCompressionRatio);
        }
        
        // Create quadtree
        this.quadtree = new Quadtree(original, minBlockSize, threshold, errorMethod, generateGif);
        BufferedImage compressed = quadtree.compressImage();
        
        // Save output
        File outputFile = new File(outputPath);
        String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
        ImageIO.write(compressed, format, outputFile);
        
        // Create GIF
        if (generateGif && quadtree.getCompressionSteps() != null) {
            GifGenerator.createGif(quadtree.getCompressionSteps(), gifPath);
        }
        
        // Return stats
        long endTime = System.currentTimeMillis();
        return new CompressionStats(
            inputFile.length(),
            outputFile.length(),
            quadtree.getDepth(),
            quadtree.getNodeCount(),
            endTime - startTime
        );
    }
    
    // Find best threshold
    private double findOptimalThreshold(BufferedImage original, double targetRatio) {
        // Set search range
        double minThreshold = 0;
        double maxThreshold = 1000;
        double currentThreshold = (minThreshold + maxThreshold) / 2;
        double currentRatio;
        int maxIterations = 8;
        
        // Smaller test image
        BufferedImage testImage = scaleDown(original, 2);
        int testBlockSize = Math.max(1, minBlockSize / 2);
        
        for (int i = 0; i < maxIterations; i++) {
            // Test compression
            Quadtree testTree = new Quadtree(testImage, testBlockSize, currentThreshold, errorMethod, false);
            
            // Check ratio
            currentRatio = 1.0 - (double) testTree.getNodeCount() / (testImage.getWidth() * testImage.getHeight());
            
            // Close enough
            if (Math.abs(currentRatio - targetRatio) < 0.05) {
                break;
            } 
            // Adjust threshold
            else if (currentRatio < targetRatio) {
                minThreshold = currentThreshold;
            } else {
                maxThreshold = currentThreshold;
            }
            
            currentThreshold = (minThreshold + maxThreshold) / 2;
            
            // Free memory
            System.gc();
        }
        
        return currentThreshold;
    }
    
    // Scale by factor
    private BufferedImage scaleDown(BufferedImage original, int factor) {
        int width = original.getWidth() / factor;
        int height = original.getHeight() / factor;
        return scaleImage(original, width, height);
    }

    // Resize image
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        // Choose format
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