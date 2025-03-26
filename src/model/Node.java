package src.model;

import java.awt.Color;

/**
 * Represents a node in the Quadtree structure
 */
public class Node {
    // Position and dimensions
    private int x, y, width, height;
    
    // Average color for this block
    private int[] avgColor;
    
    // Error value for this block
    private double error;
    
    // Children nodes (null for leaf nodes)
    private Node topLeft;
    private Node topRight;
    private Node bottomLeft;
    private Node bottomRight;
    
    // Flag to indicate if this is a leaf node
    private boolean isLeaf;
    
    /**
     * Constructor for a Node
     * 
     * @param x X-coordinate of the node
     * @param y Y-coordinate of the node
     * @param width Width of the block
     * @param height Height of the block
     * @param avgColor Average RGB color of the block
     * @param error Error value for the block
     */
    public Node(int x, int y, int width, int height, int[] avgColor, double error) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.avgColor = avgColor;
        this.error = error;
        this.isLeaf = true; // Default to leaf node until split
    }
    
    // Getters and setters
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int[] getAvgColor() {
        return avgColor;
    }
    
    public Color getColor() {
        return new Color(avgColor[0], avgColor[1], avgColor[2]);
    }
    
    public double getError() {
        return error;
    }
    
    public boolean isLeaf() {
        return isLeaf;
    }
    
    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
    
    public Node getTopLeft() {
        return topLeft;
    }
    
    public void setTopLeft(Node topLeft) {
        this.topLeft = topLeft;
    }
    
    public Node getTopRight() {
        return topRight;
    }
    
    public void setTopRight(Node topRight) {
        this.topRight = topRight;
    }
    
    public Node getBottomLeft() {
        return bottomLeft;
    }
    
    public void setBottomLeft(Node bottomLeft) {
        this.bottomLeft = bottomLeft;
    }
    
    public Node getBottomRight() {
        return bottomRight;
    }
    
    public void setBottomRight(Node bottomRight) {
        this.bottomRight = bottomRight;
    }
    
    /**
     * Splits this node into four children
     * 
     * @param topLeft Top-left child node
     * @param topRight Top-right child node
     * @param bottomLeft Bottom-left child node
     * @param bottomRight Bottom-right child node
     */
    public void split(Node topLeft, Node topRight, Node bottomLeft, Node bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.isLeaf = false;
    }
}