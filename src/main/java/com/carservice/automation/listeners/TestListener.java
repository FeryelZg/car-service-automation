package com.carservice.automation.listeners;

import com.carservice.automation.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("========================================");
        logger.info("STARTING TEST: " + result.getMethod().getMethodName());
        logger.info("TEST CLASS: " + result.getTestClass().getName());
        logger.info("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("✅ TEST PASSED: " + result.getMethod().getMethodName());

        // Optionally take screenshot on success (can be disabled in config)
        // String screenshotPath = ScreenshotUtils.takeScreenshot(result.getMethod().getMethodName() + "_PASSED");

        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("Test execution time: " + duration + " ms");
        logger.info("========================================");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("❌ TEST FAILED: " + result.getMethod().getMethodName());
        logger.error("Failure reason: " + result.getThrowable().getMessage());

        // Take screenshot on failure
        try {
            String screenshotPath = ScreenshotUtils.takeFailureScreenshot(result.getMethod().getMethodName());
            if (screenshotPath != null) {
                logger.info("Screenshot saved: " + screenshotPath);

                // Set screenshot path as system property for reporting
                System.setProperty("screenshot.path", screenshotPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to take failure screenshot: " + e.getMessage());
        }

        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("Test execution time: " + duration + " ms");
        logger.error("========================================");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⏭️ TEST SKIPPED: " + result.getMethod().getMethodName());

        if (result.getThrowable() != null) {
            logger.warn("Skip reason: " + result.getThrowable().getMessage());
        }

        logger.warn("========================================");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.warn("⚠️ TEST FAILED BUT WITHIN SUCCESS PERCENTAGE: " + result.getMethod().getMethodName());
        logger.warn("========================================");
    }
}