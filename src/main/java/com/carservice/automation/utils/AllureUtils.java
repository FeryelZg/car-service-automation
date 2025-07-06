package com.carservice.automation.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Enhanced AllureUtils with better screenshot handling
 */
public class AllureUtils {

    private static final Logger logger = LogManager.getLogger(AllureUtils.class);
    private static WebDriver driver;
    private static ConfigReader configReader;

    // Initialize with driver reference
    public static void initialize(WebDriver webDriver) {
        driver = webDriver;
        configReader = new ConfigReader();
    }

    /**
     * Enhanced screenshot attachment with better error handling and validation
     */
    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachScreenshot(String name) {
        if (driver == null) {
            logger.warn("Driver is null, cannot take screenshot for: {}", name);
            return createErrorScreenshot("Driver is null");
        }

        try {
            // Wait for page to be ready
            waitForPageReady();

            // Ensure we're taking screenshot of visible content
            scrollToTop();

            // Take screenshot
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            if (screenshot == null || screenshot.length == 0) {
                logger.warn("Screenshot is empty for: {}", name);
                return createErrorScreenshot("Empty screenshot");
            }

            // Also save to file system for debugging
            saveScreenshotToFile(screenshot, name);

            logger.info("✅ Screenshot captured successfully: {} ({} bytes)", name, screenshot.length);
            return screenshot;

        } catch (Exception e) {
            logger.error("❌ Failed to capture screenshot '{}': {}", name, e.getMessage());
            return createErrorScreenshot("Screenshot failed: " + e.getMessage());
        }
    }

    /**
     * Enhanced screenshot with retry mechanism
     */
    public static byte[] attachScreenshotWithRetry(String name, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(1000); // Wait before retry
                byte[] screenshot = attachScreenshot(name + (i > 0 ? "_retry" + i : ""));

                if (screenshot != null && screenshot.length > 1000) { // Basic validation
                    return screenshot;
                }
            } catch (Exception e) {
                logger.warn("Screenshot attempt {} failed for {}: {}", i + 1, name, e.getMessage());
            }
        }

        logger.error("All screenshot attempts failed for: {}", name);
        return createErrorScreenshot("All retry attempts failed");
    }

    /**
     * Wait for page to be ready before taking screenshot
     */
    private static void waitForPageReady() {
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // Wait for page load
                for (int i = 0; i < 30; i++) {
                    String readyState = (String) js.executeScript("return document.readyState");
                    if ("complete".equals(readyState)) {
                        break;
                    }
                    Thread.sleep(100);
                }

                // Wait for any pending animations/transitions
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.debug("Could not wait for page ready: {}", e.getMessage());
        }
    }

    /**
     * Scroll to top of page for better screenshots
     */
    private static void scrollToTop() {
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.scrollTo(0, 0);");
                Thread.sleep(200);
            }
        } catch (Exception e) {
            logger.debug("Could not scroll to top: {}", e.getMessage());
        }
    }

    /**
     * Save screenshot to file system for debugging
     */
    private static void saveScreenshotToFile(byte[] screenshot, String name) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = String.format("allure_screenshot_%s_%s.png",
                    name.replaceAll("[^a-zA-Z0-9]", "_"), timestamp);

            File screenshotDir = new File("target/allure-results/screenshots");
            screenshotDir.mkdirs();

            File screenshotFile = new File(screenshotDir, fileName);
            Files.write(screenshotFile.toPath(), screenshot);

            logger.debug("Screenshot saved to: {}", screenshotFile.getAbsolutePath());

        } catch (Exception e) {
            logger.debug("Could not save screenshot to file: {}", e.getMessage());
        }
    }

    /**
     * Create a simple error image when screenshot fails
     */
    private static byte[] createErrorScreenshot(String errorMessage) {
        try {
            // Create a simple text image using Java
            String errorText = "Screenshot Error: " + errorMessage;
            return errorText.getBytes("UTF-8");
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Take full page screenshot (if supported)
     */
    @Attachment(value = "{name} - Full Page", type = "image/png")
    public static byte[] attachFullPageScreenshot(String name) {
        try {
            if (driver instanceof JavascriptExecutor) {
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // Get full page dimensions
                Long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");
                Long clientHeight = (Long) js.executeScript("return window.innerHeight");

                logger.info("Taking full page screenshot - Height: {}, Viewport: {}", scrollHeight, clientHeight);

                // For now, just take regular screenshot
                // Full page screenshot would require more complex implementation
                return attachScreenshot(name + "_fullpage");
            }
        } catch (Exception e) {
            logger.warn("Full page screenshot failed, falling back to regular: {}", e.getMessage());
        }

        return attachScreenshot(name);
    }

    /**
     * Attach screenshot with current URL info
     */
    public static void attachScreenshotWithContext(String name) {
        try {
            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();

            addParameter("Current URL", currentUrl);
            addParameter("Page Title", pageTitle);
            addParameter("Screenshot Context", name);

            attachScreenshot(name + " [" + pageTitle + "]");

        } catch (Exception e) {
            logger.warn("Could not attach screenshot with context: {}", e.getMessage());
            attachScreenshot(name);
        }
    }

    /**
     * Enhanced failure handling with detailed info
     */
    public static void addFailureInfo(String failureReason, Exception exception) {
        addParameter("Failure Reason", failureReason);
        addParameter("Exception Type", exception.getClass().getSimpleName());
        addParameter("Exception Message", exception.getMessage());
        addParameter("Failure Time", getCurrentTimestamp());

        try {
            // Get current page info
            if (driver != null) {
                addParameter("Page URL at Failure", driver.getCurrentUrl());
                addParameter("Page Title at Failure", driver.getTitle());

                // Take multiple screenshots for better debugging
                attachScreenshotWithRetry("FAILURE_Main", 3);

                // Wait a bit and take another screenshot
                Thread.sleep(1000);
                attachScreenshot("FAILURE_Delayed");
            }
        } catch (Exception e) {
            logger.warn("Could not capture failure context: {}", e.getMessage());
        }

        // Add stack trace as attachment
        attachText("Stack Trace", getStackTrace(exception));

        logStep("❌ Test failed: " + failureReason);
    }

    // Keep all your existing methods unchanged...
    @Step("{message}")
    public static void logStep(String message) {
        logger.info("Allure Step: {}", message);
    }

    public static void startTestCase(String testName) {
        logStep("Starting test case: " + testName);
        addParameter("Test Start Time", getCurrentTimestamp());
        addParameter("Test Name", testName);
    }

    public static void addTestResult(String result) {
        addParameter("Test Result", result);
        addParameter("Test End Time", getCurrentTimestamp());
        logStep("Test completed: " + result);
    }

    @Attachment(value = "{name}", type = "text/plain")
    public static byte[] attachText(String name, String content) {
        try {
            logger.info("Text attachment added to Allure: {}", name);
            return content.getBytes();
        } catch (Exception e) {
            logger.error("Failed to attach text to Allure: {}", e.getMessage());
            return new byte[0];
        }
    }

    public static void attachFile(String name, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                Allure.addAttachment(name, new ByteArrayInputStream(fileContent));
                logger.info("File attached to Allure: {} ({})", name, filePath);
            } else {
                logger.warn("File not found for Allure attachment: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to attach file to Allure: {}", e.getMessage());
        }
    }

    public static void addParameter(String name, String value) {
        Allure.parameter(name, value);
        logger.debug("Allure parameter added: {} = {}", name, value);
    }

    public static void addEnvironmentInfo() {
        if (configReader == null) {
            configReader = new ConfigReader();
        }

        addParameter("Environment", configReader.getProperty("environment", "dev"));
        addParameter("Browser", configReader.getProperty("browser", "chrome"));
        addParameter("Headless Mode", configReader.getProperty("headless", "false"));
        addParameter("Application URL", configReader.getProperty("enduser.app.url", "N/A"));
        addParameter("Test Execution Time", getCurrentTimestamp());
        addParameter("Operating System", System.getProperty("os.name"));
        addParameter("Java Version", System.getProperty("java.version"));

        logStep("Environment information added to report");
    }

    public static void addTestData() {
        if (configReader == null) {
            configReader = new ConfigReader();
        }

        addParameter("Vehicle Plate Serie", configReader.getProperty("vehicle.plate.serie", "N/A"));
        addParameter("Vehicle Plate Numero", configReader.getProperty("vehicle.plate.numero", "N/A"));
        addParameter("Vehicle Chassis Number", configReader.getProperty("vehicle.chassis.number", "N/A"));
        addParameter("Vehicle Mileage", configReader.getProperty("vehicle.mileage", "N/A"));
        addParameter("Test Description", configReader.getProperty("vehicle.description", "N/A"));

        logStep("Test data information added to report");
    }

    public static void addLink(String name, String url) {
        Allure.link(name, url);
        logger.info("Link added to Allure: {} -> {}", name, url);
    }

    public static void addIssue(String issueId) {
        Allure.issue("issueId", issueId);
        logger.info("Issue link added to Allure: {}", issueId);
    }

    public static void addTmsLink(String testCaseId) {
        Allure.tms("testCaseId", testCaseId);
        logger.info("TMS link added to Allure: {}", testCaseId);
    }

    public static void addDescription(String description) {
        Allure.description(description);
        logger.info("Description added to Allure report");
    }

    public static void addLabel(String name, String value) {
        Allure.label(name, value);
        logger.debug("Label added to Allure: {} = {}", name, value);
    }

    public static void addPerformanceMetric(String operation, long duration) {
        addParameter(operation + " Duration (ms)", String.valueOf(duration));
        addParameter(operation + " Performance", duration < 5000 ? "Good" : "Slow");
        logStep("Performance metric recorded: " + operation + " took " + duration + "ms");
    }

    public static void timedStep(String stepName, Runnable action) {
        long startTime = System.currentTimeMillis();

        try {
            logStep("Starting timed step: " + stepName);
            action.run();
            long duration = System.currentTimeMillis() - startTime;
            addPerformanceMetric(stepName, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            addPerformanceMetric(stepName + " (Failed)", duration);
            throw e;
        }
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getMessage()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void cleanup() {
        driver = null;
        configReader = null;
    }
}