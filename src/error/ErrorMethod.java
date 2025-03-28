package src.error;

public enum ErrorMethod {
    VARIANCE(1, "Variance"),
    MAD(2, "Mean Absolute Deviation"),
    MAX_DIFF(3, "Max Pixel Difference"),
    ENTROPY(4, "Entropy"),
    SSIM(5, "Structural Similarity Index (Bonus)");
    
    private final int id;
    private final String name;
    
    ErrorMethod(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public static ErrorMethod getById(int id) {
        for (ErrorMethod method : values()) {
            if (method.getId() == id) {
                return method;
            }
        }
        return null;
    }
    
    public static String getAvailableMethods() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available error measurement methods:\n");
        for (ErrorMethod method : values()) {
            sb.append(method.getId()).append(". ").append(method.getName()).append("\n");
        }
        return sb.toString();
    }
}