package src.compression;

/**
 * Stores and calculates statistics about image compression
 */
public class CompressionStats {
    private long originalFileSize;
    private long compressedFileSize;
    private int treeDepth;
    private int nodeCount;
    private long executionTimeMs;
    
    /**
     * Constructor
     * 
     * @param originalFileSize Size of the original file in bytes
     * @param compressedFileSize Size of the compressed file in bytes
     * @param treeDepth Depth of the quadtree
     * @param nodeCount Number of nodes in the quadtree
     * @param executionTimeMs Execution time in milliseconds
     */
    public CompressionStats(long originalFileSize, long compressedFileSize, int treeDepth, int nodeCount, long executionTimeMs) {
        this.originalFileSize = originalFileSize;
        this.compressedFileSize = compressedFileSize;
        this.treeDepth = treeDepth;
        this.nodeCount = nodeCount;
        this.executionTimeMs = executionTimeMs;
    }
    
    /**
     * Calculate compression percentage
     * 
     * @return Compression percentage (0-1)
     */
    public double getCompressionPercentage() {
        if (originalFileSize == 0) {
            return 0;
        }
        return 1.0 - ((double) compressedFileSize / originalFileSize);
    }
    
    /**
     * Get original file size in bytes
     */
    public long getOriginalFileSize() {
        return originalFileSize;
    }
    
    /**
     * Get compressed file size in bytes
     */
    public long getCompressedFileSize() {
        return compressedFileSize;
    }
    
    /**
     * Get tree depth
     */
    public int getTreeDepth() {
        return treeDepth;
    }
    
    /**
     * Get node count
     */
    public int getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Get execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Format execution time as a readable string
     */
    public String getFormattedExecutionTime() {
        if (executionTimeMs < 1000) {
            return executionTimeMs + " ms";
        } else {
            double seconds = executionTimeMs / 1000.0;
            return String.format("%.2f seconds", seconds);
        }
    }
    
    /**
     * Format file size as a readable string
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }
    
    /**
     * Create a comprehensive summary of the compression
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Compression Statistics:\n");
        sb.append("--------------------\n");
        sb.append("Execution time: ").append(getFormattedExecutionTime()).append("\n");
        sb.append("Original image size: ").append(formatFileSize(originalFileSize)).append("\n");
        sb.append("Compressed image size: ").append(formatFileSize(compressedFileSize)).append("\n");
        sb.append("Compression percentage: ").append(String.format("%.2f%%", getCompressionPercentage() * 100)).append("\n");
        sb.append("Quadtree depth: ").append(treeDepth).append("\n");
        sb.append("Number of nodes: ").append(nodeCount).append("\n");
        
        return sb.toString();
    }
}