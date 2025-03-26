import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import src.compression.CompressionStats;
import src.compression.ImageCompressor;
import src.error.ErrorMethod;
import src.util.ImageUtil;

/**
 * Main entry point for the Quadtree Image Compression application
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== Quadtree Image Compression ===");
        System.out.println("IF2211 - Strategi Algoritma");
        System.out.println("----------------------------------------");
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Get input image path
            String inputPath = getInputPath(scanner);
            
            // Get error measurement method
            ErrorMethod errorMethod = getErrorMethod(scanner);
            
            // Get threshold
            double threshold = getThreshold(scanner, errorMethod);
            
            // Get minimum block size
            int minBlockSize = getMinBlockSize(scanner);
            
            // Get target compression ratio (bonus)
            double targetCompressionRatio = getTargetCompressionRatio(scanner);
            
            // Get output image path
            String outputPath = getOutputPath(scanner);
            
            // Get GIF path (bonus)
            String gifPath = getGifPath(scanner);
            
            // Perform compression
            System.out.println("\nStarting image compression...");
            ImageCompressor compressor = new ImageCompressor(
                inputPath, outputPath, gifPath, errorMethod, threshold, minBlockSize, targetCompressionRatio
            );
            
            CompressionStats stats = compressor.compress();
            
            // Display results
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
    
    /**
     * Get input image path from user
     */
    private static String getInputPath(Scanner scanner) {
        String inputPath = "";
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("Enter the absolute path of the image to compress: ");
            inputPath = scanner.nextLine().trim();
            
            if (inputPath.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(inputPath);
            if (!file.exists()) {
                System.out.println("Error: File does not exist.");
                continue;
            }
            
            if (!ImageUtil.isValidImageFile(inputPath)) {
                System.out.println("Error: Not a valid image file.");
                continue;
            }
            
            validInput = true;
        }
        
        return inputPath;
    }
    
    /**
     * Get error measurement method from user
     */
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
    
    /**
     * Get threshold value from user
     */
    private static double getThreshold(Scanner scanner, ErrorMethod errorMethod) {
        double threshold = 0;
        boolean validInput = false;
        
        // Suggest appropriate range based on error method
        String rangeHint;
        switch (errorMethod) {
            case VARIANCE:
                rangeHint = "Suggested range: 10-1000";
                break;
            case MAD:
                rangeHint = "Suggested range: 5-100";
                break;
            case MAX_DIFF:
                rangeHint = "Suggested range: 10-200";
                break;
            case ENTROPY:
                rangeHint = "Suggested range: 0.1-5.0";
                break;
            case SSIM:
                rangeHint = "Suggested range: 0.01-0.5";
                break;
            default:
                rangeHint = "Enter a positive number";
        }
        
        while (!validInput) {
            System.out.println("\nEnter the threshold value (" + rangeHint + "):");
            System.out.print("Threshold: ");
            
            try {
                threshold = Double.parseDouble(scanner.nextLine().trim());
                
                if (threshold <= 0) {
                    System.out.println("Error: Threshold must be positive.");
                    continue;
                }
                
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return threshold;
    }
    
    /**
     * Get minimum block size from user
     */
    private static int getMinBlockSize(Scanner scanner) {
        int minBlockSize = 0;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("\nEnter the minimum block size (2, 4, 8, 16, etc.): ");
            
            try {
                minBlockSize = Integer.parseInt(scanner.nextLine().trim());
                
                if (minBlockSize < 1) {
                    System.out.println("Error: Minimum block size must be at least 1.");
                    continue;
                }
                
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return minBlockSize;
    }
    
    /**
     * Get target compression ratio from user (bonus feature)
     */
    private static double getTargetCompressionRatio(Scanner scanner) {
        double ratio = 0;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.println("\nEnter target compression ratio (0-1.0, where 1.0 = 100% compression):");
            System.out.println("Enter 0 to disable automatic threshold adjustment.");
            System.out.print("Target ratio: ");
            
            try {
                ratio = Double.parseDouble(scanner.nextLine().trim());
                
                if (ratio < 0 || ratio > 1) {
                    System.out.println("Error: Ratio must be between 0 and 1.0.");
                    continue;
                }
                
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
        
        return ratio;
    }
    
    /**
     * Get output image path from user
     */
    private static String getOutputPath(Scanner scanner) {
        String outputPath = "";
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("\nEnter the absolute path for the compressed image output: ");
            outputPath = scanner.nextLine().trim();
            
            if (outputPath.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(outputPath);
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
            String extension = outputPath.substring(outputPath.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.equals("jpg") && !extension.equals("jpeg") && 
                !extension.equals("png") && !extension.equals("bmp")) {
                System.out.println("Warning: Recommended file extensions are jpg, jpeg, png, or bmp.");
                System.out.println("Continue with ." + extension + "? (y/n)");
                
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("y") && !response.equals("yes")) {
                    continue;
                }
            }
            
            validInput = true;
        }
        
        return outputPath;
    }
    
    /**
     * Get GIF output path from user (bonus feature)
     */
    private static String getGifPath(Scanner scanner) {
        System.out.println("\nWould you like to generate a GIF of the compression process? (y/n)");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (!response.equals("y") && !response.equals("yes")) {
            return null;
        }
        
        String gifPath = "";
        boolean validInput = false;
        
        while (!validInput) {
            System.out.print("Enter the absolute path for the GIF output: ");
            gifPath = scanner.nextLine().trim();
            
            if (gifPath.isEmpty()) {
                System.out.println("Error: Path cannot be empty.");
                continue;
            }
            
            File file = new File(gifPath);
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
            
            // Ensure file has .gif extension
            if (!gifPath.toLowerCase().endsWith(".gif")) {
                gifPath += ".gif";
            }
            
            validInput = true;
        }
        
        return gifPath;
    }
}