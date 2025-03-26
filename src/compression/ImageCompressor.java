package src.compression;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import src.error.ErrorMethod;
import src.model.Quadtree;
import src.util.GifGenerator;
import src.util.ImageUtil;

/**
 * Main class for image compression using quadtree
 */
public class ImageCompressor {
    private String inputPath;
    private String outputPath;
    private String gifPath;
    private ErrorMethod errorMethod;
    private double threshold;
    private int minBlockSize;
    private double targetCompressionRatio;
    private boolean generateGif;
    private Quadtree quadtree; // Add this field to store the quadtree
    
    /**
     * Constructor for ImageCompressor
     * 
     * @param inputPath Path to input image
     * @param outputPath Path for output image
     * @param gifPath Path for output GIF
     * @param errorMethod Error calculation method
     * @param threshold Error threshold
     * @param minBlockSize Minimum block size
     * @param targetCompressionRatio Target compression ratio (0 to disable)
     */
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
    
    /**
     * Run the compression process
     * 
     * @return CompressionStats with results
     * @throws IOException If there is an error reading or writing the images
     */
    public CompressionStats compress() throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Load the input image
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputPath);
        }
        
        BufferedImage original = ImageIO.read(inputFile);
        
        // If target compression ratio is specified, adjust threshold automatically
        if (targetCompressionRatio > 0) {
            threshold = findOptimalThreshold(original, targetCompressionRatio);
        }
        
        // Create quadtree and compress the image
        this.quadtree = new Quadtree(original, minBlockSize, threshold, errorMethod, generateGif);
        BufferedImage compressed = quadtree.compressImage(); // FIX: Call from quadtree instance
        
        // Save the compressed image
        File outputFile = new File(outputPath);
        String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
        ImageIO.write(compressed, format, outputFile);
        
        // Generate GIF if requested
        if (generateGif && quadtree.getCompressionSteps() != null) { // FIX: Call from quadtree instance
            GifGenerator.createGif(quadtree.getCompressionSteps(), gifPath); // FIX: Call from quadtree instance
        }
        
        long endTime = System.currentTimeMillis();
        
        // Create and return compression statistics
        return new CompressionStats(
            inputFile.length(),
            outputFile.length(),
            quadtree.getDepth(), // FIX: Call from quadtree instance
            quadtree.getNodeCount(), // FIX: Call from quadtree instance
            endTime - startTime
        );
    }
    
    /**
     * Find optimal threshold to achieve target compression ratio
     * This is a simplified implementation using binary search
     * 
     * @param original Original image
     * @param targetRatio Target compression ratio
     * @return Optimal threshold
     */
    private double findOptimalThreshold(BufferedImage original, double targetRatio) {
        double minThreshold = 0;
        double maxThreshold = 1000; // Arbitrary upper bound, could be adjusted based on error method
        double currentThreshold = (minThreshold + maxThreshold) / 2;
        double currentRatio;
        int maxIterations = 10; // Limit iterations to prevent infinite loop
        
        for (int i = 0; i < maxIterations; i++) {
            // Create temporary quadtree with current threshold
            Quadtree tempTree = new Quadtree(original, minBlockSize, currentThreshold, errorMethod, false);
            BufferedImage tempCompressed = tempTree.compressImage();
            
            // Estimate compression ratio (using node count as proxy)
            currentRatio = 1.0 - (double) tempTree.getNodeCount() / (original.getWidth() * original.getHeight());
            
            // Adjust threshold based on current ratio vs target
            if (Math.abs(currentRatio - targetRatio) < 0.01) {
                // Close enough
                break;
            } else if (currentRatio < targetRatio) {
                // Need more compression, increase threshold
                minThreshold = currentThreshold;
            } else {
                // Too much compression, decrease threshold
                maxThreshold = currentThreshold;
            }
            
            // Update threshold
            currentThreshold = (minThreshold + maxThreshold) / 2;
        }
        
        return currentThreshold;
    }
}