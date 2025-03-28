package src.model;

import java.awt.Color;

public class Node {
    // Region info
    private int x, y, width, height;
    private int[] avgColor;
    private double error;
    
    // Children
    private Node topLeft;
    private Node topRight;
    private Node bottomLeft;
    private Node bottomRight;
    
    // Leaf status
    private boolean isLeaf;
    
    public Node(int x, int y, int width, int height, int[] avgColor, double error) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.avgColor = avgColor;
        this.error = error;
        this.isLeaf = true; // Start as leaf until split
    }
    
    // Simple getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getAvgColor() { return avgColor; }
    public double getError() { return error; }
    public boolean isLeaf() { return isLeaf; }
    
    public Color getColor() {
        return new Color(avgColor[0], avgColor[1], avgColor[2]);
    }
    
    // Child node getters
    public Node getTopLeft() { return topLeft; }
    public Node getTopRight() { return topRight; }
    public Node getBottomLeft() { return bottomLeft; }
    public Node getBottomRight() { return bottomRight; }
    
    // These setters are rarely used except in split()
    public void setLeaf(boolean isLeaf) { this.isLeaf = isLeaf; }
    public void setTopLeft(Node node) { this.topLeft = node; }
    public void setTopRight(Node node) { this.topRight = node; }
    public void setBottomLeft(Node node) { this.bottomLeft = node; }
    public void setBottomRight(Node node) { this.bottomRight = node; }
    
    public void split(Node topLeft, Node topRight, Node bottomLeft, Node bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.isLeaf = false;
    }
}