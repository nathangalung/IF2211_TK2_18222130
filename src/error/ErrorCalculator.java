package src.error;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ErrorCalculator {
    
    public static double calculateError(BufferedImage image, int x, int y, int width, int height, ErrorMethod method) {
        // Most methods need average color
        int[] avgColor = calculateAvgColor(image, x, y, width, height);
        
        switch (method) {
            case VARIANCE:
                return calculateVariance(image, x, y, width, height, avgColor);
            case MAD:
                return calculateMAD(image, x, y, width, height, avgColor);
            case MAX_DIFF:
                return calculateMaxDiff(image, x, y, width, height);
            case ENTROPY:
                return calculateEntropy(image, x, y, width, height);
            case SSIM:
                return calculateSimplifiedSSIM(image, x, y, width, height, avgColor);
            default:
                return calculateVariance(image, x, y, width, height, avgColor);
        }
    }
    
    public static int[] calculateAvgColor(BufferedImage image, int x, int y, int width, int height) {
        long rSum = 0, gSum = 0, bSum = 0;
        int count = 0;
        
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                rSum += pixel.getRed();
                gSum += pixel.getGreen();
                bSum += pixel.getBlue();
                count++;
            }
        }
        
        return new int[]{
            (int)(rSum / count),
            (int)(gSum / count),
            (int)(bSum / count)
        };
    }
    
    private static double calculateVariance(BufferedImage image, int x, int y, int width, int height, int[] avgColor) {
        double rVariance = 0, gVariance = 0, bVariance = 0;
        int count = width * height;
        
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                rVariance += Math.pow(pixel.getRed() - avgColor[0], 2);
                gVariance += Math.pow(pixel.getGreen() - avgColor[1], 2);
                bVariance += Math.pow(pixel.getBlue() - avgColor[2], 2);
            }
        }
        
        rVariance /= count;
        gVariance /= count;
        bVariance /= count;
        
        return (rVariance + gVariance + bVariance) / 3;
    }
    
    private static double calculateMAD(BufferedImage image, int x, int y, int width, int height, int[] avgColor) {
        double rMAD = 0, gMAD = 0, bMAD = 0;
        int count = width * height;
        
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                rMAD += Math.abs(pixel.getRed() - avgColor[0]);
                gMAD += Math.abs(pixel.getGreen() - avgColor[1]);
                bMAD += Math.abs(pixel.getBlue() - avgColor[2]);
            }
        }
        
        rMAD /= count;
        gMAD /= count;
        bMAD /= count;
        
        return (rMAD + gMAD + bMAD) / 3;
    }
    
    private static double calculateMaxDiff(BufferedImage image, int x, int y, int width, int height) {
        int rMin = 255, gMin = 255, bMin = 255;
        int rMax = 0, gMax = 0, bMax = 0;
        
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                
                rMin = Math.min(rMin, pixel.getRed());
                gMin = Math.min(gMin, pixel.getGreen());
                bMin = Math.min(bMin, pixel.getBlue());
                
                rMax = Math.max(rMax, pixel.getRed());
                gMax = Math.max(gMax, pixel.getGreen());
                bMax = Math.max(bMax, pixel.getBlue());
            }
        }
        
        double rDiff = rMax - rMin;
        double gDiff = gMax - gMin;
        double bDiff = bMax - bMin;
        
        return (rDiff + gDiff + bDiff) / 3;
    }
    
    private static double calculateEntropy(BufferedImage image, int x, int y, int width, int height) {
        int[] rHistogram = new int[256];
        int[] gHistogram = new int[256];
        int[] bHistogram = new int[256];
        
        int totalPixels = width * height;
        
        // Reset histograms
        Arrays.fill(rHistogram, 0);
        Arrays.fill(gHistogram, 0);
        Arrays.fill(bHistogram, 0);
        
        // Count color frequencies
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                rHistogram[pixel.getRed()]++;
                gHistogram[pixel.getGreen()]++;
                bHistogram[pixel.getBlue()]++;
            }
        }
        
        // Calculate entropy for each channel
        double rEntropy = 0, gEntropy = 0, bEntropy = 0;
        
        for (int i = 0; i < 256; i++) {
            if (rHistogram[i] > 0) {
                double probability = (double) rHistogram[i] / totalPixels;
                rEntropy -= probability * (Math.log(probability) / Math.log(2));
            }
            
            if (gHistogram[i] > 0) {
                double probability = (double) gHistogram[i] / totalPixels;
                gEntropy -= probability * (Math.log(probability) / Math.log(2));
            }
            
            if (bHistogram[i] > 0) {
                double probability = (double) bHistogram[i] / totalPixels;
                bEntropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }
        
        return (rEntropy + gEntropy + bEntropy) / 3;
    }
    
    private static double calculateSimplifiedSSIM(BufferedImage image, int x, int y, int width, int height, int[] avgColor) {
        // Standard SSIM constants
        final double C1 = Math.pow(0.01 * 255, 2);
        final double C2 = Math.pow(0.03 * 255, 2);
        
        // Create reference image with solid color
        BufferedImage avgImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int avgRGB = new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB();
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                avgImage.setRGB(i, j, avgRGB);
            }
        }
        
        // Set up component arrays
        double[] meanX = new double[3]; // Block mean
        double[] meanY = new double[3]; // Reference mean
        double[] varX = new double[3];  // Block variance
        double[] covarXY = new double[3]; // Covariance
        
        // Means are just the average color
        meanX[0] = meanY[0] = avgColor[0];
        meanX[1] = meanY[1] = avgColor[1];
        meanX[2] = meanY[2] = avgColor[2];
        
        // Calculate variance and covariance
        for (int j = y; j < y + height; j++) {
            for (int i = x; i < x + width; i++) {
                Color pixel = new Color(image.getRGB(i, j));
                
                varX[0] += Math.pow(pixel.getRed() - meanX[0], 2);
                varX[1] += Math.pow(pixel.getGreen() - meanX[1], 2);
                varX[2] += Math.pow(pixel.getBlue() - meanX[2], 2);
                
                // Reference image is solid color, so covariance is simplified
                covarXY[0] += (pixel.getRed() - meanX[0]) * (avgColor[0] - meanY[0]);
                covarXY[1] += (pixel.getGreen() - meanX[1]) * (avgColor[1] - meanY[1]);
                covarXY[2] += (pixel.getBlue() - meanX[2]) * (avgColor[2] - meanY[2]);
            }
        }
        
        int n = width * height;
        for (int i = 0; i < 3; i++) {
            varX[i] /= n;
            covarXY[i] /= n;
        }
        
        // Reference image has 0 variance (solid color)
        double[] varY = {0, 0, 0};
        
        // Calculate SSIM for each channel
        double[] ssim = new double[3];
        for (int i = 0; i < 3; i++) {
            ssim[i] = ((2 * meanX[i] * meanY[i] + C1) * (2 * covarXY[i] + C2)) / 
                      ((meanX[i] * meanX[i] + meanY[i] * meanY[i] + C1) * (varX[i] + varY[i] + C2));
        }
        
        // Average the channels
        double ssimRGB = (ssim[0] + ssim[1] + ssim[2]) / 3;
        
        // Invert for error measurement (1 = max error, 0 = no error)
        return 1 - ssimRGB;
    }
}