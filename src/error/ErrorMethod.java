package src.error;

// Error method options
public enum ErrorMethod {
    VARIANCE(1, "Variance"),
    MAD(2, "Mean Absolute Deviation"),
    MAX_DIFF(3, "Max Pixel Difference"),
    ENTROPY(4, "Entropy"),
    SSIM(5, "Structural Similarity Index (Bonus)");
    
    private final int id;
    private final String name;
    
    // Constructor
    ErrorMethod(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Get method id
    public int getId() {
        return id;
    }
    
    // Get method name
    public String getName() {
        return name;
    }
    
    // Find by id
    public static ErrorMethod getById(int id) {
        for (ErrorMethod method : values()) {
            if (method.getId() == id) {
                return method;
            }
        }
        return null;
    }
    
    // List all methods
    public static String getAvailableMethods() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available error measurement methods:\n");
        for (ErrorMethod method : values()) {
            sb.append(method.getId()).append(". ").append(method.getName()).append("\n");
        }
        return sb.toString();
    }
}