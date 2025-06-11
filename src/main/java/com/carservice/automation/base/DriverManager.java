package com.carservice.automation.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * Initialize WebDriver based on browser parameter
     * @param browser Browser name (chrome, firefox, edge)
     * @param headless Run in headless mode
     * @return WebDriver instance
     */
    public static WebDriver initializeDriver(String browser, boolean headless) {
        logger.info("🚀 DriverManager.initializeDriver() called");
        logger.info("📋 Parameters - Browser: '{}', Headless: {}", browser, headless);
        logger.info("🧵 Current Thread: {}", Thread.currentThread().getName());

        WebDriver driver = null;

        try {
            // Check if driver already exists for this thread
            WebDriver existingDriver = driverThreadLocal.get();
            if (existingDriver != null) {
                logger.warn("⚠️ Driver already exists for this thread, quitting old driver first");
                try {
                    existingDriver.quit();
                } catch (Exception e) {
                    logger.warn("⚠️ Error quitting existing driver: {}", e.getMessage());
                }
                driverThreadLocal.remove();
            }

            logger.info("🔧 Starting driver initialization for browser: {}", browser.toLowerCase());

            switch (browser.toLowerCase().trim()) {
                case "chrome":
                    driver = initializeChromeDriver(headless);
                    break;

                case "firefox":
                    driver = initializeFirefoxDriver(headless);
                    break;

                case "edge":
                    driver = initializeEdgeDriver(headless);
                    break;

                default:
                    String errorMsg = "Browser not supported: " + browser + ". Supported: chrome, firefox, edge";
                    logger.error("❌ {}", errorMsg);
                    throw new IllegalArgumentException(errorMsg);
            }

            if (driver == null) {
                String errorMsg = "Driver initialization returned null for browser: " + browser;
                logger.error("❌ {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }

            // Configure timeouts
            logger.info("⏱️ Setting timeouts...");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

            // Maximize window (skip if headless)
            if (!headless) {
                logger.info("🖥️ Maximizing window...");
                driver.manage().window().maximize();
            } else {
                logger.info("🔇 Skipping window maximize (headless mode)");
            }

            // Store driver in ThreadLocal for parallel execution
            driverThreadLocal.set(driver);
            logger.info("📦 Driver stored in ThreadLocal for thread: {}", Thread.currentThread().getName());

            // Test driver functionality
            logger.info("🧪 Testing driver functionality...");
            String currentUrl = driver.getCurrentUrl();
            String sessionId = driver.toString();

            logger.info("✅ Driver initialized successfully!");
            logger.info("📊 Browser: {}", browser);
            logger.info("📊 Headless: {}", headless);
            logger.info("📊 Driver Class: {}", driver.getClass().getSimpleName());
            logger.info("📊 Session: {}", sessionId);
            logger.info("📊 Current URL: {}", currentUrl);

        } catch (Exception e) {
            logger.error("💥 Failed to initialize driver for browser '{}': {}", browser, e.getMessage());
            logger.error("💥 Exception details:", e);

            // Cleanup on failure
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception quitEx) {
                    logger.warn("⚠️ Error during cleanup: {}", quitEx.getMessage());
                }
            }
            driverThreadLocal.remove();

            throw new RuntimeException("Driver initialization failed for browser: " + browser, e);
        }

        logger.info("🎉 DriverManager.initializeDriver() completed successfully");
        return driver;
    }

    /**
     * Initialize Chrome WebDriver with detailed logging
     */
    private static WebDriver initializeChromeDriver(boolean headless) {
        logger.info("🔧 Initializing ChromeDriver...");

        try {
            logger.info("📥 Setting up ChromeDriver with WebDriverManager...");
            WebDriverManager.chromedriver().setup();
            logger.info("✅ WebDriverManager setup completed for Chrome");

            logger.info("⚙️ Configuring Chrome options...");
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--disable-notifications");
            chromeOptions.addArguments("--disable-popup-blocking");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--remote-allow-origins=*");

            // Additional stability options
            chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
            chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            chromeOptions.setExperimentalOption("useAutomationExtension", false);

            if (headless) {
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--window-size=1920,1080");
                logger.info("🔇 Chrome headless mode enabled");
            }

            logger.info("🚀 Creating ChromeDriver instance...");
            WebDriver chromeDriver = new ChromeDriver(chromeOptions);

            logger.info("✅ ChromeDriver created successfully");
            return chromeDriver;

        } catch (Exception e) {
            logger.error("❌ ChromeDriver initialization failed: {}", e.getMessage());
            throw new RuntimeException("ChromeDriver initialization failed", e);
        }
    }

    /**
     * Initialize Firefox WebDriver with detailed logging
     */
    private static WebDriver initializeFirefoxDriver(boolean headless) {
        logger.info("🔧 Initializing FirefoxDriver...");

        try {
            logger.info("📥 Setting up FirefoxDriver with WebDriverManager...");
            WebDriverManager.firefoxdriver().setup();
            logger.info("✅ WebDriverManager setup completed for Firefox");

            FirefoxOptions firefoxOptions = new FirefoxOptions();
            if (headless) {
                firefoxOptions.addArguments("--headless");
                logger.info("🔇 Firefox headless mode enabled");
            }

            logger.info("🚀 Creating FirefoxDriver instance...");
            WebDriver firefoxDriver = new FirefoxDriver(firefoxOptions);

            logger.info("✅ FirefoxDriver created successfully");
            return firefoxDriver;

        } catch (Exception e) {
            logger.error("❌ FirefoxDriver initialization failed: {}", e.getMessage());
            throw new RuntimeException("FirefoxDriver initialization failed", e);
        }
    }

    /**
     * Initialize Edge WebDriver with detailed logging
     */
    private static WebDriver initializeEdgeDriver(boolean headless) {
        logger.info("🔧 Initializing EdgeDriver...");

        try {
            logger.info("📥 Setting up EdgeDriver with WebDriverManager...");
            WebDriverManager.edgedriver().setup();
            logger.info("✅ WebDriverManager setup completed for Edge");

            EdgeOptions edgeOptions = new EdgeOptions();
            edgeOptions.addArguments("--disable-notifications");
            edgeOptions.addArguments("--disable-popup-blocking");
            edgeOptions.addArguments("--disable-dev-shm-usage");
            edgeOptions.addArguments("--no-sandbox");
            edgeOptions.addArguments("--remote-allow-origins=*");

            if (headless) {
                edgeOptions.addArguments("--headless");
                logger.info("🔇 Edge headless mode enabled");
            }

            logger.info("🚀 Creating EdgeDriver instance...");
            WebDriver edgeDriver = new EdgeDriver(edgeOptions);

            logger.info("✅ EdgeDriver created successfully");
            return edgeDriver;

        } catch (Exception e) {
            logger.error("❌ EdgeDriver initialization failed: {}", e.getMessage());
            throw new RuntimeException("EdgeDriver initialization failed", e);
        }
    }

    /**
     * Get the current thread's WebDriver instance
     * @return WebDriver instance
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            String errorMsg = "Driver is null for thread: " + Thread.currentThread().getName() +
                    ". Make sure to initialize driver before use.";
            logger.error("❌ {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        logger.debug("📋 Retrieved driver for thread: {}", Thread.currentThread().getName());
        return driver;
    }

    /**
     * Quit the driver and remove from ThreadLocal
     */
    public static void quitDriver() {
        logger.info("🚫 DriverManager.quitDriver() called for thread: {}", Thread.currentThread().getName());

        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                logger.info("🚫 Quitting driver...");
                driver.quit();
                logger.info("✅ Driver quit successfully");
            } catch (Exception e) {
                logger.warn("⚠️ Error while quitting driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
                logger.info("🧹 Driver removed from ThreadLocal");
            }
        } else {
            logger.warn("⚠️ Driver was already null, nothing to quit");
        }
    }

    /**
     * Close current browser window
     */
    public static void closeDriver() {
        logger.info("🔒 Closing current browser window...");
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.close();
                logger.info("✅ Driver closed successfully");
            } catch (Exception e) {
                logger.warn("⚠️ Error while closing driver: {}", e.getMessage());
            }
        } else {
            logger.warn("⚠️ Driver was null, nothing to close");
        }
    }

    /**
     * Check if driver is initialized
     * @return true if driver exists, false otherwise
     */
    public static boolean isDriverInitialized() {
        boolean initialized = driverThreadLocal.get() != null;
        logger.debug("📊 Driver initialized check for thread {}: {}",
                Thread.currentThread().getName(), initialized);
        return initialized;
    }

    /**
     * Get current browser name
     * @return Browser name or "unknown" if driver not initialized
     */
    public static String getCurrentBrowserName() {
        try {
            WebDriver driver = getDriver();
            String browserName = driver.getClass().getSimpleName().replace("Driver", "").toLowerCase();
            logger.debug("📊 Current browser: {}", browserName);
            return browserName;
        } catch (Exception e) {
            logger.warn("⚠️ Could not get browser name: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Get debug information about current driver state
     */
    public static String getDriverDebugInfo() {
        try {
            WebDriver driver = driverThreadLocal.get();
            if (driver == null) {
                return "Driver is NULL for thread: " + Thread.currentThread().getName();
            }

            return String.format("Driver Debug Info - Thread: %s, Class: %s, Session: %s, URL: %s",
                    Thread.currentThread().getName(),
                    driver.getClass().getSimpleName(),
                    driver.toString(),
                    driver.getCurrentUrl()
            );
        } catch (Exception e) {
            return "Error getting debug info: " + e.getMessage();
        }
    }

    // ... (keep all your other existing methods: refreshPage, navigateToUrl, etc.)

    /**
     * Refresh current page
     */
    public static void refreshPage() {
        try {
            getDriver().navigate().refresh();
            logger.info("📄 Page refreshed successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to refresh page: {}", e.getMessage());
            throw new RuntimeException("Page refresh failed", e);
        }
    }

    /**
     * Navigate to URL
     * @param url URL to navigate to
     */
    public static void navigateToUrl(String url) {
        try {
            getDriver().get(url);
            logger.info("🌐 Navigated to URL: {}", url);
        } catch (Exception e) {
            logger.error("❌ Failed to navigate to URL: {} - {}", url, e.getMessage());
            throw new RuntimeException("Navigation failed", e);
        }
    }

    /**
     * Get current URL
     * @return Current URL
     */
    public static String getCurrentUrl() {
        try {
            String currentUrl = getDriver().getCurrentUrl();
            logger.debug("📍 Current URL: {}", currentUrl);
            return currentUrl;
        } catch (Exception e) {
            logger.error("❌ Failed to get current URL: {}", e.getMessage());
            throw new RuntimeException("Failed to get current URL", e);
        }
    }

    /**
     * Get page title
     * @return Page title
     */
    public static String getPageTitle() {
        try {
            String title = getDriver().getTitle();
            logger.debug("📋 Page title: {}", title);
            return title;
        } catch (Exception e) {
            logger.error("❌ Failed to get page title: {}", e.getMessage());
            throw new RuntimeException("Failed to get page title", e);
        }
    }

    /**
     * Switch to window by title
     * @param windowTitle Window title to switch to
     * @return true if switch was successful
     */
    public static boolean switchToWindowByTitle(String windowTitle) {
        try {
            WebDriver driver = getDriver();
            String currentWindow = driver.getWindowHandle();

            for (String windowHandle : driver.getWindowHandles()) {
                driver.switchTo().window(windowHandle);
                if (driver.getTitle().contains(windowTitle)) {
                    logger.info("🔄 Switched to window with title: {}", windowTitle);
                    return true;
                }
            }

            // Switch back to original window if not found
            driver.switchTo().window(currentWindow);
            logger.warn("⚠️ Window with title '{}' not found", windowTitle);
            return false;

        } catch (Exception e) {
            logger.error("❌ Failed to switch to window: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get number of open windows/tabs
     * @return Number of windows
     */
    public static int getWindowCount() {
        try {
            int count = getDriver().getWindowHandles().size();
            logger.debug("📊 Number of open windows: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("❌ Failed to get window count: {}", e.getMessage());
            return 0;
        }
    }
}