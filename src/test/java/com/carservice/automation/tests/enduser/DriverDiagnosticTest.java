package com.carservice.automation.tests.enduser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

public class DriverDiagnosticTest {

    private static final Logger logger = LogManager.getLogger(DriverDiagnosticTest.class);

    @Test
    public void diagnoseDriverIssues() {
        logger.info("🔍 === DRIVER DIAGNOSTIC TEST ===");

        // Step 1: Check system information
        checkSystemInfo();

        // Step 2: Test WebDriverManager
        testWebDriverManager();

        // Step 3: Test manual driver creation
        testManualDriverCreation();

        // Step 4: Test DriverManager class
        testDriverManagerClass();
    }

    private void checkSystemInfo() {
        logger.info("📊 === SYSTEM INFORMATION ===");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("OS Name: {}", System.getProperty("os.name"));
        logger.info("OS Version: {}", System.getProperty("os.version"));
        logger.info("OS Architecture: {}", System.getProperty("os.arch"));
        logger.info("User Directory: {}", System.getProperty("user.dir"));

        // Check PATH for Chrome
        String path = System.getenv("PATH");
        boolean chromeInPath = path != null && (
                path.contains("Chrome") ||
                        path.contains("chrome") ||
                        path.contains("Google")
        );
        logger.info("Chrome likely in PATH: {}", chromeInPath);

        // Check Chrome installation paths
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            logger.info("Windows detected - Chrome should be in Program Files");
        } else if (os.contains("mac")) {
            logger.info("Mac detected - Chrome should be in Applications");
        } else {
            logger.info("Linux detected - Chrome should be in /usr/bin or /opt");
        }
    }

    private void testWebDriverManager() {
        logger.info("🧪 === TESTING WEBDRIVERMANAGER ===");

        try {
            logger.info("Setting up WebDriverManager for Chrome...");
            WebDriverManager.chromedriver().setup();
            logger.info("✅ WebDriverManager setup successful");

            // Check what version was downloaded
            String driverPath = WebDriverManager.chromedriver().getDownloadedDriverPath();
            logger.info("Downloaded driver path: {}", driverPath);

        } catch (Exception e) {
            logger.error("❌ WebDriverManager failed: {}", e.getMessage());
            e.printStackTrace();

            // Try clearing cache and retry
            try {
                logger.info("Clearing WebDriverManager cache and retrying...");
                WebDriverManager.chromedriver().clearDriverCache().setup();
                logger.info("✅ WebDriverManager retry successful");
            } catch (Exception retryEx) {
                logger.error("❌ WebDriverManager retry also failed: {}", retryEx.getMessage());
            }
        }
    }

    private void testManualDriverCreation() {
        logger.info("🧪 === TESTING MANUAL DRIVER CREATION ===");
        WebDriver driver = null;

        try {
            logger.info("Creating ChromeOptions...");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--headless"); // Start with headless to avoid GUI issues

            logger.info("Creating ChromeDriver...");
            driver = new ChromeDriver(options);

            logger.info("✅ ChromeDriver created successfully!");
            logger.info("Driver info: {}", driver.toString());

            // Test basic functionality
            logger.info("Testing navigation...");
            driver.get("data:,"); // Simple data URL
            String currentUrl = driver.getCurrentUrl();
            logger.info("✅ Navigation test passed. Current URL: {}", currentUrl);

        } catch (Exception e) {
            logger.error("❌ Manual driver creation failed: {}", e.getMessage());
            e.printStackTrace();

            // Try to give more specific error information
            if (e.getMessage().contains("chrome")) {
                logger.error("💡 Chrome-related issue. Check if Chrome browser is installed.");
            }
            if (e.getMessage().contains("driver")) {
                logger.error("💡 Driver-related issue. Check WebDriverManager setup.");
            }

        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    logger.info("✅ Driver cleanup successful");
                } catch (Exception cleanupEx) {
                    logger.warn("⚠️ Driver cleanup warning: {}", cleanupEx.getMessage());
                }
            }
        }
    }

    private void testDriverManagerClass() {
        logger.info("🧪 === TESTING DRIVERMANAGER CLASS ===");

        try {
            // Check if DriverManager class exists and is accessible
            Class<?> driverManagerClass = Class.forName("com.carservice.automation.base.DriverManager");
            logger.info("✅ DriverManager class found: {}", driverManagerClass.getName());

            // Try to call the initialization method
            logger.info("Testing DriverManager.initializeDriver...");

            // Use reflection to call the method safely
            java.lang.reflect.Method initMethod = driverManagerClass.getMethod("initializeDriver", String.class, boolean.class);
            WebDriver driver = (WebDriver) initMethod.invoke(null, "chrome", true); // headless mode

            if (driver != null) {
                logger.info("✅ DriverManager.initializeDriver successful!");
                logger.info("Driver info: {}", driver.toString());

                // Test basic functionality
                driver.get("data:,");
                logger.info("✅ DriverManager driver navigation test passed");

                // Cleanup
                java.lang.reflect.Method quitMethod = driverManagerClass.getMethod("quitDriver");
                quitMethod.invoke(null);
                logger.info("✅ DriverManager cleanup successful");

            } else {
                logger.error("❌ DriverManager.initializeDriver returned null");
            }

        } catch (ClassNotFoundException e) {
            logger.error("❌ DriverManager class not found: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ DriverManager test failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void checkDependencies() {
        logger.info("📦 === CHECKING DEPENDENCIES ===");

        // Check Selenium
        try {
            Class.forName("org.openqa.selenium.WebDriver");
            logger.info("✅ Selenium WebDriver found");
        } catch (ClassNotFoundException e) {
            logger.error("❌ Selenium WebDriver not found");
        }

        // Check WebDriverManager
        try {
            Class.forName("io.github.bonigarcia.wdm.WebDriverManager");
            logger.info("✅ WebDriverManager found");
        } catch (ClassNotFoundException e) {
            logger.error("❌ WebDriverManager not found");
        }

        // Check TestNG
        try {
            Class.forName("org.testng.annotations.Test");
            logger.info("✅ TestNG found");
        } catch (ClassNotFoundException e) {
            logger.error("❌ TestNG not found");
        }

        // Check Log4j
        try {
            Class.forName("org.apache.logging.log4j.LogManager");
            logger.info("✅ Log4j found");
        } catch (ClassNotFoundException e) {
            logger.error("❌ Log4j not found");
        }
    }
}