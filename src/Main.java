import java.io.File;
import java.util.Scanner;
import src.compression.CompressionStats;
import src.compression.ImageCompressor;
import src.error.ErrorMethod;
import src.util.ImageUtil;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== Quadtree Image Compression ===");
        System.out.println("IF2211 - Strategi Algoritma");
        System.out.println("----------------------------------------");
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Get all required inputs
            String inputPath = getInputPath(scanner);
            ErrorMethod errorMethod = getErrorMethod(scanner);
            double threshold = getThreshold(scanner, errorMethod);
            int minBlockSize = getMinBlockSize(scanner);
            double targetRatio = getTargetCompressionRatio(scanner);
            String outputPath = getOutputPath(scanner);
            String gifPath = getGifPath(scanner);
            
            // Run compression
            System.out.println("\nStarting image compression...");
            ImageCompressor compressor = new ImageCompressor(
                inputPath, outputPath, gifPath, errorMethod, threshold, minBlockSize, targetRatio
            );
            
            CompressionStats stats = compressor.compress();
            
            // Show results
            System.out.println("\n" + stats.getSummary());
            System.out.println("Compressed image saved to: " + outputPath);
            
            if (gifPath != null && !gifPath.isEmpty()) {
                System.out.println("Compression process GIF saved to: " + gifPath);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static String getInputPath(Scanner scanner) {
        String path = "";
        boolean valid = false;
        
        while (!valid) {
            System.out.print("Enter the absolute path of the image to compress: ");
            path = scanner.nextLine().trim();
            
            if (path.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Error: File does not exist.");
                continue;
            }
            
            if (!ImageUtil.isValidImageFile(path)) {
                System.out.println("Error: Not a valid image file.");
                continue;
            }
            
            valid = true;
        }
        
        return path;
    }
    
    private static ErrorMethod getErrorMethod(Scanner scanner) {
        ErrorMethod method = null;
        
        while (method == null) {
            System.out.println("\n" + ErrorMethod.getAvailableMethods());
            System.out.print("Select error measurement method (1-" + ErrorMethod.values().length + "): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                method = ErrorMethod.getById(choice);
                
                if (method == null) {
                    System.out.println("Error: Invalid choice. Please select a number from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return method;
    }
    
    private static double getThreshold(Scanner scanner, ErrorMethod errorMethod) {
        double threshold = 0;
        boolean valid = false;
        
        // Give range hints based on method
        String hint;
        switch (errorMethod) {
            case VARIANCE:   hint = "Suggested range: 10-1000"; break;
            case MAD:        hint = "Suggested range: 5-100"; break;
            case MAX_DIFF:   hint = "Suggested range: 10-200"; break;
            case ENTROPY:    hint = "Suggested range: 0.1-5.0"; break;
            case SSIM:       hint = "Suggested range: 0.01-0.5"; break;
            default:         hint = "Enter a positive number";
        }
        
        while (!valid) {
            System.out.println("\nEnter the threshold value (" + hint + "):");
            System.out.print("Threshold: ");
            
            try {
                threshold = Double.parseDouble(scanner.nextLine().trim());
                
                if (threshold <= 0) {
                    System.out.println("Error: Threshold must be positive.");
                    continue;
                }
                
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return threshold;
    }
    
    private static int getMinBlockSize(Scanner scanner) {
        int size = 0;
        boolean valid = false;
        
        while (!valid) {
            System.out.print("\nEnter the minimum block size (2, 4, 8, 16, etc.): ");
            
            try {
                size = Integer.parseInt(scanner.nextLine().trim());
                
                if (size < 1) {
                    System.out.println("Error: Minimum block size must be at least 1.");
                    continue;
                }
                
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return size;
    }
    
    private static double getTargetCompressionRatio(Scanner scanner) {
        double ratio = 0;
        boolean valid = false;
        
        while (!valid) {
            System.out.println("\nEnter target compression ratio (0-1.0, where 1.0 = 100% compression):");
            System.out.println("Enter 0 to disable automatic threshold adjustment.");
            System.out.print("Target ratio: ");
            
            try {
                ratio = Double.parseDouble(scanner.nextLine().trim());
                
                if (ratio < 0 || ratio > 1) {
                    System.out.println("Error: Ratio must be between 0 and 1.0.");
                    continue;
                }
                
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return ratio;
    }
    
    private static String getOutputPath(Scanner scanner) {
        String path = "";
        boolean valid = false;
        
        while (!valid) {
            System.out.print("\nEnter the absolute path for the compressed image output: ");
            path = scanner.nextLine().trim();
            
            if (path.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(path);
            File parentDir = file.getParentFile();
            
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Warning: Directory does not exist. Create? (y/n)");
                String response = scanner.nextLine().trim().toLowerCase();
                
                if (response.equals("y") || response.equals("yes")) {
                    if (!parentDir.mkdirs()) {
                        System.out.println("Error: Could not create directory.");
                        continue;
                    }
                } else {
                    System.out.println("Please enter a different path.");
                    continue;
                }
            }
            
            // Check file extension
            String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
            if (!ext.equals("jpg") && !ext.equals("jpeg") && 
                !ext.equals("png") && !ext.equals("bmp")) {
                System.out.println("Warning: Recommended file extensions are jpg, jpeg, png, or bmp.");
                System.out.println("Continue with ." + ext + "? (y/n)");
                
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("y") && !response.equals("yes")) {
                    continue;
                }
            }
            
            valid = true;
        }
        
        return path;
    }
    
    private static String getGifPath(Scanner scanner) {
        System.out.println("\nWould you like to generate a GIF of the compression process? (y/n)");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (!response.equals("y") && !response.equals("yes")) {
            return null;
        }
        
        String path = "";
        boolean valid = false;
        
        while (!valid) {
            System.out.print("Enter the absolute path for the GIF output: ");
            path = scanner.nextLine().trim();
            
            if (path.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(path);
            File parentDir = file.getParentFile();
            
            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Warning: Directory does not exist. Create? (y/n)");
                response = scanner.nextLine().trim().toLowerCase();
                
                if (response.equals("y") || response.equals("yes")) {
                    if (!parentDir.mkdirs()) {
                        System.out.println("Error: Could not create directory.");
                        continue;
                    }
                } else {
                    System.out.println("Please enter a different path.");
                    continue;
                }
            }
            
            // Add .gif extension if missing
            if (!path.toLowerCase().endsWith(".gif")) {
                path += ".gif";
            }
            
            valid = true;
        }
        
        return path;
    }
}