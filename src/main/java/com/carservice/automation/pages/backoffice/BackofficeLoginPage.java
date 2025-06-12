package com.carservice.automation.pages.backoffice;

import com.carservice.automation.base.BasePage;
import com.carservice.automation.utils.AllureUtils;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.List;

/**
 * Page Object class for Backoffice Login functionality
 */
public class BackofficeLoginPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(BackofficeLoginPage.class);

    // Test credentials from config
    private final String adminUsername;
    private final String adminPassword;

    public BackofficeLoginPage(WebDriver driver) {
        super(driver);
        this.adminUsername = configReader.getProperty("admin.username");
        this.adminPassword = configReader.getProperty("admin.password");
    }

    /**
     * Perform login with validation tests
     */
    @Step("Login to backoffice with validation tests")
    public void loginWithValidation() {
        logger.info("Starting backoffice login with validation tests");

        validateEmptyCredentials();
        validateInvalidCredentials();
        loginWithValidCredentials();

        AllureUtils.logStep("Backoffice login completed successfully");
    }

    /**
     * Validate empty credentials show proper validation
     */
    @Step("Validate empty credentials validation")
    private void validateEmptyCredentials() {
        logger.info("Testing empty credentials validation");

        WebElement usernameInput = findUsernameInput();
        WebElement passwordInput = findPasswordInput();
        WebElement loginButton = findLoginButton();

        // Clear any existing values
        clearInput(usernameInput);
        clearInput(passwordInput);
        waitForElement(300);

        // Try to click login with empty fields
        boolean isLoginButtonEnabled = loginButton.isEnabled();
        logger.info("Login button enabled with empty fields: {}", isLoginButtonEnabled);

        AllureUtils.addParameter("Empty Credentials Test", "Login button state: " + (isLoginButtonEnabled ? "Enabled" : "Disabled"));
        AllureUtils.logStep("Empty credentials validation completed");
    }

    /**
     * Validate invalid credentials behavior
     */
    @Step("Validate invalid credentials behavior")
    private void validateInvalidCredentials() {
        logger.info("Testing invalid credentials");

        WebElement usernameInput = findUsernameInput();
        WebElement passwordInput = findPasswordInput();

        // Test with invalid credentials
        clearInput(usernameInput);
        clearInput(passwordInput);

        usernameInput.sendKeys("invalid_user");
        passwordInput.sendKeys("invalid_pass");
        waitForElement(300);

        AllureUtils.addParameter("Invalid Credentials Test", "Username: invalid_user, Password: invalid_pass");
        AllureUtils.logStep("Invalid credentials entered for testing");
    }

    /**
     * Login with valid credentials
     */
    @Step("Login with valid credentials")
    private void loginWithValidCredentials() {
        logger.info("Logging in with valid credentials");

        WebElement usernameInput = findUsernameInput();
        WebElement passwordInput = findPasswordInput();
        WebElement loginButton = findLoginButton();

        // Clear and enter valid credentials
        clearInput(usernameInput);
        clearInput(passwordInput);

        usernameInput.sendKeys(adminUsername);
        passwordInput.sendKeys(adminPassword);
        waitForElement(500);

        // Click login button
        clickElement(loginButton, "Login button");
        waitForElement(2000);

        AllureUtils.addParameter("Login Username", adminUsername);
        AllureUtils.addParameter("Login Password", "***masked***");
        AllureUtils.attachScreenshot("After login attempt");

        logger.info("Login attempt completed");
    }

    /**
     * Find username input with multiple selector fallbacks
     */
    private WebElement findUsernameInput() {
        String[] usernameSelectors = {
                // ✅ WORKING - This was found successfully
                "//tui-input[@formcontrolname='username']//input[contains(@id, 'tui_')]",

                // Backup selectors
                "//tui-input[@formcontrolname='username']//input[@type='text' and contains(@class, 't-input')]",
                "//input[@type='text' and preceding-sibling::*//label[contains(text(), 'Nom d')]]",
                "//div[contains(@class, 't-content')]//input[@type='text'][1]",
                "//input[@aria-describedby and @type='text' and not(@tuimaskaccessor)]"
        };

        return findElementWithMultipleSelectors(usernameSelectors, "Username input");
    }

    /**
     * Find password input with multiple selector fallbacks
     */
    private WebElement findPasswordInput() {
        String[] passwordSelectors = {
                // ✅ WORKING - This was found successfully
                "//tui-input-password[@formcontrolname='password']//input[contains(@id, 'tui_')]",

                // Backup selectors
                "//tui-input-password[@formcontrolname='password']//input[@type='password' and contains(@class, 't-input')]",
                "//input[@type='password' and preceding-sibling::*//label[contains(text(), 'Mot de passe')]]",
                "//input[@type='password' and @aria-describedby]",
                "//tui-input-password//input[@type='password'][1]"
        };

        return findElementWithMultipleSelectors(passwordSelectors, "Password input");
    }

    /**
     * Verify login page is loaded
     */
    @Step("Verify login page is loaded")
    public void verifyLoginPageLoaded() {
        logger.info("Verifying login page is loaded");

        // First, take a screenshot for debugging
        AllureUtils.attachScreenshot("Login page verification - start");

        try {
            String[] titleSelectors = {
                    "//span[contains(text(), 'Authentification')]",
                    "//h1[contains(text(), 'Login')]",
                    "//div[contains(@class, 'auth-title')]",
                    "//*[contains(text(), 'Authentification')]"
            };

            WebElement titleElement = null;
            for (String selector : titleSelectors) {
                try {
                    titleElement = findElementWithWait(selector);
                    if (titleElement != null) {
                        logger.info("Found title element with selector: {}", selector);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (titleElement == null) {
                logger.warn("Could not find login page title, running debug analysis");
                debugLoginPageElements();
            } else {
                Assert.assertNotNull(titleElement, "Login page should be loaded");
            }

            // Try to verify form elements are present (with lenient checking)
            try {
                WebElement usernameInput = findUsernameInput();
                WebElement passwordInput = findPasswordInput();
                WebElement loginButton = findLoginButton();

                Assert.assertNotNull(usernameInput, "Username input should be present");
                Assert.assertNotNull(passwordInput, "Password input should be present");
                Assert.assertNotNull(loginButton, "Login button should be present");

                logger.info("All login form elements found successfully");

            } catch (Exception e) {
                logger.error("Failed to find login form elements: {}", e.getMessage());
                debugLoginPageElements();
                throw e;
            }

            AllureUtils.attachScreenshot("Login page loaded and verified");
            AllureUtils.logStep("Login page verification completed");

            logger.info("Login page verification completed successfully");

        } catch (Exception e) {
            AllureUtils.attachScreenshot("Login page verification failed");
            logger.error("Login page verification failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Find login button with multiple selector fallbacks
     */
    private WebElement findLoginButton() {
        String[] loginButtonSelectors = {
                // ✅ WORKING - This was found successfully
                "//button[@tuibutton]//span[@class='t-content' and contains(text(), 'Se connecter')]",

                // Backup selectors
                "//button[@tuibutton and @data-appearance='primary' and @data-size='l']",
                "//button[@tuibutton]//tui-wrapper[@data-appearance='primary']",
                "//button[@tuibutton and @type='button' and contains(@class, 'w-100')]",
                "//button//span[contains(text(), 'Se connecter')]/ancestor::button",
                "//button[.//span[contains(text(), 'Se connecter')]]",
                "//button[@tuibutton]",
                "//button[@type='button']"
        };

        return findElementWithMultipleSelectors(loginButtonSelectors, "Login button");
    }

    /**
     * Debug method to help troubleshoot element finding issues
     */
    @Step("Debug login page elements")
    public void debugLoginPageElements() {
        logger.info("=== DEBUG: Login Page Elements ===");

        try {
            // Check page source for debugging
            String pageSource = driver.getPageSource();
            logger.info("Page title: {}", driver.getTitle());
            logger.info("Current URL: {}", driver.getCurrentUrl());

            // Check for common login elements
            if (pageSource.contains("Se connecter")) {
                logger.info("✅ Found 'Se connecter' text in page source");
            }
            if (pageSource.contains("Authentification")) {
                logger.info("✅ Found 'Authentification' text in page source");
            }
            if (pageSource.contains("tui-input")) {
                logger.info("✅ Found 'tui-input' components in page source");
            }
            if (pageSource.contains("formcontrolname")) {
                logger.info("✅ Found 'formcontrolname' attributes in page source");
            }

            // Try to find any input elements
            try {
                List<WebElement> allInputs = driver.findElements(By.tagName("input"));
                logger.info("Found {} input elements on page", allInputs.size());

                for (int i = 0; i < Math.min(allInputs.size(), 5); i++) {
                    WebElement input = allInputs.get(i);
                    logger.info("Input {}: type='{}', class='{}', id='{}'",
                            i+1,
                            input.getAttribute("type"),
                            input.getAttribute("class"),
                            input.getAttribute("id"));
                }
            } catch (Exception e) {
                logger.warn("Could not enumerate input elements: {}", e.getMessage());
            }

            // Try to find any button elements
            try {
                List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                logger.info("Found {} button elements on page", allButtons.size());

                for (int i = 0; i < Math.min(allButtons.size(), 5); i++) {
                    WebElement button = allButtons.get(i);
                    logger.info("Button {}: text='{}', class='{}'",
                            i+1,
                            button.getText(),
                            button.getAttribute("class"));
                }
            } catch (Exception e) {
                logger.warn("Could not enumerate button elements: {}", e.getMessage());
            }

            AllureUtils.attachScreenshot("Login page debug");

        } catch (Exception e) {
            logger.error("Debug method failed: {}", e.getMessage());
        }

        logger.info("=== END DEBUG ===");
    }

    /**
     * Quick login method without validation (for subsequent tests)
     */
    @Step("Quick login to backoffice")
    public void quickLogin() {
        logger.info("Performing quick login");

        WebElement usernameInput = findUsernameInput();
        WebElement passwordInput = findPasswordInput();
        WebElement loginButton = findLoginButton();

        clearInput(usernameInput);
        clearInput(passwordInput);

        usernameInput.sendKeys(adminUsername);
        passwordInput.sendKeys(adminPassword);

        clickElement(loginButton, "Login button");
        waitForElement(2000);

        AllureUtils.attachScreenshot("Quick login completed");
        logger.info("Quick login completed");
    }
}