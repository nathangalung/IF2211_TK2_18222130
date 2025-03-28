package src.compression;

public class CompressionStats {
    private long originalFileSize;
    private long compressedFileSize;
    private int treeDepth;
    private int nodeCount;
    private long executionTimeMs;
    
    public CompressionStats(long originalFileSize, long compressedFileSize, int treeDepth, int nodeCount, long executionTimeMs) {
        this.originalFileSize = originalFileSize;
        this.compressedFileSize = compressedFileSize;
        this.treeDepth = treeDepth;
        this.nodeCount = nodeCount;
        this.executionTimeMs = executionTimeMs;
    }
    
    public double getCompressionPercentage() {
        if (originalFileSize == 0) {
            return 0;
        }
        return 1.0 - ((double) compressedFileSize / originalFileSize);
    }
    
    // Simple getters for basic stats
    public int getTreeDepth() { return treeDepth; }
    public int getNodeCount() { return nodeCount; }
    
    private String formatTime() {
        if (executionTimeMs < 1000) {
            return executionTimeMs + " ms";
        } else {
            double seconds = executionTimeMs / 1000.0;
            return String.format("%.2f seconds", seconds);
        }
    }
    
    private static String formatSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }
    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Compression Statistics:\n");
        sb.append("--------------------\n");
        sb.append("Execution time: ").append(formatTime()).append("\n");
        sb.append("Original image size: ").append(formatSize(originalFileSize)).append("\n");
        sb.append("Compressed image size: ").append(formatSize(compressedFileSize)).append("\n");
        sb.append("Compression percentage: ").append(String.format("%.2f%%", getCompressionPercentage() * 100)).append("\n");
        sb.append("Quadtree depth: ").append(treeDepth).append("\n");
        sb.append("Number of nodes: ").append(nodeCount).append("\n");
        
        return sb.toString();
    }
}