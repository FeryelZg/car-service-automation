package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.By;

import java.time.Duration;

/**
 * Fast and lean page load detection
 * Moves on as soon as page is ready, no over-waiting
 */
public class FastPageLoadDetector {

    private static final Logger logger = LogManager.getLogger(FastPageLoadDetector.class);

    /**
     * Fast page load detection - exits as soon as page is ready
     */
    public static void waitForPageReady(WebDriver driver) {
        waitForPageReady(driver, 60); // Default 60 seconds max
    }

    /**
     * Fast page load detection with custom timeout
     */
    public static void waitForPageReady(WebDriver driver, int maxTimeoutSeconds) {
        logger.info("⚡ Fast page load detection (max {}s)", maxTimeoutSeconds);
        long startTime = System.currentTimeMillis();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(maxTimeoutSeconds));

            // Use a single comprehensive condition that checks everything at once
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    try {
                        JavascriptExecutor js = (JavascriptExecutor) driver;

                        // Check 1: Document ready
                        String readyState = (String) js.executeScript("return document.readyState");
                        if (!"complete".equals(readyState)) {
                            logger.debug("❌ Document not ready: {}", readyState);
                            return false;
                        }

                        // Check 2: Page has content (not blank/loading page)
                        Long bodyHeight = (Long) js.executeScript("return document.body ? document.body.scrollHeight : 0");
                        if (bodyHeight < 100) { // Page too small, likely still loading
                            logger.debug("❌ Page content too small: {}px", bodyHeight);
                            return false;
                        }

                        // Check 3: No loading indicators
                        Boolean hasLoadingIndicators = (Boolean) js.executeScript(
                                "var loadingElements = document.querySelectorAll('.loading, .spinner, .loader, [class*=\"loading\"], [class*=\"spinner\"]');" +
                                        "return loadingElements.length > 0 && Array.from(loadingElements).some(el => el.offsetParent !== null);"
                        );
                        if (hasLoadingIndicators) {
                            logger.debug("❌ Loading indicators still visible");
                            return false;
                        }

                        // Check 4: jQuery done (if present)
                        Boolean jQueryDone = (Boolean) js.executeScript(
                                "return typeof jQuery === 'undefined' || jQuery.active === 0"
                        );
                        if (!jQueryDone) {
                            logger.debug("❌ jQuery still active");
                            return false;
                        }

                        // All checks passed!
                        logger.debug("✅ All page ready checks passed");
                        return true;

                    } catch (Exception e) {
                        // If any check fails, assume not ready
                        logger.debug("❌ Page ready check failed: {}", e.getMessage());
                        return false;
                    }
                }
            });

            // Add minimal buffer
            Thread.sleep(1000);

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Page ready in {}ms", totalTime);

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.warn("⚠️ Page ready detection timed out after {}ms: {}", totalTime, e.getMessage());

            // Still take a small buffer in case page is usable
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Even faster detection for subsequent page loads (cache warm)
     */
    public static void waitForSubsequentPageReady(WebDriver driver) {
        logger.info("⚡⚡ Quick page ready check for subsequent load");
        long startTime = System.currentTimeMillis();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    try {
                        JavascriptExecutor js = (JavascriptExecutor) driver;

                        // Just check document ready and basic content
                        String readyState = (String) js.executeScript("return document.readyState");
                        Boolean hasContent = (Boolean) js.executeScript("return document.body && document.body.children.length > 0");

                        return "complete".equals(readyState) && hasContent;

                    } catch (Exception e) {
                        return false;
                    }
                }
            });

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Quick page ready in {}ms", totalTime);

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.warn("⚠️ Quick page ready timed out after {}ms", totalTime);
        }
    }

    /**
     * Ultra-fast check - just document ready
     */
    public static void waitForBasicPageReady(WebDriver driver) {
        logger.info("⚡⚡⚡ Basic document ready check");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String readyState = (String) js.executeScript("return document.readyState");
                    return "complete".equals(readyState);
                }
            });

            logger.info("✅ Document ready");

        } catch (Exception e) {
            logger.warn("⚠️ Document ready check failed: {}", e.getMessage());
        }
    }

    /**
     * Smart detection that chooses strategy based on scenario
     */
    public static void smartWaitForPage(WebDriver driver, boolean isFirstLoad) {
        if (isFirstLoad) {
            logger.info("🚀 First load detected - using thorough check");
            waitForPageReady(driver, 90); // Longer timeout for first load
        } else {
            logger.info("⚡ Subsequent load - using quick check");
            waitForSubsequentPageReady(driver);
        }
    }

    /**
     * Check if specific elements are present (for app-specific readiness)
     */
    public static boolean waitForSpecificElements(WebDriver driver, String... selectors) {
        logger.info("🎯 Waiting for specific elements: {}", String.join(", ", selectors));

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Reduced from 20 to 10

            return wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    for (String selector : selectors) {
                        try {
                            if (driver.findElements(By.cssSelector(selector)).isEmpty()) {
                                logger.debug("❌ Element not found: {}", selector);
                                return false;
                            }
                        } catch (Exception e) {
                            logger.debug("❌ Error checking element {}: {}", selector, e.getMessage());
                            return false;
                        }
                    }
                    logger.debug("✅ All specific elements found");
                    return true;
                }
            });

        } catch (Exception e) {
            logger.warn("⚠️ Specific elements check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * AutoTeam-specific fast page detection
     */
    public static void waitForAutoTeamPageReady(WebDriver driver) {
        logger.info("🚗 AutoTeam page ready detection");

        // Get timeout from configuration
        int timeout = com.carservice.automation.utils.ConfigurationManager.getPageLoadTimeout();
        boolean useFastMode = com.carservice.automation.utils.ConfigurationManager.useFastPageLoadDetection();

        logger.info("📋 Using timeout: {}s, Fast mode: {}", timeout, useFastMode);

        if (useFastMode) {
            // Fast detection
            waitForPageReady(driver, timeout);
        } else {
            // Just basic waiting
            waitForBasicPageReady(driver);
            try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // Quick check for AutoTeam-specific elements (optional)
        try {
            boolean hasNavOrContent = waitForSpecificElements(driver,
                    "nav", ".navbar", ".navigation", "main", ".main-content", ".container", "header", ".header");

            if (hasNavOrContent) {
                logger.info("✅ AutoTeam page elements detected");
            } else {
                logger.info("⚠️ AutoTeam elements not found, but continuing");
            }

        } catch (Exception e) {
            logger.warn("⚠️ AutoTeam element check failed: {}", e.getMessage());
        }
    }
}