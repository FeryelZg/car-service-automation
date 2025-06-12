package com.carservice.automation.pages.backoffice;

import com.carservice.automation.base.BasePage;
import com.carservice.automation.utils.AllureUtils;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Page Object class for Workspace Selection functionality
 */
public class WorkspaceSelectionPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(WorkspaceSelectionPage.class);

    // Locators
    private static final String WORKSPACE_TITLE_XPATH = "//span[contains(text(), 'Espace de travail')]";
    private static final String WORKSPACE_DROPDOWN_XPATH = "//tui-select[@tuitextfieldsize='m']//input[@readonly]";
    private static final String HAVAL_OPTION_XPATH = "//div[contains(text(), 'HAVAL')]";
    private static final String START_BUTTON_XPATH = "//button[contains(., 'Commencer')]";
    private static final String LOGOUT_BUTTON_XPATH = "//button[contains(., 'Déconnexion')]";

    // Alternative selectors for dropdown
    private static final String DROPDOWN_WRAPPER_XPATH = "//tui-select[@formcontrolname and @tuitextfieldsize='m']";
    private static final String DROPDOWN_INPUT_XPATH = "//input[contains(@id, 'tui_interactive_') and @readonly]";

    public WorkspaceSelectionPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verify workspace selection page is loaded
     */
    @Step("Verify workspace selection page is loaded")
    public void verifyWorkspacePageLoaded() {
        logger.info("Verifying workspace selection page is loaded");

        WebElement titleElement = findElementWithWait(WORKSPACE_TITLE_XPATH);
        Assert.assertNotNull(titleElement, "Workspace page title should be present");

        WebElement dropdown = findWorkspaceDropdown();
        Assert.assertNotNull(dropdown, "Workspace dropdown should be present");

        WebElement startButton = findElementWithWait(START_BUTTON_XPATH);
        Assert.assertNotNull(startButton, "Start button should be present");

        // Verify start button is initially disabled
        Assert.assertFalse(startButton.isEnabled(), "Start button should be disabled initially");

        AllureUtils.attachScreenshot("Workspace selection page loaded");
        AllureUtils.logStep("Workspace selection page verification completed");

        logger.info("Workspace selection page verified successfully");
    }

    /**
     * Select HAVAL workspace
     */
    @Step("Select HAVAL workspace")
    public void selectHavalWorkspace() {
        logger.info("Selecting HAVAL workspace");

        // Click on workspace dropdown
        WebElement dropdown = findWorkspaceDropdown();
        clickElement(dropdown, "Workspace dropdown");
        waitForElement(1000);

        AllureUtils.attachScreenshot("Workspace dropdown opened");

        // Select HAVAL option
        WebElement havalOption = findHavalWorkspaceOption();
        Assert.assertNotNull(havalOption, "HAVAL option should be available");

        clickElement(havalOption, "HAVAL workspace option");
        waitForElement(1000);

        AllureUtils.addParameter("Selected Workspace", "HAVAL");
        AllureUtils.attachScreenshot("HAVAL workspace selected");

        logger.info("HAVAL workspace selected successfully");
    }

    /**
     * Click start button to enter workspace
     */
    @Step("Click start button to enter workspace")
    public void clickStartButton() {
        logger.info("Clicking start button");

        WebElement startButton = findElementWithWait(START_BUTTON_XPATH);
        Assert.assertNotNull(startButton, "Start button should be present");

        // Verify button is now enabled after workspace selection
        Assert.assertTrue(startButton.isEnabled(), "Start button should be enabled after workspace selection");

        clickElement(startButton, "Start button");
        waitForElement(3000);

        AllureUtils.attachScreenshot("Start button clicked - entering workspace");
        AllureUtils.logStep("Successfully entered HAVAL workspace");

        logger.info("Start button clicked successfully");
    }

    /**
     * Complete workspace selection flow
     */
    @Step("Complete workspace selection flow")
    public void completeWorkspaceSelection() {
        logger.info("Starting complete workspace selection flow");

        verifyWorkspacePageLoaded();
        selectHavalWorkspace();
        clickStartButton();

        AllureUtils.logStep("Workspace selection flow completed successfully");
    }

    /**
     * Find workspace dropdown with multiple selector fallbacks
     */
    private WebElement findWorkspaceDropdown() {
        String[] selectors = {
                WORKSPACE_DROPDOWN_XPATH,
                DROPDOWN_INPUT_XPATH,
                "//tui-select//input[@readonly]",
                "//div[contains(@class, 't-wrapper')]//input[@readonly]"
        };

        return findElementWithMultipleSelectors(selectors, "Workspace dropdown");
    }

    /**
     * Verify workspace dashboard is loaded after selection
     */
    @Step("Verify workspace dashboard is loaded")
    public void verifyWorkspaceDashboardLoaded() {
        logger.info("Verifying workspace dashboard is loaded");

        // Check for dashboard elements
        String[] dashboardSelectors = {
                "//div[contains(@class, 'main-side-menu')]",
                "//span[contains(text(), 'HAVAL')]",
                "//nav[contains(@class, 'workspace-services')]"
        };

        boolean dashboardFound = false;
        for (String selector : dashboardSelectors) {
            WebElement element = findElementWithWait(selector);
            if (element != null && element.isDisplayed()) {
                dashboardFound = true;
                break;
            }
        }

        Assert.assertTrue(dashboardFound, "Workspace dashboard should be loaded");

        AllureUtils.attachScreenshot("Workspace dashboard loaded");
        AllureUtils.logStep("Workspace dashboard verification completed");

        logger.info("Workspace dashboard verified successfully");
    }

    /**
     * Logout from workspace (if needed for cleanup)
     */
    @Step("Logout from workspace")
    public void logout() {
        logger.info("Logging out from workspace");

        try {
            WebElement logoutButton = findElementWithWait(LOGOUT_BUTTON_XPATH);
            if (logoutButton != null) {
                clickElement(logoutButton, "Logout button");
                waitForElement(2000);
                AllureUtils.attachScreenshot("Logged out");
                logger.info("Logout completed successfully");
            }
        } catch (Exception e) {
            logger.warn("Logout button not found or not clickable: {}", e.getMessage());
        }
    }
    /**
     * Find login button with multiple selector fallbacks
     */
    private WebElement findHavalWorkspaceOption() {
        String[] havalWorkspaceSelectors = {
                "//tui-select-option[contains(text(), 'HAVAL')]",
                "//*[contains(text(), 'HAVAL')]",
                "//tui-select-option[contains(normalize-space(text()), 'HAVAL')]",
                "//tui-select-option[starts-with(normalize-space(text()), 'HAVAL')]",
                "//tui-select-option[@class='ng-star-inserted' and contains(text(), 'HAVAL')]"
        };

        return findElementWithMultipleSelectors(havalWorkspaceSelectors, "HAVAL workspace option");
    }

}