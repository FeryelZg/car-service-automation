package com.carservice.automation.base;

import com.carservice.automation.utils.ConfigReader;
import com.carservice.automation.utils.ScreenshotUtils;
import com.carservice.automation.utils.AllureUtils;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.time.Duration;

public class BaseTest {

    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected ConfigReader config;
    protected WebDriverWait wait;

    // Default URLs if config fails
    private static final String DEFAULT_ENDUSER_URL = "https://autoteam-dev.teamdev.tn/home";
    private static final String DEFAULT_BACKOFFICE_URL = "https://autoteam-bo-dev.teamdev.tn/auth";

    @BeforeClass
    public void classSetup() {
        logger.info("🚀 === Starting Test Class Setup ===");
        logger.info("Class: {}", this.getClass().getSimpleName());
        logger.info("Thread: {}", Thread.currentThread().getName());

        try {
            config = new ConfigReader();
            logger.info("✅ Configuration loaded successfully");
        } catch (Exception e) {
            logger.warn("⚠️ Failed to load configuration, using defaults: {}", e.getMessage());
            config = null;
        }
    }

    @BeforeMethod
    @Parameters({"browser"})
    public void setup(@Optional("chrome") String browser) {
        logger.info("🔧 === Starting Test Method Setup ===");
        logger.info("Thread: {}", Thread.currentThread().getName());
        logger.info("Received browser parameter: {}", browser);

        try {
            // Get browser configuration
            String browserName = getConfigProperty("browser", browser);
            boolean headless = Boolean.parseBoolean(getConfigProperty("headless", "false"));

            logger.info("🌐 Browser configuration: {} (headless: {})", browserName, headless);

            // Initialize driver with detailed error handling
            logger.info("🔧 Calling DriverManager.initializeDriver...");

            try {
                driver = DriverManager.initializeDriver(browserName, headless);

                if (driver == null) {
                    logger.error("❌ CRITICAL: DriverManager.initializeDriver() returned null!");
                    throw new RuntimeException("Driver initialization returned null");
                }

                logger.info("✅ Driver initialized successfully: {}", driver.getClass().getSimpleName());

                // Initialize WebDriverWait
                int explicitWait = Integer.parseInt(getConfigProperty("explicit.wait", "20"));
                wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
                logger.info("✅ WebDriverWait initialized with {} seconds timeout", explicitWait);

                // Initialize AllureUtils with driver
                AllureUtils.initialize(driver);
                logger.info("✅ AllureUtils initialized");

                logger.info("🎉 Driver setup completed successfully!");

            } catch (Exception driverEx) {
                logger.error("💥 DRIVER INITIALIZATION FAILED!");
                logger.error("💥 Exception type: {}", driverEx.getClass().getSimpleName());
                logger.error("💥 Exception message: {}", driverEx.getMessage());

                provideTroubleshootingInfo(browserName, driverEx);
                driver = null;
                throw new RuntimeException("Driver initialization failed: " + driverEx.getMessage(), driverEx);
            }

        } catch (Exception e) {
            logger.error("💥 TEST SETUP FAILED!");
            logger.error("💥 Error: {}", e.getMessage());
            driver = null;
            logSystemDebugInfo();
            throw new RuntimeException("Test setup failed: " + e.getMessage(), e);
        }

        logger.info("✅ === Test Method Setup Completed Successfully ===");
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        logger.info("🧹 === Starting Test Method Teardown ===");
        logger.info("Test result status: {}", getTestResultStatus(result.getStatus()));

        try {
            // Handle test results with Allure
            handleTestResult(result);

            // Close browser
            if (driver != null) {
                logger.info("🚫 Quitting driver...");
                try {
                    driver.quit();
                    logger.info("✅ Driver quit successfully");
                } catch (Exception e) {
                    logger.warn("⚠️ Error while quitting driver: {}", e.getMessage());
                }
                driver = null;
            } else {
                logger.warn("⚠️ Driver was null during teardown - setup likely failed");
            }

            // Cleanup DriverManager and AllureUtils
            try {
                if (DriverManager.isDriverInitialized()) {
                    logger.info("🧹 Cleaning up DriverManager...");
                    DriverManager.quitDriver();
                }
                AllureUtils.cleanup();
            } catch (Exception e) {
                logger.warn("⚠️ Error during cleanup: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.warn("⚠️ Error during teardown: {}", e.getMessage());
        }

        logger.info("✅ Test method teardown completed");
    }

    @AfterClass
    public void classTeardown() {
        logger.info("🏁 === Test Class Completed: {} ===", this.getClass().getSimpleName());
    }

    /**
     * Navigate to End User Application
     */
    @Step("Navigate to End User Application")
    protected void navigateToEndUserApp() {
        if (driver == null) {
            logger.error("❌ CRITICAL: Driver is null in navigateToEndUserApp()!");
            throw new RuntimeException("Driver is null - setup may have failed. Check logs for DriverManager initialization errors.");
        }

        String url = getConfigProperty("enduser.app.url", DEFAULT_ENDUSER_URL);
        logger.info("🌐 Navigating to End User App: {}", url);

        try {
            AllureUtils.addParameter("Application URL", url);
            driver.get(url);
            logger.info("✅ Navigation completed successfully");
            waitForPageToLoad();
            AllureUtils.attachScreenshot("Application Loaded");
        } catch (Exception e) {
            logger.error("❌ Navigation failed: {}", e.getMessage());
            AllureUtils.attachScreenshot("Navigation Failed");
            throw new RuntimeException("Navigation to end user app failed", e);
        }
    }

    /**
     * Navigate to Backoffice Application
     */
    @Step("Navigate to Backoffice Application")
    protected void navigateToBackofficeApp() {
        if (driver == null) {
            logger.error("❌ CRITICAL: Driver is null in navigateToBackofficeApp()!");
            throw new RuntimeException("Driver is null - setup may have failed. Check logs for DriverManager initialization errors.");
        }

        String url = getConfigProperty("backoffice.app.url", DEFAULT_BACKOFFICE_URL);
        logger.info("🌐 Navigating to Backoffice App: {}", url);

        try {
            AllureUtils.addParameter("Backoffice URL", url);
            driver.get(url);
            logger.info("✅ Navigation completed successfully");
            waitForPageToLoad();
            AllureUtils.attachScreenshot("Backoffice Loaded");
        } catch (Exception e) {
            logger.error("❌ Navigation failed: {}", e.getMessage());
            AllureUtils.attachScreenshot("Navigation Failed");
            throw new RuntimeException("Navigation to backoffice app failed", e);
        }
    }

    /**
     * Wait for page to be fully loaded
     */
    @Step("Wait for page to load completely")
    protected void waitForPageToLoad() {
        try {
            logger.info("⏳ Waiting for page to load...");
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            Thread.sleep(1000); // Additional buffer
            logger.info("✅ Page loaded successfully");
            AllureUtils.logStep("Page loaded successfully");
        } catch (Exception e) {
            logger.warn("⚠️ Page load wait issue: {}", e.getMessage());
        }
    }

    /**
     * Take screenshot with custom name
     */
    @Step("Take screenshot: {name}")
    protected void takeScreenshot(String name) {
        try {
            if (driver != null) {
                ScreenshotUtils.takeScreenshot(name);
                AllureUtils.attachScreenshot(name);
                logger.info("✅ Screenshot taken: {}", name);
            } else {
                logger.warn("⚠️ Cannot take screenshot - driver is null");
            }
        } catch (Exception e) {
            logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Handle test results for Allure reporting
     */
    private void handleTestResult(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                AllureUtils.addTestResult("PASSED");
                AllureUtils.addParameter("Test Status", "PASSED");
                logger.info("✅ Test PASSED: {}", testName);
                break;

            case ITestResult.FAILURE:
                logger.info("❌ Test FAILED: {}", testName);
                try {
                    if (driver != null) {
                        AllureUtils.attachScreenshot("Failure Screenshot");
                        ScreenshotUtils.takeFailureScreenshot(testName);
                        logger.info("✅ Failure screenshot captured");
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Failed to take failure screenshot: {}", e.getMessage());
                }

                // Add failure information to Allure
                Throwable throwable = result.getThrowable();
                if (throwable != null) {
                    AllureUtils.addFailureInfo("Test failed: " + throwable.getMessage(),
                            (Exception) throwable);
                }
                break;

            case ITestResult.SKIP:
                AllureUtils.addParameter("Test Status", "SKIPPED");
                AllureUtils.logStep("Test was skipped");
                logger.info("⏭️ Test SKIPPED: {}", testName);
                break;
        }
    }

    /**
     * Get configuration property with fallback
     */
    private String getConfigProperty(String key, String defaultValue) {
        try {
            if (config != null) {
                String value = config.getProperty(key);
                if (value != null) {
                    logger.debug("📋 Config property '{}' = '{}'", key, value);
                    return value;
                } else {
                    logger.debug("📋 Config property '{}' not found, using default: {}", key, defaultValue);
                }
            } else {
                logger.debug("📋 Config is null, using default for '{}': {}", key, defaultValue);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Error reading config property '{}': {}", key, e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Provide troubleshooting information for driver initialization failures
     */
    private void provideTroubleshootingInfo(String browserName, Exception exception) {
        logger.error("🔧 === TROUBLESHOOTING INFORMATION ===");
        logger.error("Browser requested: {}", browserName);
        logger.error("Exception type: {}", exception.getClass().getSimpleName());

        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("chrome")) {
                logger.error("💡 Chrome-related issue detected:");
                logger.error("   - Ensure Chrome browser is installed");
                logger.error("   - Check Chrome version compatibility");
                logger.error("   - Try running: google-chrome --version");
            }
            if (message.contains("driver")) {
                logger.error("💡 WebDriver issue detected:");
                logger.error("   - WebDriverManager may have failed to download ChromeDriver");
                logger.error("   - Try clearing cache: rm -rf ~/.cache/selenium/");
                logger.error("   - Check internet connectivity");
            }
            if (message.contains("session")) {
                logger.error("💡 Session issue detected:");
                logger.error("   - Chrome may not be starting properly");
                logger.error("   - Try headless mode: -Dheadless=true");
                logger.error("   - Check for conflicting Chrome processes");
            }
        }
    }

    /**
     * Log system debug information
     */
    private void logSystemDebugInfo() {
        logger.error("🔍 === SYSTEM DEBUG INFORMATION ===");
        logger.error("Java Version: {}", System.getProperty("java.version"));
        logger.error("OS: {} {} {}",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        logger.error("Working Directory: {}", System.getProperty("user.dir"));
        logger.error("Java Home: {}", System.getProperty("java.home"));
    }

    /**
     * Get readable test result status
     */
    private String getTestResultStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASSED";
            case ITestResult.FAILURE: return "FAILED";
            case ITestResult.SKIP: return "SKIPPED";
            default: return "UNKNOWN";
        }
    }
}