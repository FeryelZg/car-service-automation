package com.carservice.automation.base;

import com.carservice.automation.utils.ConfigReader;
import com.carservice.automation.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base Page class containing common functionality for all page objects
 */
public abstract class BasePage {

    private static final Logger logger = LogManager.getLogger(BasePage.class);

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final ConfigReader configReader;
    protected final JavascriptExecutor jsExecutor;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.configReader = new ConfigReader();
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    /**
     * Find element with explicit wait
     * @param xpath XPath locator
     * @return WebElement or null if not found
     */
    protected WebElement findElementWithWait(String xpath) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            logger.debug("Element not found with xpath: {}", xpath);
            return null;
        }
    }

    /**
     * Find clickable element with explicit wait
     * @param xpath XPath locator
     * @return WebElement or null if not found
     */
    protected WebElement findClickableElementWithWait(String xpath) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        } catch (Exception e) {
            logger.debug("Clickable element not found with xpath: {}", xpath);
            return null;
        }
    }

    /**
     * Find element with multiple selectors
     * @param selectors Array of XPath selectors
     * @param elementName Name of element for logging
     * @return WebElement
     * @throws RuntimeException if element not found with any selector
     */
    protected WebElement findElementWithMultipleSelectors(String[] selectors, String elementName) {
        for (String selector : selectors) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                logger.info("Found {} with selector: {}", elementName, selector);
                return element;
            } catch (Exception e) {
                logger.debug("{} not found with selector: {}", elementName, selector);
            }
        }
        throw new RuntimeException("Could not find " + elementName + " with any selector");
    }

    /**
     * Click element with scroll and retry mechanism
     * @param element WebElement to click
     * @param elementName Name for logging
     */
    protected void clickElement(WebElement element, String elementName) {
        try {
            scrollToElement(element);
            waitForElement(500);

            try {
                element.click();
                logger.info("Clicked {} successfully", elementName);
            } catch (Exception e) {
                jsExecutor.executeScript("arguments[0].click();", element);
                logger.info("Clicked {} using JavaScript", elementName);
            }
        } catch (Exception e) {
            logger.error("Failed to click {}: {}", elementName, e.getMessage());
            throw new RuntimeException("Could not click " + elementName, e);
        }
    }

    /**
     * Scroll to element using JavaScript
     * @param element WebElement to scroll to
     */
    protected void scrollToElement(WebElement element) {
        jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    /**
     * Scroll page by specified pixels
     * @param pixels Number of pixels to scroll
     */
    protected void scrollPage(int pixels) {
        jsExecutor.executeScript("window.scrollBy(0, " + pixels + ");");
        waitForElement(500);
    }

    /**
     * Clear input field properly with events
     * @param inputElement WebElement input field
     */
    protected void clearInput(WebElement inputElement) {
        try {
            jsExecutor.executeScript(
                    "arguments[0].value = '';" +
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    inputElement
            );

            inputElement.clear();
            inputElement.sendKeys("");
            waitForElement(100);

        } catch (Exception e) {
            logger.warn("Failed to clear input properly, using standard clear(): {}", e.getMessage());
            inputElement.clear();
        }
    }

    /**
     * Wait for specified duration
     * @param milliseconds Duration to wait in milliseconds
     */
    protected void waitForElement(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait interrupted: {}", e.getMessage());
        }
    }

    /**
     * Check if element is present
     * @param xpath XPath locator
     * @return true if element is present, false otherwise
     */
    protected boolean isElementPresent(String xpath) {
        try {
            driver.findElement(By.xpath(xpath));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if element is displayed
     * @param xpath XPath locator
     * @return true if element is displayed, false otherwise
     */
    protected boolean isElementDisplayed(String xpath) {
        try {
            WebElement element = driver.findElement(By.xpath(xpath));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Take screenshot with specified name
     * @param screenshotName Name for the screenshot
     */
    protected void takeScreenshot(String screenshotName) {
        try {
            ScreenshotUtils.takeScreenshot(screenshotName);
            logger.info("Screenshot taken: {}", screenshotName);
        } catch (Exception e) {
            logger.warn("Failed to take screenshot '{}': {}", screenshotName, e.getMessage());
        }
    }

    /**
     * Get text from element
     * @param xpath XPath locator
     * @return Element text or empty string if not found
     */
    protected String getElementText(String xpath) {
        try {
            WebElement element = findElementWithWait(xpath);
            return element != null ? element.getText() : "";
        } catch (Exception e) {
            logger.warn("Could not get text from element {}: {}", xpath, e.getMessage());
            return "";
        }
    }

    /**
     * Get attribute value from element
     * @param xpath XPath locator
     * @param attributeName Attribute name
     * @return Attribute value or empty string if not found
     */
    protected String getElementAttribute(String xpath, String attributeName) {
        try {
            WebElement element = findElementWithWait(xpath);
            return element != null ? element.getAttribute(attributeName) : "";
        } catch (Exception e) {
            logger.warn("Could not get attribute '{}' from element {}: {}", attributeName, xpath, e.getMessage());
            return "";
        }
    }

    /**
     * Wait until element is clickable
     * @param xpath XPath locator
     * @return WebElement when clickable
     */
    protected WebElement waitForElementToBeClickable(String xpath) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    /**
     * Wait until element is visible
     * @param xpath XPath locator
     * @return WebElement when visible
     */
    protected WebElement waitForElementToBeVisible(String xpath) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }
}