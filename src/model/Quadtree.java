package src.model;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
    private static final int MAX_FRAMES = 25;

    public Quadtree(BufferedImage image, int minBlockSize, double threshold, 
                   ErrorMethod errorMethod, boolean captureSteps) {
        this.minBlockSize = minBlockSize;
        this.threshold = threshold;
        this.errorMethod = errorMethod;
        this.depth = 0;
        this.nodeCount = 0;
        this.originalImage = image;
        this.compressionSteps = captureSteps ? new ArrayList<>() : null;
        
        // Add first frame
        if (captureSteps) {
            BufferedImage initialImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = initialImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            
            compressionSteps.add(initialImage);
        }
        
        // Build tree
        this.root = buildTree(image, 0, 0, image.getWidth(), image.getHeight(), 0);
        
        // Add last frame
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
        
        // Create node
        Node node = new Node(x, y, width, height, avgColor, error);
        
        // Split if needed
        if (error > threshold && width > minBlockSize && height > minBlockSize) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            
            // Create children
            Node topLeft = buildTree(image, x, y, halfWidth, halfHeight, currentDepth + 1);
            Node topRight = buildTree(image, x + halfWidth, y, halfWidth, halfHeight, currentDepth + 1);
            Node bottomLeft = buildTree(image, x, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            Node bottomRight = buildTree(image, x + halfWidth, y + halfHeight, halfWidth, halfHeight, currentDepth + 1);
            
            // Connect children
            node.split(topLeft, topRight, bottomLeft, bottomRight);
            
            // Capture frames
            int captureFrequency = calculateCaptureFrequency(currentDepth);
            if (compressionSteps != null && stepCounter % captureFrequency == 0 && compressionSteps.size() < MAX_FRAMES) {
                captureProgressFrame();
            }
            stepCounter++;
        }
        
        return node;
    }
    
    // Frame capture frequency
    private int calculateCaptureFrequency(int depth) {
        // Early splits
        if (depth <= 2) return 1;
        
        // Based on size
        int totalPixels = originalImage.getWidth() * originalImage.getHeight();
        if (totalPixels < 250000) {
            return 2; 
        } else if (totalPixels < 1000000) {
            return 4;
        } else {
            return 8;
        }
    }
    
    // Capture compression progress
    private void captureProgressFrame() {
        try {
            // Create new image
            BufferedImage stepImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
            
            // Draw background
            Graphics2D g = stepImage.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.drawImage(originalImage, 0, 0, null);
            
            // Draw quadtree
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            renderQuadtreeWithStrongBorders(stepImage, root, g);
            g.dispose();
            
            // Save frame
            compressionSteps.add(stepImage);
            
            // Free memory
            if (originalImage.getWidth() * originalImage.getHeight() > 1000000) {
                System.gc();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Warning: Memory limit reached, stopping frame capture");
            compressionSteps = null;
        }
    }

    // Draw with thick borders
    private void renderQuadtreeWithStrongBorders(BufferedImage image, Node node, Graphics2D g) {
        if (node == null) return;
        
        if (node.isLeaf()) {
            // Fill block
            g.setColor(node.getColor());
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            
            // Draw border
            g.setColor(new Color(0, 0, 0, 200));
            g.setStroke(new BasicStroke(Math.max(1, node.getWidth()/100)));
            g.drawRect(node.getX(), node.getY(), node.getWidth()-1, node.getHeight()-1);
        } else {
            // Border for parent
            if (node.getWidth() > minBlockSize * 4) {
                g.setColor(new Color(255, 0, 0, 100));
                g.setStroke(new BasicStroke(Math.max(2, node.getWidth()/80)));
                g.drawRect(node.getX(), node.getY(), node.getWidth()-1, node.getHeight()-1);
            }
            
            // Draw children
            if (node.getTopLeft() != null) renderQuadtreeWithStrongBorders(image, node.getTopLeft(), g);
            if (node.getTopRight() != null) renderQuadtreeWithStrongBorders(image, node.getTopRight(), g);
            if (node.getBottomLeft() != null) renderQuadtreeWithStrongBorders(image, node.getBottomLeft(), g);
            if (node.getBottomRight() != null) renderQuadtreeWithStrongBorders(image, node.getBottomRight(), g);
        }
    }
    
    // Draw with thin borders
    private void renderQuadtreeWithBorders(BufferedImage image, Node node, Graphics2D g) {
        if (node == null) return;
        
        if (node.isLeaf()) {
            // Fill block
            g.setColor(node.getColor());
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            
            // Draw border
            g.setColor(new Color(0, 0, 0, 128));
            g.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        } else {
            // Draw children
            if (node.getTopLeft() != null) renderQuadtreeWithBorders(image, node.getTopLeft(), g);
            if (node.getTopRight() != null) renderQuadtreeWithBorders(image, node.getTopRight(), g);
            if (node.getBottomLeft() != null) renderQuadtreeWithBorders(image, node.getBottomLeft(), g);
            if (node.getBottomRight() != null) renderQuadtreeWithBorders(image, node.getBottomRight(), g);
        }
    }

    // Create compressed image
    public BufferedImage compressImage() {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Choose format
        int imageType = (width * height > 4000000) ? 
                         BufferedImage.TYPE_3BYTE_BGR : 
                         BufferedImage.TYPE_INT_RGB;
        
        BufferedImage result = new BufferedImage(width, height, imageType);
        
        // Process large images in tiles
        if (width * height > 8000000) {
            int tileSize = 512;
            for (int y = 0; y < height; y += tileSize) {
                for (int x = 0; x < width; x += tileSize) {
                    int tileWidth = Math.min(tileSize, width - x);
                    int tileHeight = Math.min(tileSize, height - y);
                    renderTile(result, root, x, y, tileWidth, tileHeight);
                    
                    // Free memory
                    if (x % 1024 == 0 && y % 1024 == 0) {
                        System.gc();
                    }
                }
            }
        } else {
            // Render at once
            renderQuadtree(result, root);
        }
        
        return result;
    }

    // Render part of image
    private void renderTile(BufferedImage image, Node node, int tileX, int tileY, int tileWidth, int tileHeight) {
        if (node == null) return;
        
        // Skip non-intersecting
        if (!intersects(node.getX(), node.getY(), node.getWidth(), node.getHeight(), 
                        tileX, tileY, tileWidth, tileHeight)) {
            return;
        }
        
        if (node.isLeaf()) {
            // Draw leaf intersection
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
            // Check children
            renderTile(image, node.getTopLeft(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getTopRight(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getBottomLeft(), tileX, tileY, tileWidth, tileHeight);
            renderTile(image, node.getBottomRight(), tileX, tileY, tileWidth, tileHeight);
        }
    }
    
    // Check rectangle overlap
    private boolean intersects(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    // Render full quadtree
    private void renderQuadtree(BufferedImage image, Node node) {
        if (node == null) return;
        
        if (node.isLeaf()) {
            // Fill block
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
    
    // Render with null check
    private void renderQuadtreePartial(BufferedImage image, Node node) {
        if (node == null) return;
        
        if (node.isLeaf()) {
            // Fill block
            Graphics2D g = image.createGraphics();
            g.setColor(node.getColor());
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g.dispose();
        } else {
            // Check then draw
            if (node.getTopLeft() != null) renderQuadtreePartial(image, node.getTopLeft());
            if (node.getTopRight() != null) renderQuadtreePartial(image, node.getTopRight());
            if (node.getBottomLeft() != null) renderQuadtreePartial(image, node.getBottomLeft());
            if (node.getBottomRight() != null) renderQuadtreePartial(image, node.getBottomRight());
        }
    }

    // Get data methods
    public List<BufferedImage> getCompressionSteps() { return compressionSteps; }
    public int getDepth() { return depth; }
    public int getNodeCount() { return nodeCount; }
    public Node getRoot() { return root; }
}