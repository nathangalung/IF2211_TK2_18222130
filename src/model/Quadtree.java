package src.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import src.error.ErrorCalculator;
import src.error.ErrorMethod;

public class Quadtree {
    private Node root;
    private int minBlockSize;
    private double threshold;
    private ErrorMethod errorMethod;
    private int depth;
    private int nodeCount;
    private BufferedImage originalImage;
    private List<BufferedImage> compressionSteps;
    private int stepCounter = 0;
    private static final int MAX_FRAMES = 30;

    public Quadtree(BufferedImage image, int minBlockSize, double threshold, 
                   ErrorMethod errorMethod, boolean captureSteps) {
        this.minBlockSize = minBlockSize;
        this.threshold = threshold;
        this.errorMethod = errorMethod;
        this.depth = 0;
        this.nodeCount = 0;
        this.originalImage = image;
        this.compressionSteps = captureSteps ? new ArrayList<>() : null;
        
        // Add blank starting frame if making GIF
        if (captureSteps) {
            BufferedImage initialImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = initialImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.dispose();
            
            compressionSteps.add(initialImage);
        }
        
        // Build the tree from the image
        this.root = buildTree(image, 0, 0, image.getWidth(), image.getHeight(), 0);
        
        // Add final frame if making GIF
        if (captureSteps) {
            BufferedImage finalImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            renderQuadtree(finalImage, root);
            compressionSteps.add(finalImage);
        }
    }

    private Node buildTree(BufferedImage image, int x, int y, int width, int height, int currentDepth) {
        // Track stats
        this.nodeCount++;
        this.depth = Math.max(this.depth, currentDepth);
        
        // Get block info
        int[] avgColor = ErrorCalculator.calculateAvgColor(image, x, y, width, height);
        double error = ErrorCalculator.calculateError(image, x, y, width, height, errorMethod);
        
        // Create node for this block
        Node node = new Node(x, y, width, height, avgColor, error);
        
        // Split if needed
        if (error > threshold && width > minBlockSize && height > minBlockSize) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            
            // Create 4 children
            Node topLeft = buildTree(image, x, y, halfWidth, halfHeight, currentDepth + 1);
            Node topRight = buildTree(image, x + halfWidth, y, halfWidth, halfHeight, currentDepth + 1);
            Node bottomLeft = buildTree(image, x, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            Node bottomRight = buildTree(image, x + halfWidth, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            
            // Connect children to parent
            node.split(topLeft, topRight, bottomLeft, bottomRight);
            
            // Save frames for GIF occasionally (saves memory)
            if (compressionSteps != null && currentDepth <= 6 && stepCounter % 5 == 0 && 
                compressionSteps.size() < MAX_FRAMES) {
                
                BufferedImage stepImage = new BufferedImage(
                    originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                
                renderQuadtreePartial(stepImage, root);
                compressionSteps.add(stepImage);
            }
            stepCounter++;
        }
        
        return node;
    }

    public BufferedImage compressImage() {
        // Use a more memory-efficient approach for large images
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // For very large images (>2000px), use TYPE_3BYTE_BGR instead of TYPE_INT_RGB
        int imageType = (width * height > 4000000) ? 
                         BufferedImage.TYPE_3BYTE_BGR : 
                         BufferedImage.TYPE_INT_RGB;
        
        BufferedImage result = new BufferedImage(width, height, imageType);
        
        // Process image in tiles for very large images to reduce memory usage
        if (width * height > 8000000) {
            int tileSize = 512;
            for (int y = 0; y < height; y += tileSize) {
                for (int x = 0; x < width; x += tileSize) {
                    int tileWidth = Math.min(tileSize, width - x);
                    int tileHeight = Math.min(tileSize, height - y);
                    renderTile(result, root, x, y, tileWidth, tileHeight);
                    
                    // Force garbage collection occasionally
                    if (x % 1024 == 0 && y % 1024 == 0) {
                        System.gc();
                    }
                }
            }
        } else {
            // For smaller images, render the whole tree at once
            renderQuadtree(result, root);
        }
        
        return result;
    }

    private void renderTile(BufferedImage image, Node node, int tileX, int tileY, int tileWidth, int tileHeight) {
        if (node == null) return;
        
        // Check if this node intersects with our tile
        if (!intersects(node.getX(), node.getY(), node.getWidth(), node.getHeight(), 
                        tileX, tileY, tileWidth, tileHeight)) {
            return;
        }
        
        if (node.isLeaf()) {
            // Only draw the part of this leaf that's in our tile
            int x1 = Math.max(node.getX(), tileX);
            int y1 = Math.max(node.getY(), tileY);
            int x2 = Math.min(node.getX() + node.getWidth(), tileX + tileWidth);
            int y2 = Math.min(node.getY() + node.getHeight(), tileY + tileHeight);
            
            if (x2 > x1 && y2 > y1) {
                Graphics2D g = image.createGraphics();
                g.setColor(node.getColor());
                g.fillRect(x1, y1, x2 - x1, y2 - y1);
                g.dispose();
            }
        } else {
            // Recurse to children
            renderTile(image, node.getTopLeft(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getTopRight(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getBottomLeft(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getBottomRight(), tileX, tileY, tileWidth, tileHeight);
        }
    }
    
    private boolean intersects(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void renderQuadtree(BufferedImage image, Node node) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) {
            // Fill block with its average color
            Graphics2D g = image.createGraphics();
            g.setColor(node.getColor());
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g.dispose();
        } else {
            // Draw children
            renderQuadtree(image, node.getTopLeft());
            renderQuadtree(image, node.getTopRight());
            renderQuadtree(image, node.getBottomLeft());
            renderQuadtree(image, node.getBottomRight());
        }
    }
    
    private void renderQuadtreePartial(BufferedImage image, Node node) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) {
            // Fill block with its average color
            Graphics2D g = image.createGraphics();
            g.setColor(node.getColor());
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g.dispose();
        } else {
            // Check each child exists before drawing
            if (node.getTopLeft() != null) renderQuadtreePartial(image, node.getTopLeft());
            if (node.getTopRight() != null) renderQuadtreePartial(image, node.getTopRight());
            if (node.getBottomLeft() != null) renderQuadtreePartial(image, node.getBottomLeft());
            if (node.getBottomRight() != null) renderQuadtreePartial(image, node.getBottomRight());
        }
    }

    // Simple getters
    public List<BufferedImage> getCompressionSteps() { return compressionSteps; }
    public int getDepth() { return depth; }
    public int getNodeCount() { return nodeCount; }
    public Node getRoot() { return root; }
}