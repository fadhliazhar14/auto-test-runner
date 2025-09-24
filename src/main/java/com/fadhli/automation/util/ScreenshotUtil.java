package com.fadhli.automation.util;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for capturing and managing screenshots
 * Provides methods for screenshot capture with automatic file naming and organization
 */
public class ScreenshotUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);
    
    private static final String SCREENSHOTS_DIR = "screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    
    /**
     * Capture screenshot with automatic filename
     */
    public static String captureScreenshot(WebDriver driver) {
        return captureScreenshot(driver, "screenshot_" + getCurrentTimestamp());
    }
    
    /**
     * Capture screenshot with custom filename
     */
    public static String captureScreenshot(WebDriver driver, String filename) {
        if (driver == null) {
            logger.warn("Cannot capture screenshot: WebDriver is null");
            return null;
        }
        
        if (!(driver instanceof TakesScreenshot)) {
            logger.warn("Cannot capture screenshot: WebDriver does not support screenshots");
            return null;
        }
        
        try {
            // Ensure screenshots directory exists
            ensureScreenshotsDirectory();
            
            // Capture screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            
            // Generate filename with timestamp if not provided
            String finalFilename = filename;
            if (!finalFilename.toLowerCase().endsWith(".png")) {
                finalFilename += ".png";
            }
            
            // Create file path
            Path screenshotPath = Paths.get(SCREENSHOTS_DIR, finalFilename);
            
            // Write screenshot to file
            Files.write(screenshotPath, screenshotBytes);
            
            String absolutePath = screenshotPath.toAbsolutePath().toString();
            logger.info("Screenshot captured: {}", absolutePath);
            
            return absolutePath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot: {}", filename, e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while capturing screenshot: {}", filename, e);
            return null;
        }
    }
    
    /**
     * Capture screenshot for failed test case
     */
    public static String captureFailureScreenshot(WebDriver driver, String testName) {
        String filename = String.format("failure_%s_%s", 
            sanitizeFilename(testName), 
            getCurrentTimestamp());
        
        return captureScreenshot(driver, filename);
    }
    
    /**
     * Capture screenshot for specific step
     */
    public static String captureStepScreenshot(WebDriver driver, String stepName, int stepNumber) {
        String filename = String.format("step_%03d_%s_%s", 
            stepNumber,
            sanitizeFilename(stepName), 
            getCurrentTimestamp());
        
        return captureScreenshot(driver, filename);
    }
    
    /**
     * Get current timestamp for filenames
     */
    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * Sanitize filename by removing/replacing invalid characters
     */
    private static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        
        // Replace invalid filename characters with underscores
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                      .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
    }
    
    /**
     * Ensure screenshots directory exists
     */
    private static void ensureScreenshotsDirectory() throws IOException {
        Path screenshotsPath = Paths.get(SCREENSHOTS_DIR);
        if (!Files.exists(screenshotsPath)) {
            Files.createDirectories(screenshotsPath);
            logger.debug("Created screenshots directory: {}", screenshotsPath.toAbsolutePath());
        }
    }
    
    /**
     * Get screenshots directory path
     */
    public static String getScreenshotsDirectory() {
        return Paths.get(SCREENSHOTS_DIR).toAbsolutePath().toString();
    }
    
    /**
     * Clean old screenshots (older than specified days)
     */
    public static int cleanOldScreenshots(int daysOld) {
        try {
            Path screenshotsPath = Paths.get(SCREENSHOTS_DIR);
            if (!Files.exists(screenshotsPath)) {
                return 0;
            }
            
            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
            int deletedCount = 0;
            
            Files.list(screenshotsPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".png"))
                .forEach(path -> {
                    try {
                        if (Files.getLastModifiedTime(path).toMillis() < cutoffTime) {
                            Files.delete(path);
                            logger.debug("Deleted old screenshot: {}", path.getFileName());
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to delete old screenshot: {}", path.getFileName(), e);
                    }
                });
            
            logger.info("Cleaned {} old screenshots (older than {} days)", deletedCount, daysOld);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("Failed to clean old screenshots", e);
            return 0;
        }
    }
}