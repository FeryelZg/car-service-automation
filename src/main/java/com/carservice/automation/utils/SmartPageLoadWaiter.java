package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Smart page load waiter that handles heavy web applications
 * Especially useful for first-time loads when browser cache is empty
 */
public class SmartPageLoadWaiter {

    private static final Logger logger = LogManager.getLogger(SmartPageLoadWaiter.class);

    /**
     * Comprehensive page load wait for heavy applications
     * Combines multiple waiting strategies for maximum reliability
     */
    public static void waitForPageToFullyLoad(WebDriver driver) {
        waitForPageToFullyLoad(driver, 120); // Default 2 minutes for heavy apps
    }

    /**
     * Comprehensive page load wait with custom timeout
     */
    public static void waitForPageToFullyLoad(WebDriver driver, int timeoutSeconds) {
        logger.info("🔄 Starting comprehensive page load wait (timeout: {}s)", timeoutSeconds);
        long startTime = System.currentTimeMillis();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

            // Step 1: Wait for basic document ready
            logger.info("📄 Step 1: Waiting for document.readyState = complete");
            waitForDocumentReady(driver, wait);

            // Step 2: Wait for jQuery if present
            logger.info("⚡ Step 2: Waiting for jQuery to finish (if present)");
            waitForJQueryIfPresent(driver, wait);

            // Step 3: Wait for Angular if present
            logger.info("🅰️ Step 3: Waiting for Angular to finish (if present)");
            waitForAngularIfPresent(driver, wait);

            // Step 4: Wait for no network activity (modern apps)
            logger.info("🌐 Step 4: Waiting for network activity to settle");
            waitForNetworkIdle(driver, wait);

            // Step 5: Wait for critical page elements (app-specific)
            logger.info("🎯 Step 5: Waiting for critical page elements");
            waitForCriticalElements(driver, wait);

            // Step 6: Final buffer for any remaining async operations
            logger.info("⏳ Step 6: Final buffer wait");
            Thread.sleep(2000);

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Page fully loaded in {}ms ({} seconds)", totalTime, totalTime / 1000.0);

        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.warn("⚠️ Page load wait completed with issues after {}ms: {}", totalTime, e.getMessage());

            // Take screenshot for debugging
            takePageLoadDebugScreenshot(driver, "page_load_timeout");
        }
    }

    /**
     * Wait for document.readyState = complete
     */
    private static void waitForDocumentReady(WebDriver driver, WebDriverWait wait) {
        try {
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String readyState = (String) js.executeScript("return document.readyState");
                    logger.debug("Document readyState: {}", readyState);
                    return "complete".equals(readyState);
                }
            });
            logger.info("✅ Document ready state: complete");
        } catch (Exception e) {
            logger.warn("⚠️ Document ready wait failed: {}", e.getMessage());
        }
    }

    /**
     * Wait for jQuery to finish loading and executing (if present)
     */
    private static void waitForJQueryIfPresent(WebDriver driver, WebDriverWait wait) {
        try {
            // Check if jQuery is present
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean jQueryPresent = (Boolean) js.executeScript("return typeof jQuery !== 'undefined'");

            if (jQueryPresent) {
                logger.info("📚 jQuery detected, waiting for completion");
                wait.until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver driver) {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        try {
                            Long activeRequests = (Long) js.executeScript("return jQuery.active");
                            logger.debug("jQuery active requests: {}", activeRequests);
                            return activeRequests == 0;
                        } catch (Exception e) {
                            return true; // If jQuery check fails, assume it's done
                        }
                    }
                });
                logger.info("✅ jQuery operations completed");
            } else {
                logger.debug("📚 jQuery not detected, skipping jQuery wait");
            }
        } catch (Exception e) {
            logger.debug("jQuery wait check failed: {}", e.getMessage());
        }
    }

    /**
     * Wait for Angular to finish loading (if present)
     */
    private static void waitForAngularIfPresent(WebDriver driver, WebDriverWait wait) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Check for Angular
            Boolean angularPresent = (Boolean) js.executeScript(
                    "return window.angular !== undefined || window.getAllAngularTestabilities !== undefined"
            );

            if (angularPresent) {
                logger.info("🅰️ Angular detected, waiting for completion");
                wait.until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver driver) {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        try {
                            return (Boolean) js.executeScript(
                                    "if (window.angular) {" +
                                            "  var injector = window.angular.element(document).injector();" +
                                            "  if (injector && injector.get) {" +
                                            "    var $http = injector.get('$http');" +
                                            "    return $http.pendingRequests.length === 0;" +
                                            "  }" +
                                            "}" +
                                            "return true;"
                            );
                        } catch (Exception e) {
                            return true;
                        }
                    }
                });
                logger.info("✅ Angular operations completed");
            } else {
                logger.debug("🅰️ Angular not detected, skipping Angular wait");
            }
        } catch (Exception e) {
            logger.debug("Angular wait check failed: {}", e.getMessage());
        }
    }

    /**
     * Wait for network activity to settle (for modern SPAs)
     */
    private static void waitForNetworkIdle(WebDriver driver, WebDriverWait wait) {
        try {
            // Use Performance API to check for network activity
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    try {
                        // Check if all resources have finished loading
                        Long performanceEntries = (Long) js.executeScript(
                                "var entries = performance.getEntriesByType('navigation');" +
                                        "if (entries.length > 0) {" +
                                        "  var nav = entries[0];" +
                                        "  return nav.loadEventEnd > 0 ? 1 : 0;" +
                                        "}" +
                                        "return 1;"
                        );

                        boolean networkIdle = performanceEntries > 0;
                        if (!networkIdle) {
                            logger.debug("🌐 Network still active...");
                        }
                        return networkIdle;

                    } catch (Exception e) {
                        return true; // If performance API fails, assume network is idle
                    }
                }
            });
            logger.info("✅ Network activity settled");
        } catch (Exception e) {
            logger.debug("Network idle wait failed: {}", e.getMessage());
        }
    }

    /**
     * Wait for critical page elements that indicate the page is ready
     */
    private static void waitForCriticalElements(WebDriver driver, WebDriverWait wait) {
        try {
            // Wait for body element to be present and have content
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    try {
                        WebElement body = driver.findElement(By.tagName("body"));
                        String bodyClass = body.getAttribute("class");
                        String bodyText = body.getText();

                        // Check if body has content and doesn't have loading classes
                        boolean hasContent = bodyText != null && bodyText.length() > 50;
                        boolean notLoading = bodyClass == null ||
                                (!bodyClass.contains("loading") && !bodyClass.contains("spinner"));

                        logger.debug("Body content length: {}, Loading classes: {}",
                                bodyText != null ? bodyText.length() : 0, !notLoading);

                        return hasContent && notLoading;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });
            logger.info("✅ Critical page elements are ready");
        } catch (Exception e) {
            logger.warn("⚠️ Critical elements wait failed: {}", e.getMessage());
        }
    }

    /**
     * Application-specific wait for AutoTeam pages
     */
    public static void waitForAutoTeamPageLoad(WebDriver driver) {
        logger.info("🚗 Waiting for AutoTeam application to load completely");

        try {
            // Get timeout from configuration
            int timeout = com.carservice.automation.utils.ConfigurationManager.getPageLoadTimeout();
            boolean useSmartWait = com.carservice.automation.utils.ConfigurationManager.useFastPageLoadDetection();

            logger.info("📋 Using timeout: {}s, Smart wait: {}", timeout, useSmartWait);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));

            if (useSmartWait) {
                // First, do the comprehensive wait
                waitForPageToFullyLoad(driver, timeout);
            } else {
                // Just basic document ready wait
                waitForDocumentReady(driver, wait);
                Thread.sleep(5000);
            }

            // Then wait for AutoTeam-specific elements
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    try {
                        // Look for common AutoTeam elements that indicate page is ready
                        boolean hasNavigation = !driver.findElements(By.cssSelector("nav, .navbar, .navigation")).isEmpty();
                        boolean hasMainContent = !driver.findElements(By.cssSelector("main, .main-content, .container")).isEmpty();
                        boolean noLoadingSpinners = driver.findElements(By.cssSelector(".loading, .spinner, .loader")).isEmpty();

                        logger.debug("AutoTeam elements - Nav: {}, Content: {}, No spinners: {}",
                                hasNavigation, hasMainContent, noLoadingSpinners);

                        return hasNavigation && hasMainContent && noLoadingSpinners;
                    } catch (Exception e) {
                        return false;
                    }
                }
            });

            // Additional buffer for AutoTeam app
            Thread.sleep(3000);

            logger.info("✅ AutoTeam application fully loaded and ready");

        } catch (Exception e) {
            logger.warn("⚠️ AutoTeam page load wait completed with warnings: {}", e.getMessage());
            takePageLoadDebugScreenshot(driver, "autoteam_load_timeout");
        }
    }

    /**
     * Quick wait for subsequent navigations (when cache is warm)
     */
    public static void waitForSubsequentPageLoad(WebDriver driver) {
        logger.info("⚡ Quick wait for subsequent page load (cache warm)");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Just basic waits since cache should be warm
            waitForDocumentReady(driver, wait);
            Thread.sleep(1000);

            logger.info("✅ Subsequent page load completed");
        } catch (Exception e) {
            logger.warn("⚠️ Subsequent page load wait failed: {}", e.getMessage());
        }
    }

    /**
     * Take debug screenshot for page load issues
     */
    private static void takePageLoadDebugScreenshot(WebDriver driver, String suffix) {
        try {
            String screenshotName = "PAGE_LOAD_DEBUG_" + suffix + "_" + System.currentTimeMillis();
            // Use your existing screenshot utility
            com.carservice.automation.utils.ScreenshotUtils.takeScreenshot(screenshotName);
            logger.info("🔍 Page load debug screenshot taken: {}", screenshotName);
        } catch (Exception e) {
            logger.debug("Could not take page load debug screenshot: {}", e.getMessage());
        }
    }

    /**
     * Check page load performance and log metrics
     */
    public static void logPageLoadMetrics(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            String performanceScript =
                    "var perfData = performance.getEntriesByType('navigation')[0];" +
                            "if (perfData) {" +
                            "  return {" +
                            "    'domContentLoaded': Math.round(perfData.domContentLoadedEventEnd - perfData.navigationStart)," +
                            "    'fullLoad': Math.round(perfData.loadEventEnd - perfData.navigationStart)," +
                            "    'firstPaint': performance.getEntriesByName('first-paint')[0] ? Math.round(performance.getEntriesByName('first-paint')[0].startTime) : 0" +
                            "  };" +
                            "}" +
                            "return null;";

            Object result = js.executeScript(performanceScript);
            if (result != null) {
                logger.info("📊 Page Load Metrics: {}", result.toString());
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve page load metrics: {}", e.getMessage());
        }
    }
}