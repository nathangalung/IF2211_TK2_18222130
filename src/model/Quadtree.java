package src.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import src.error.ErrorCalculator;
import src.error.ErrorMethod;

/**
 * Quadtree implementation for image compression
 */
public class Quadtree {
    private Node root;
    private int minBlockSize;
    private double threshold;
    private ErrorMethod errorMethod;
    private int depth;
    private int nodeCount;
    private BufferedImage originalImage;
    private List<BufferedImage> compressionSteps; // For GIF generation
    private int stepCounter = 0; // Counter to limit frame capture
    private static final int MAX_GIF_FRAMES = 30; // Maximum number of frames for GIF

    /**
     * Constructor for Quadtree
     * 
     * @param image Original image to compress
     * @param minBlockSize Minimum block size
     * @param threshold Error threshold
     * @param errorMethod Error calculation method
     * @param captureSteps Whether to capture steps for GIF
     */
    public Quadtree(BufferedImage image, int minBlockSize, double threshold, ErrorMethod errorMethod, boolean captureSteps) {
        this.minBlockSize = minBlockSize;
        this.threshold = threshold;
        this.errorMethod = errorMethod;
        this.depth = 0;
        this.nodeCount = 0;
        this.originalImage = image;
        this.compressionSteps = captureSteps ? new ArrayList<>() : null;
        
        // Add initial image to steps if capturing
        if (captureSteps) {
            // Create initial image (blank or original)
            BufferedImage initialImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = initialImage.createGraphics();
            g2d.setColor(Color.WHITE); // Or average color of original
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.dispose();
            
            compressionSteps.add(initialImage);
        }
        
        // Build the quadtree
        this.root = buildTree(image, 0, 0, image.getWidth(), image.getHeight(), 0);
        
        // Capture the final compressed image for GIF if needed
        if (captureSteps) {
            BufferedImage finalImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            renderQuadtree(finalImage, root);
            compressionSteps.add(finalImage);
        }
    }

    /**
     * Build the quadtree recursively
     */
    private Node buildTree(BufferedImage image, int x, int y, int width, int height, int currentDepth) {
        // Increment node count
        this.nodeCount++;
        
        // Update tree depth if needed
        if (currentDepth > this.depth) {
            this.depth = currentDepth;
        }
        
        // Calculate average color for this block
        int[] avgColor = ErrorCalculator.calculateAvgColor(image, x, y, width, height);
        
        // Calculate error using the specified method
        double error = ErrorCalculator.calculateError(image, x, y, width, height, errorMethod);
        
        // Create a new node
        Node node = new Node(x, y, width, height, avgColor, error);
        
        // Check if we need to split further
        if (error > threshold && width > minBlockSize && height > minBlockSize) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            
            // Create four child nodes
            Node topLeft = buildTree(image, x, y, halfWidth, halfHeight, currentDepth + 1);
            Node topRight = buildTree(image, x + halfWidth, y, halfWidth, halfHeight, currentDepth + 1);
            Node bottomLeft = buildTree(image, x, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            Node bottomRight = buildTree(image, x + halfWidth, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            
            // Link children to parent
            node.split(topLeft, topRight, bottomLeft, bottomRight);
            
            // Capture intermediate step for GIF if enabled - but only occasionally to save memory
            if (compressionSteps != null && currentDepth <= 6 && stepCounter % 5 == 0 && 
                compressionSteps.size() < MAX_GIF_FRAMES) {
                
                BufferedImage stepImage = new BufferedImage(
                    originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                
                renderQuadtreePartial(stepImage, root);
                compressionSteps.add(stepImage);
            }
            stepCounter++;
        }
        
        return node;
    }

    /**
     * Create a compressed representation of the original image
     * 
     * @return Compressed image
     */
    public BufferedImage compressImage() {
        BufferedImage compressed = new BufferedImage(
            originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        renderQuadtree(compressed, root);
        return compressed;
    }

    /**
     * Render the quadtree to an image
     */
    private void renderQuadtree(BufferedImage image, Node node) {
        if (node == null) {
            // Handle null node gracefully
            return;
        }
        
        if (node.isLeaf()) {
            // Fill this block with the average color
            Color avgColor = node.getColor();
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(avgColor);
            g2d.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g2d.dispose();
        } else {
            // Recursively render children
            renderQuadtree(image, node.getTopLeft());
            renderQuadtree(image, node.getTopRight());
            renderQuadtree(image, node.getBottomLeft());
            renderQuadtree(image, node.getBottomRight());
        }
    }
    
    /**
     * Render a partially built quadtree to an image (for GIF frames)
     * This version safely handles incomplete trees
     */
    private void renderQuadtreePartial(BufferedImage image, Node node) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) {
            // Fill this block with the average color
            Color avgColor = node.getColor();
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(avgColor);
            g2d.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g2d.dispose();
        } else {
            // Check if children exist before recursing
            if (node.getTopLeft() != null) renderQuadtreePartial(image, node.getTopLeft());
            if (node.getTopRight() != null) renderQuadtreePartial(image, node.getTopRight());
            if (node.getBottomLeft() != null) renderQuadtreePartial(image, node.getBottomLeft());
            if (node.getBottomRight() != null) renderQuadtreePartial(image, node.getBottomRight());
        }
    }

    /**
     * Get the compression steps for GIF creation
     */
    public List<BufferedImage> getCompressionSteps() {
        return compressionSteps;
    }

    /**
     * Get the tree depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Get the node count
     */
    public int getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Get the quadtree root node
     */
    public Node getRoot() {
        return root;
    }
}