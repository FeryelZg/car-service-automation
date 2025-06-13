package com.carservice.automation.stepdefinitions;

import com.carservice.automation.base.DriverManager;
import com.carservice.automation.utils.AllureUtils;
import com.carservice.automation.utils.ConfigurationManager;
import com.carservice.automation.utils.ScreenshotUtils;
import com.carservice.automation.utils.FastPageLoadDetector;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Cucumber Hooks with robust navigation and better error handling
 * Handles different scenarios and provides fallback mechanisms
 */
public class CucumberHooks {

    private static final Logger logger = LogManager.getLogger(CucumberHooks.class);

    /**
     * Runs before each scenario
     * Sets up the browser and navigates to the application
     */
    @Before
    public void beforeScenario(Scenario scenario) {
        logger.info("🚀 === Starting Scenario: {} ===", scenario.getName());

        try {
            // Log current configuration for debugging
            ConfigurationManager.logCurrentConfiguration();

            // Initialize AllureUtils for the scenario
            AllureUtils.startTestCase(scenario.getName());

            // Set up browser
            setupBrowser();

            // Navigate to appropriate application based on scenario
            navigateBasedOnScenario(scenario);

            logger.info("✅ Scenario setup completed successfully");

        } catch (Exception e) {
            logger.error("❌ Scenario setup failed: {}", e.getMessage());
            AllureUtils.addFailureInfo("Scenario setup failed", e);

            // Take screenshot for debugging
            takeDebugScreenshot("setup_failed");

            throw new RuntimeException("Failed to set up scenario: " + scenario.getName(), e);
        }
    }

    /**
     * Runs after each scenario
     * Takes screenshot if failed and cleans up resources
     */
    @After
    public void afterScenario(Scenario scenario) {
        logger.info("🏁 === Finishing Scenario: {} ===", scenario.getName());

        try {
            // Handle scenario result
            handleScenarioResult(scenario);

            // Clean up driver
            cleanupDriver();

            // Clean up AllureUtils
            AllureUtils.cleanup();

            logger.info("✅ Scenario cleanup completed");

        } catch (Exception e) {
            logger.warn("⚠️ Error during scenario cleanup: {}", e.getMessage());
        }
    }

    // Private helper methods

    private void setupBrowser() {
        // Get browser configuration from ConfigurationManager
        String browser = ConfigurationManager.getBrowser();
        boolean headless = ConfigurationManager.isHeadless();

        logger.info("🌐 Browser configuration: {} (headless: {})", browser, headless);

        // Initialize driver using DriverManager
        WebDriver driver = DriverManager.initializeDriver(browser, headless);

        // Configure additional timeouts for problematic sites
        configureDriverTimeouts(driver);

        // Initialize AllureUtils with driver (after driver is created)
        AllureUtils.initialize(driver);

        logger.info("✅ Browser setup completed successfully");
    }

    private void configureDriverTimeouts(WebDriver driver) {
        try {
            // Increase timeouts for slow-loading pages
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60)); // Increased from 30s
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));   // Increased from 10s
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(45));   // Increased from 30s

            logger.info("⏱️ Driver timeouts configured: PageLoad=60s, Implicit=15s, Script=45s");
        } catch (Exception e) {
            logger.warn("⚠️ Could not configure driver timeouts: {}", e.getMessage());
        }
    }

    private void navigateBasedOnScenario(Scenario scenario) {
        String scenarioName = scenario.getName().toLowerCase();

        // Determine which application to navigate to based on scenario name
        if (scenarioName.contains("intervention") || scenarioName.contains("backoffice")) {
            navigateToBackofficeApp();
        } else {
            // Default: navigate to end-user app for appointment booking
            navigateToEndUserApp();
        }
    }

    private void navigateToEndUserApp() {
        try {
            String url = ConfigurationManager.getEndUserAppUrl();
            logger.info("🌐 Navigating to End User App: {}", url);

            navigateWithRetry(url, 3);

            logger.info("✅ End User App navigation completed successfully");

        } catch (Exception e) {
            logger.error("❌ End User App navigation failed: {}", e.getMessage());
            throw new RuntimeException("Navigation to end user application failed", e);
        }
    }

    private void navigateToBackofficeApp() {
        try {
            String url = ConfigurationManager.getBackofficeAppUrl();
            logger.info("🏢 Navigating to Backoffice App: {}", url);

            navigateWithRetry(url, 3);

            logger.info("✅ Backoffice App navigation completed successfully");

        } catch (Exception e) {
            logger.error("❌ Backoffice App navigation failed: {}", e.getMessage());
            throw new RuntimeException("Navigation to backoffice application failed", e);
        }
    }

    private void navigateWithRetry(String url, int maxRetries) {
        WebDriver driver = DriverManager.getDriver();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("🔄 Navigation attempt {} of {} to: {}", attempt, maxRetries, url);

                // Clear any existing page first
                if (attempt > 1) {
                    clearBrowserState(driver);
                }

                // Navigate to URL
                driver.get(url);

                // Wait for page to load
                waitForPageToLoad(driver);

                // Verify navigation was successful
                String currentUrl = driver.getCurrentUrl();
                logger.info("✅ Navigation successful. Current URL: {}", currentUrl);

                return; // Success!

            } catch (TimeoutException e) {
                logger.warn("⏱️ Navigation attempt {} timed out: {}", attempt, e.getMessage());

                if (attempt == maxRetries) {
                    // Last attempt failed
                    takeDebugScreenshot("navigation_timeout_final");
                    throw new RuntimeException("Navigation failed after " + maxRetries + " attempts. URL: " + url, e);
                } else {
                    // Try again
                    logger.info("🔄 Retrying navigation in 2 seconds...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (Exception e) {
                logger.error("❌ Navigation attempt {} failed with error: {}", attempt, e.getMessage());

                if (attempt == maxRetries) {
                    takeDebugScreenshot("navigation_error_final");
                    throw new RuntimeException("Navigation failed after " + maxRetries + " attempts. URL: " + url, e);
                }
            }
        }
    }

    private void clearBrowserState(WebDriver driver) {
        try {
            logger.info("🧹 Clearing browser state before retry...");

            // Try to navigate to about:blank first
            driver.get("about:blank");
            Thread.sleep(1000);

            // Clear browser data if possible
            driver.manage().deleteAllCookies();

        } catch (Exception e) {
            logger.debug("Could not clear browser state: {}", e.getMessage());
        }
    }

    private void waitForPageToLoad(WebDriver driver) throws InterruptedException {
        try {
            logger.info("⚡ Fast page load detection...");

            // Use fast detection instead of comprehensive waiting
            FastPageLoadDetector.waitForAutoTeamPageReady(driver);

            logger.info("✅ Page ready - continuing with test");

        } catch (Exception e) {
            logger.warn("⚠️ Fast page load detection failed: {}", e.getMessage());

            // Ultra-fast fallback - just document ready
            try {
                FastPageLoadDetector.waitForBasicPageReady(driver);
                Thread.sleep(2000); // Minimal buffer
                logger.info("✅ Fallback page ready check completed");

            } catch (Exception fallbackError) {
                logger.warn("⚠️ Even basic page ready check failed: {}", fallbackError.getMessage());
                Thread.sleep(3000); // Last resort wait
            }
        }
    }

    private void handleScenarioResult(Scenario scenario) {
        if (scenario.isFailed()) {
            logger.info("📸 Scenario failed, taking screenshot");

            try {
                // Take screenshot using your existing utility
                String screenshotName = "FAILED_" + scenario.getName().replaceAll(" ", "_");
                ScreenshotUtils.takeFailureScreenshot(screenshotName);

                // Also attach to Allure report
                AllureUtils.attachScreenshot("Failure Screenshot");
                AllureUtils.addTestResult("FAILED - " + scenario.getName());

            } catch (Exception e) {
                logger.warn("⚠️ Could not take failure screenshot: {}", e.getMessage());
            }
        } else {
            logger.info("✅ Scenario passed successfully");
            AllureUtils.addTestResult("PASSED - " + scenario.getName());
        }
    }

    private void cleanupDriver() {
        try {
            if (DriverManager.isDriverInitialized()) {
                logger.info("🧹 Cleaning up driver...");
                DriverManager.quitDriver();
            } else {
                logger.debug("🧹 Driver was not initialized, nothing to clean up");
            }
        } catch (Exception e) {
            logger.warn("⚠️ Error cleaning up driver: {}", e.getMessage());
        }
    }

    private void takeDebugScreenshot(String suffix) {
        try {
            if (DriverManager.isDriverInitialized()) {
                String screenshotName = "DEBUG_" + suffix + "_" + System.currentTimeMillis();
                ScreenshotUtils.takeScreenshot(screenshotName);
                logger.info("🔍 Debug screenshot taken: {}", screenshotName);
            }
        } catch (Exception e) {
            logger.debug("Could not take debug screenshot: {}", e.getMessage());
        }
    }
}