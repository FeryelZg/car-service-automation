package com.carservice.automation.utils;

import com.carservice.automation.base.DriverManager;
import io.qameta.allure.Attachment;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtils {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "reports/screenshots/";

    /**
     * Take screenshot with current timestamp
     * @param testName Name of the test
     * @return Screenshot file path
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public static String takeScreenshot(String testName) {
        return takeScreenshot(testName, generateTimestamp());
    }

    /**
     * Take screenshot with custom suffix
     * @param testName Name of the test
     * @param suffix Custom suffix for filename
     * @return Screenshot file path
     */
    public static String takeScreenshot(String testName, String suffix) {
        try {
            // Ensure screenshot directory exists
            createScreenshotDirectory();

            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                logger.warn("Driver is null, cannot take screenshot");
                return null;
            }

            // Take screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);

            // Generate filename
            String fileName = cleanFileName(testName) + "_" + suffix + ".png";
            String filePath = SCREENSHOT_DIR + fileName;
            File destFile = new File(filePath);

            // Copy screenshot to destination
            FileUtils.copyFile(sourceFile, destFile);

            logger.info("Screenshot saved: " + filePath);
            return filePath;

        } catch (IOException e) {
            logger.error("Failed to take screenshot: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while taking screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Take screenshot for failed test
     * @param testName Name of the failed test
     * @return Screenshot file path
     */
    public static String takeFailureScreenshot(String testName) {
        return takeScreenshot(testName, "FAILED_" + generateTimestamp());
    }

    /**
     * Take screenshot for passed test
     * @param testName Name of the passed test
     * @return Screenshot file path
     */
    public static String takePassedScreenshot(String testName) {
        return takeScreenshot(testName, "PASSED_" + generateTimestamp());
    }

    /**
     * Take screenshot at specific step
     * @param testName Name of the test
     * @param stepName Name of the step
     * @return Screenshot file path
     */
    public static String takeStepScreenshot(String testName, String stepName) {
        return takeScreenshot(testName, "STEP_" + cleanFileName(stepName) + "_" + generateTimestamp());
    }

    /**
     * Create screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Screenshot directory created: " + SCREENSHOT_DIR);
            } else {
                logger.warn("Failed to create screenshot directory: " + SCREENSHOT_DIR);
            }
        }
    }

    /**
     * Generate timestamp for filename
     * @return Formatted timestamp string
     */
    private static String generateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
        return now.format(formatter);
    }

    /**
     * Clean filename to remove invalid characters
     * @param fileName Original filename
     * @return Cleaned filename
     */
    private static String cleanFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }

        // Replace invalid characters with underscores
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_")
                .replaceAll("_{2,}", "_")  // Replace multiple underscores with single
                .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
    }

    /**
     * Get absolute path of screenshot
     * @param relativePath Relative path of screenshot
     * @return Absolute path
     */
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        File file = new File(relativePath);
        return file.getAbsolutePath();
    }

    /**
     * Check if screenshot file exists
     * @param filePath Path to screenshot file
     * @return true if file exists, false otherwise
     */
    public static boolean screenshotExists(String filePath) {
        if (filePath == null) {
            return false;
        }

        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    /**
     * Get screenshot file size in bytes
     * @param filePath Path to screenshot file
     * @return File size in bytes, or -1 if file doesn't exist
     */
    public static long getScreenshotSize(String filePath) {
        if (filePath == null) {
            return -1;
        }

        File file = new File(filePath);
        return file.exists() ? file.length() : -1;
    }

    /**
     * Clean old screenshots (older than specified days)
     * @param daysToKeep Number of days to keep screenshots
     */
    public static void cleanOldScreenshots(int daysToKeep) {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
            File[] files = screenshotDir.listFiles();

            if (files != null) {
                int deletedCount = 0;
                long totalSizeDeleted = 0;

                for (File file : files) {
                    if (file.isFile() && file.lastModified() < cutoffTime) {
                        long fileSize = file.length();
                        if (file.delete()) {
                            deletedCount++;
                            totalSizeDeleted += fileSize;
                        }
                    }
                }

                if (deletedCount > 0) {
                    double sizeMB = totalSizeDeleted / (1024.0 * 1024.0);
                    logger.info("Cleaned " + deletedCount + " old screenshots (" +
                            String.format("%.2f", sizeMB) + " MB freed)");
                }
            }

        } catch (Exception e) {
            logger.warn("Error cleaning old screenshots: " + e.getMessage());
        }
    }

    /**
     * Get total number of screenshots in directory
     * @return Number of screenshot files
     */
    public static int getScreenshotCount() {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                return 0;
            }

            File[] files = screenshotDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

            return files != null ? files.length : 0;

        } catch (Exception e) {
            logger.warn("Error counting screenshots: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get total size of all screenshots in directory
     * @return Total size in bytes
     */
    public static long getTotalScreenshotSize() {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                return 0;
            }

            File[] files = screenshotDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

            long totalSize = 0;
            if (files != null) {
                for (File file : files) {
                    totalSize += file.length();
                }
            }

            return totalSize;

        } catch (Exception e) {
            logger.warn("Error calculating screenshot directory size: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Take screenshot with browser information in filename
     * @param testName Name of the test
     * @return Screenshot file path
     */
    public static String takeScreenshotWithBrowserInfo(String testName) {
        String browserName = DriverManager.getCurrentBrowserName();
        String suffix = browserName + "_" + generateTimestamp();
        return takeScreenshot(testName, suffix);
    }

    /**
     * Take multiple screenshots with delay
     * @param testName Name of the test
     * @param count Number of screenshots to take
     * @param delayMillis Delay between screenshots in milliseconds
     * @return Array of screenshot file paths
     */
    public static String[] takeMultipleScreenshots(String testName, int count, int delayMillis) {
        String[] screenshots = new String[count];

        for (int i = 0; i < count; i++) {
            String suffix = "MULTI_" + (i + 1) + "_" + generateTimestamp();
            screenshots[i] = takeScreenshot(testName, suffix);

            if (i < count - 1 && delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    logger.warn("Screenshot delay interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return screenshots;
    }
}