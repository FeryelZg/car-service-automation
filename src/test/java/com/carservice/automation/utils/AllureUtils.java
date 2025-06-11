package com.carservice.automation.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for enhanced Allure reporting functionality
 * Provides convenient methods for adding attachments, steps, and metadata to Allure reports
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
     * Add a step with message to Allure report
     * @param message Step message
     */
    @Step("{message}")
    public static void logStep(String message) {
        logger.info("Allure Step: {}", message);
    }

    /**
     * Start a test case with initial information
     * @param testName Name of the test case
     */
    public static void startTestCase(String testName) {
        logStep("Starting test case: " + testName);
        addParameter("Test Start Time", getCurrentTimestamp());
        addParameter("Test Name", testName);
    }

    /**
     * Add test result information
     * @param result Test result message
     */
    public static void addTestResult(String result) {
        addParameter("Test Result", result);
        addParameter("Test End Time", getCurrentTimestamp());
        logStep("Test completed: " + result);
    }

    /**
     * Add failure information to Allure report
     * @param failureReason Reason for failure
     * @param exception Exception that caused failure
     */
    public static void addFailureInfo(String failureReason, Exception exception) {
        addParameter("Failure Reason", failureReason);
        addParameter("Exception Type", exception.getClass().getSimpleName());
        addParameter("Exception Message", exception.getMessage());
        addParameter("Failure Time", getCurrentTimestamp());

        // Take screenshot on failure
        if (driver != null) {
            attachScreenshot("Failure Screenshot");
        }

        // Add stack trace as attachment
        attachText("Stack Trace", getStackTrace(exception));

        logStep("Test failed: " + failureReason);
    }

    /**
     * Attach screenshot to Allure report
     * @param name Name for the screenshot
     * @return Screenshot as byte array
     */
    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachScreenshot(String name) {
        if (driver == null) {
            logger.warn("Driver is null, cannot take screenshot");
            return new byte[0];
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            logger.info("Screenshot attached to Allure: {}", name);
            return screenshot;
        } catch (Exception e) {
            logger.error("Failed to take screenshot for Allure: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Attach text content to Allure report
     * @param name Attachment name
     * @param content Text content
     * @return Text content as byte array
     */
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

    /**
     * Attach file to Allure report
     * @param name Attachment name
     * @param filePath Path to file
     */
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

    /**
     * Add parameter to Allure report
     * @param name Parameter name
     * @param value Parameter value
     */
    public static void addParameter(String name, String value) {
        Allure.parameter(name, value);
        logger.debug("Allure parameter added: {} = {}", name, value);
    }

    /**
     * Add environment information to Allure report
     */
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

    /**
     * Add test data information to Allure report
     */
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

    /**
     * Add link to Allure report
     * @param name Link name
     * @param url Link URL
     */
    public static void addLink(String name, String url) {
        Allure.link(name, url);
        logger.info("Link added to Allure: {} -> {}", name, url);
    }

    /**
     * Add issue link to Allure report
     * @param issueId Issue ID
     */
    public static void addIssue(String issueId) {
        Allure.issue("issueId",issueId);
        logger.info("Issue link added to Allure: {}", issueId);
    }

    /**
     * Add TMS link to Allure report
     * @param testCaseId Test case ID
     */
    public static void addTmsLink(String testCaseId) {
        Allure.tms("testCaseId",testCaseId);
        logger.info("TMS link added to Allure: {}", testCaseId);
    }

    /**
     * Add description to current test
     * @param description Test description
     */
    public static void addDescription(String description) {
        Allure.description(description);
        logger.info("Description added to Allure report");
    }

    /**
     * Add label to current test
     * @param name Label name
     * @param value Label value
     */
    public static void addLabel(String name, String value) {
        Allure.label(name, value);
        logger.debug("Label added to Allure: {} = {}", name, value);
    }

    /**
     * Add performance metrics
     * @param operation Operation name
     * @param duration Duration in milliseconds
     */
    public static void addPerformanceMetric(String operation, long duration) {
        addParameter(operation + " Duration (ms)", String.valueOf(duration));
        addParameter(operation + " Performance", duration < 5000 ? "Good" : "Slow");
        logStep("Performance metric recorded: " + operation + " took " + duration + "ms");
    }

    /**
     * Create a step that measures execution time
     * @param stepName Name of the step
     * @param action Runnable action to execute
     */
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

    /**
     * Get current timestamp as formatted string
     * @return Formatted timestamp
     */
    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Get stack trace as string
     * @param exception Exception to get stack trace from
     * @return Stack trace as string
     */
    private static String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getMessage()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Cleanup resources
     */
    public static void cleanup() {
        driver = null;
        configReader = null;
    }
}