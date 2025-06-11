package com.carservice.automation.utils;

import com.carservice.automation.base.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class WaitUtils {

    private static final Logger logger = LogManager.getLogger(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 20;
    private static final int DEFAULT_POLLING = 500;

    /**
     * Get WebDriverWait instance with default timeout
     * @return WebDriverWait instance
     */
    public static WebDriverWait getWebDriverWait() {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    /**
     * Get WebDriverWait instance with custom timeout
     * @param timeoutInSeconds Timeout in seconds
     * @return WebDriverWait instance
     */
    public static WebDriverWait getWebDriverWait(int timeoutInSeconds) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutInSeconds));
    }

    /**
     * Get FluentWait instance with custom timeout and polling interval
     * @param timeoutInSeconds Timeout in seconds
     * @param pollingInMillis Polling interval in milliseconds
     * @return FluentWait instance
     */
    public static FluentWait<WebDriver> getFluentWait(int timeoutInSeconds, int pollingInMillis) {
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofMillis(pollingInMillis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Wait for element to be visible
     * @param locator Element locator
     * @return WebElement when visible
     */
    public static WebElement waitForElementVisible(By locator) {
        logger.debug("Waiting for element to be visible: " + locator);
        try {
            WebElement element = getWebDriverWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.debug("Element is now visible: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not visible within timeout: " + locator);
            throw new RuntimeException("Element not visible: " + locator, e);
        }
    }

    /**
     * Wait for element to be visible with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Custom timeout
     * @return WebElement when visible
     */
    public static WebElement waitForElementVisible(By locator, int timeoutInSeconds) {
        logger.debug("Waiting for element to be visible (timeout: " + timeoutInSeconds + "s): " + locator);
        try {
            WebElement element = getWebDriverWait(timeoutInSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.debug("Element is now visible: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not visible within " + timeoutInSeconds + " seconds: " + locator);
            throw new RuntimeException("Element not visible: " + locator, e);
        }
    }

    /**
     * Wait for element to be clickable
     * @param locator Element locator
     * @return WebElement when clickable
     */
    public static WebElement waitForElementClickable(By locator) {
        logger.debug("Waiting for element to be clickable: " + locator);
        try {
            WebElement element = getWebDriverWait().until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element is now clickable: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not clickable within timeout: " + locator);
            throw new RuntimeException("Element not clickable: " + locator, e);
        }
    }

    /**
     * Wait for element to be clickable with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Custom timeout
     * @return WebElement when clickable
     */
    public static WebElement waitForElementClickable(By locator, int timeoutInSeconds) {
        logger.debug("Waiting for element to be clickable (timeout: " + timeoutInSeconds + "s): " + locator);
        try {
            WebElement element = getWebDriverWait(timeoutInSeconds).until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element is now clickable: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not clickable within " + timeoutInSeconds + " seconds: " + locator);
            throw new RuntimeException("Element not clickable: " + locator, e);
        }
    }

    /**
     * Wait for element to be present in DOM
     * @param locator Element locator
     * @return WebElement when present
     */
    public static WebElement waitForElementPresent(By locator) {
        logger.debug("Waiting for element to be present: " + locator);
        try {
            WebElement element = getWebDriverWait().until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element is now present: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not present within timeout: " + locator);
            throw new RuntimeException("Element not present: " + locator, e);
        }
    }

    /**
     * Wait for element to be present with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Custom timeout
     * @return WebElement when present
     */
    public static WebElement waitForElementPresent(By locator, int timeoutInSeconds) {
        logger.debug("Waiting for element to be present (timeout: " + timeoutInSeconds + "s): " + locator);
        try {
            WebElement element = getWebDriverWait(timeoutInSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element is now present: " + locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element not present within " + timeoutInSeconds + " seconds: " + locator);
            throw new RuntimeException("Element not present: " + locator, e);
        }
    }

    /**
     * Wait for all elements to be visible
     * @param locator Element locator
     * @return List of WebElements when all are visible
     */
    public static List<WebElement> waitForAllElementsVisible(By locator) {
        logger.debug("Waiting for all elements to be visible: " + locator);
        try {
            List<WebElement> elements = getWebDriverWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
            logger.debug("All elements are now visible: " + locator + " (count: " + elements.size() + ")");
            return elements;
        } catch (TimeoutException e) {
            logger.error("Not all elements visible within timeout: " + locator);
            throw new RuntimeException("Not all elements visible: " + locator, e);
        }
    }

    /**
     * Wait for element to disappear
     * @param locator Element locator
     * @return true when element is no longer visible
     */
    public static boolean waitForElementToDisappear(By locator) {
        logger.debug("Waiting for element to disappear: " + locator);
        try {
            boolean disappeared = getWebDriverWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element has disappeared: " + locator);
            return disappeared;
        } catch (TimeoutException e) {
            logger.error("Element still visible after timeout: " + locator);
            throw new RuntimeException("Element still visible: " + locator, e);
        }
    }

    /**
     * Wait for element to disappear with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Custom timeout
     * @return true when element is no longer visible
     */
    public static boolean waitForElementToDisappear(By locator, int timeoutInSeconds) {
        logger.debug("Waiting for element to disappear (timeout: " + timeoutInSeconds + "s): " + locator);
        try {
            boolean disappeared = getWebDriverWait(timeoutInSeconds).until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element has disappeared: " + locator);
            return disappeared;
        } catch (TimeoutException e) {
            logger.error("Element still visible after " + timeoutInSeconds + " seconds: " + locator);
            throw new RuntimeException("Element still visible: " + locator, e);
        }
    }

    /**
     * Wait for text to be present in element
     * @param locator Element locator
     * @param text Expected text
     * @return true when text is present
     */
    public static boolean waitForTextToBePresentInElement(By locator, String text) {
        logger.debug("Waiting for text '" + text + "' to be present in element: " + locator);
        try {
            boolean textPresent = getWebDriverWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            logger.debug("Text '" + text + "' is now present in element: " + locator);
            return textPresent;
        } catch (TimeoutException e) {
            logger.error("Text '" + text + "' not present in element within timeout: " + locator);
            throw new RuntimeException("Text not present in element: " + locator, e);
        }
    }

    /**
     * Wait for text to be present in element value (input fields)
     * @param locator Element locator
     * @param text Expected text in value attribute
     * @return true when text is present in value
     */
    public static boolean waitForTextToBePresentInElementValue(By locator, String text) {
        logger.debug("Waiting for text '" + text + "' to be present in element value: " + locator);
        try {
            boolean textPresent = getWebDriverWait().until(ExpectedConditions.textToBePresentInElementValue(locator, text));
            logger.debug("Text '" + text + "' is now present in element value: " + locator);
            return textPresent;
        } catch (TimeoutException e) {
            logger.error("Text '" + text + "' not present in element value within timeout: " + locator);
            throw new RuntimeException("Text not present in element value: " + locator, e);
        }
    }

    /**
     * Wait for URL to contain specific text
     * @param urlFragment URL fragment to wait for
     * @return true when URL contains the fragment
     */
    public static boolean waitForUrlToContain(String urlFragment) {
        logger.debug("Waiting for URL to contain: " + urlFragment);
        try {
            boolean urlContains = getWebDriverWait().until(ExpectedConditions.urlContains(urlFragment));
            logger.debug("URL now contains: " + urlFragment);
            return urlContains;
        } catch (TimeoutException e) {
            logger.error("URL does not contain '" + urlFragment + "' within timeout");
            throw new RuntimeException("URL does not contain: " + urlFragment, e);
        }
    }

    /**
     * Wait for URL to be exactly as expected
     * @param expectedUrl Expected URL
     * @return true when URL matches
     */
    public static boolean waitForUrlToBe(String expectedUrl) {
        logger.debug("Waiting for URL to be: " + expectedUrl);
        try {
            boolean urlMatches = getWebDriverWait().until(ExpectedConditions.urlToBe(expectedUrl));
            logger.debug("URL is now: " + expectedUrl);
            return urlMatches;
        } catch (TimeoutException e) {
            logger.error("URL is not '" + expectedUrl + "' within timeout");
            throw new RuntimeException("URL does not match: " + expectedUrl, e);
        }
    }

    /**
     * Wait for page title to contain specific text
     * @param titleFragment Title fragment to wait for
     * @return true when title contains the fragment
     */
    public static boolean waitForTitleToContain(String titleFragment) {
        logger.debug("Waiting for title to contain: " + titleFragment);
        try {
            boolean titleContains = getWebDriverWait().until(ExpectedConditions.titleContains(titleFragment));
            logger.debug("Title now contains: " + titleFragment);
            return titleContains;
        } catch (TimeoutException e) {
            logger.error("Title does not contain '" + titleFragment + "' within timeout");
            throw new RuntimeException("Title does not contain: " + titleFragment, e);
        }
    }

    /**
     * Wait for page title to be exactly as expected
     * @param expectedTitle Expected title
     * @return true when title matches
     */
    public static boolean waitForTitleToBe(String expectedTitle) {
        logger.debug("Waiting for title to be: " + expectedTitle);
        try {
            boolean titleMatches = getWebDriverWait().until(ExpectedConditions.titleIs(expectedTitle));
            logger.debug("Title is now: " + expectedTitle);
            return titleMatches;
        } catch (TimeoutException e) {
            logger.error("Title is not '" + expectedTitle + "' within timeout");
            throw new RuntimeException("Title does not match: " + expectedTitle, e);
        }
    }

    /**
     * Wait for alert to be present
     * @return Alert object when present
     */
    public static Alert waitForAlert() {
        logger.debug("Waiting for alert to be present");
        try {
            Alert alert = getWebDriverWait().until(ExpectedConditions.alertIsPresent());
            logger.debug("Alert is now present");
            return alert;
        } catch (TimeoutException e) {
            logger.error("Alert not present within timeout");
            throw new RuntimeException("Alert not present", e);
        }
    }

    /**
     * Wait for alert with custom timeout
     * @param timeoutInSeconds Custom timeout
     * @return Alert object when present
     */
    public static Alert waitForAlert(int timeoutInSeconds) {
        logger.debug("Waiting for alert to be present (timeout: " + timeoutInSeconds + "s)");
        try {
            Alert alert = getWebDriverWait(timeoutInSeconds).until(ExpectedConditions.alertIsPresent());
            logger.debug("Alert is now present");
            return alert;
        } catch (TimeoutException e) {
            logger.error("Alert not present within " + timeoutInSeconds + " seconds");
            throw new RuntimeException("Alert not present", e);
        }
    }

    /**
     * Wait for number of windows to be a specific count
     * @param numberOfWindows Expected number of windows
     * @return true when condition is met
     */
    public static boolean waitForNumberOfWindows(int numberOfWindows) {
        logger.debug("Waiting for number of windows to be: " + numberOfWindows);
        try {
            boolean windowCountMet = getWebDriverWait().until(ExpectedConditions.numberOfWindowsToBe(numberOfWindows));
            logger.debug("Number of windows is now: " + numberOfWindows);
            return windowCountMet;
        } catch (TimeoutException e) {
            logger.error("Number of windows not " + numberOfWindows + " within timeout");
            throw new RuntimeException("Window count not met: " + numberOfWindows, e);
        }
    }

    /**
     * Wait for element attribute to contain specific value
     * @param locator Element locator
     * @param attribute Attribute name
     * @param value Expected attribute value
     * @return true when attribute contains the value
     */
    public static boolean waitForAttributeToContain(By locator, String attribute, String value) {
        logger.debug("Waiting for attribute '" + attribute + "' to contain '" + value + "' in element: " + locator);
        try {
            boolean attributeContains = getWebDriverWait().until(ExpectedConditions.attributeContains(locator, attribute, value));
            logger.debug("Attribute '" + attribute + "' now contains '" + value + "'");
            return attributeContains;
        } catch (TimeoutException e) {
            logger.error("Attribute '" + attribute + "' does not contain '" + value + "' within timeout");
            throw new RuntimeException("Attribute condition not met", e);
        }
    }

    /**
     * Wait for element to be selected (checkboxes, radio buttons)
     * @param locator Element locator
     * @return true when element is selected
     */
    public static boolean waitForElementToBeSelected(By locator) {
        logger.debug("Waiting for element to be selected: " + locator);
        try {
            boolean selected = getWebDriverWait().until(ExpectedConditions.elementToBeSelected(locator));
            logger.debug("Element is now selected: " + locator);
            return selected;
        } catch (TimeoutException e) {
            logger.error("Element not selected within timeout: " + locator);
            throw new RuntimeException("Element not selected: " + locator, e);
        }
    }

    /**
     * Wait for custom condition using FluentWait
     * @param condition Custom expected condition
     * @param timeoutInSeconds Timeout in seconds
     * @param pollingInMillis Polling interval in milliseconds
     * @return Result of the condition
     */
    public static <T> T waitForCustomCondition(Function<WebDriver, T> condition, int timeoutInSeconds, int pollingInMillis) {
        logger.debug("Waiting for custom condition");
        try {
            T result = getFluentWait(timeoutInSeconds, pollingInMillis).until(condition);
            logger.debug("Custom condition met");
            return result;
        } catch (TimeoutException e) {
            logger.error("Custom condition not met within timeout");
            throw new RuntimeException("Custom condition not met", e);
        }
    }

    /**
     * Wait with simple sleep (use sparingly)
     * @param milliseconds Time to wait in milliseconds
     */
    public static void sleep(int milliseconds) {
        try {
            logger.debug("Sleeping for " + milliseconds + " milliseconds");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.warn("Sleep interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if element is present without waiting
     * @param locator Element locator
     * @return true if element is present, false otherwise
     */
    public static boolean isElementPresent(By locator) {
        try {
            DriverManager.getDriver().findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Check if element is visible without waiting
     * @param locator Element locator
     * @return true if element is visible, false otherwise
     */
    public static boolean isElementVisible(By locator) {
        try {
            WebElement element = DriverManager.getDriver().findElement(locator);
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Check if element is clickable without waiting
     * @param locator Element locator
     * @return true if element is clickable, false otherwise
     */
    public static boolean isElementClickable(By locator) {
        try {
            WebElement element = DriverManager.getDriver().findElement(locator);
            return element.isEnabled() && element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Wait for page to load completely (using JavaScript)
     * @return true when page is loaded
     */
    public static boolean waitForPageToLoad() {
        logger.debug("Waiting for page to load completely");
        try {
            boolean pageLoaded = getWebDriverWait().until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String readyState = js.executeScript("return document.readyState").toString();
                return "complete".equals(readyState);
            });
            logger.debug("Page has loaded completely");
            return pageLoaded;
        } catch (TimeoutException e) {
            logger.error("Page did not load completely within timeout");
            throw new RuntimeException("Page load timeout", e);
        }
    }

    /**
     * Wait for Ajax/jQuery to complete (if jQuery is present)
     * @return true when Ajax requests are complete
     */
    public static boolean waitForAjaxToComplete() {
        logger.debug("Waiting for Ajax requests to complete");
        try {
            boolean ajaxComplete = getWebDriverWait().until(driver -> {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                try {
                    Object result = js.executeScript("return jQuery.active == 0");
                    return Boolean.TRUE.equals(result);
                } catch (Exception e) {
                    // jQuery not present, assume Ajax is complete
                    return true;
                }
            });
            logger.debug("Ajax requests completed");
            return ajaxComplete;
        } catch (TimeoutException e) {
            logger.error("Ajax requests did not complete within timeout");
            throw new RuntimeException("Ajax timeout", e);
        }
    }
}