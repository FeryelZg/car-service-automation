package com.carservice.automation.pages.enduser;

import com.carservice.automation.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object class for Repairer Selection functionality
 */
public class RepairerSelectionPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(RepairerSelectionPage.class);

    // Locators
    private static final String INFO_BUTTON_XPATH = "//div[contains(@class, 'info-button')]//p[(contains(text(), '+ info') or contains(text(), '+ infos'))]/parent::div";
    private static final String INFO_PANEL_XPATH = "//div[contains(@class, 'container-detail-agency')]";
    private static final String CLOSE_INFO_BUTTON_XPATH = "//img[contains(@src, 'icon-close-info.png')]";
    private static final String SELECT_REPAIRER_BUTTON_XPATH = "//p[contains(@class, 'check-agency-title') and (contains(text(), 'Select this repairer') or contains(text(), 'Sélectionner ce réparateur'))]/ancestor::div[contains(@class, 'check-agency')]";
    private static final String NEXT_BUTTON_XPATH = "//button[contains(@class, 'ot-button-primary') and (contains(., 'Next') or contains(., 'Suivant'))]";

    // Date and time selection locators
    private static final String DATE_SELECTOR_BASE = "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='";
    private static final String AGENCY_CLOSED_XPATH = "//p[contains(text(), 'Agency closed')]";
    private static final String TIME_SLOT_XPATH = "//div[contains(@class, 'hour') and not(contains(@class, 'disabled'))]//p[contains(@class, 'hour-title')]";

    public RepairerSelectionPage(WebDriver driver) {
        super(driver);
    }

    /**
     * View repairer information
     */
    public void viewRepairerInfo() {
        logger.info("Viewing repairer information");

        WebElement infoButton = findElementWithWait(INFO_BUTTON_XPATH);
        clickElement(infoButton, "+ info button");
        waitForElement(1000);

        // Verify info panel is displayed
        WebElement infoPanel = findElementWithWait(INFO_PANEL_XPATH);
        if (infoPanel == null) {
            throw new RuntimeException("Repairer info panel should be displayed");
        }

        // Scroll within the info panel
        jsExecutor.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", infoPanel);
        waitForElement(1000);

        logger.info("Repairer information displayed successfully");
    }

    /**
     * Close repairer information panel
     */
    public void closeRepairerInfo() {
        logger.info("Closing repairer information panel");

        WebElement closeButton = findElementWithWait(CLOSE_INFO_BUTTON_XPATH);
        clickElement(closeButton, "Close info button");
        waitForElement(500);

        logger.info("Repairer information panel closed successfully");
    }

    /**
     * Select repairer
     */
    public void selectRepairer() {
        logger.info("Selecting repairer");

        waitForElement(2000);

        WebElement selectRepairerButton = findElementWithWait(SELECT_REPAIRER_BUTTON_XPATH);
        clickElement(selectRepairerButton, "Select this repairer button");
        waitForElement(1000);

        logger.info("Repairer selected successfully");
    }

    /**
     * Click the Next button
     */
    public void clickNextButton() {
        logger.info("Clicking Next button");
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);
        clickElement(nextButton, "Next button after repairer selection");
    }

    /**
     * Select date and time for appointment
     */
    public void selectDateAndTime() {
        logger.info("Selecting date and time");

        waitForElement(2000);
        int maxAttempts = 1;
        boolean timeSlotFound = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("Attempt {} to find available date and time", attempt);

            selectFutureDate(attempt);
            waitForElement(1500);

            //COMMENTED TO FIX A TIME FOR DEMO TO BE REMOVED AFTER DEMO
//            if (isAgencyClosed()) {
//                logger.info("Agency closed for selected date, trying another date...");
//                continue;
//            }

            if (selectAvailableTimeSlot()) {
                timeSlotFound = true;
                break;
            }
        }

        if (!timeSlotFound) {
            throw new RuntimeException("Could not find any available time slots after " + maxAttempts + " attempts");
        }

        logger.info("Date and time selected successfully");
    }

    /**
     * Check if agency is closed for selected date
     * @return true if agency is closed, false otherwise
     */
    private boolean isAgencyClosed() {
        try {
            String[] closedSelectors = {
                    "//p[contains(text(), 'Agency closed')]",
                    "//img[contains(@src, 'calendar-Not-Available.png')]",
                    "//div[contains(text(), 'Agency closed')]"
            };

            for (String selector : closedSelectors) {
                WebElement closedElement = findElementWithWait(selector);
                if (closedElement != null && closedElement.isDisplayed()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Select a future date from calendar
     * @param attempt Attempt number to select different dates
     */
    private void selectFutureDate(int attempt) {
        logger.info("Selecting future date from calendar (attempt {})", attempt);

        // Try specific dates first
        int[] preferredDates = {8};
        int dateIndex = Math.min(attempt - 1, preferredDates.length - 1);
        int targetDate = preferredDates[dateIndex];

        String selector = DATE_SELECTOR_BASE + targetDate + "']";

        try {
            WebElement dateElement = waitForElementToBeClickable(selector);
            clickElement(dateElement, "Future date (day " + targetDate + ")");
            waitForElement(1000);
            logger.info("Future date selected from calendar");
        } catch (Exception e) {
            logger.warn("Specific date not found, trying any available date");
            String fallbackSelector = "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[contains(@class, 'btn-light') and not(contains(@class, 'text-muted'))][position()=" + attempt + "]";
            WebElement dateElement = waitForElementToBeClickable(fallbackSelector);
            clickElement(dateElement, "Future date (fallback)");
            waitForElement(1000);
        }
    }

    /**
     * Select an available time slot
     * @return true if time slot selected, false if none available
     */
    private boolean selectAvailableTimeSlot() {
        logger.info("Selecting available time slot");

        try {
            String[] timeSelectors = {
                    "//div[contains(@class, 'hour') and not(contains(@class, 'disabled'))]//p[contains(@class, 'hour-title')]",
                    "//div[contains(@class, 'container-hours')]//div[contains(@class, 'hour')]//p[contains(text(), ':')]",
                    "//p[contains(@class, 'hour-title') and contains(text(), ':')]"
            };

            WebElement timeSlot = null;
            for (String selector : timeSelectors) {
                try {
                    timeSlot = waitForElementToBeClickable(selector);
                    if (timeSlot != null) break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (timeSlot != null) {
                clickElement(timeSlot, "Available time slot");
                waitForElement(1000);
                logger.info("Available time slot selected");
                return true;
            } else {
                logger.info("No time slots available for this date");
                return false;
            }

        } catch (Exception e) {
            logger.warn("Could not select time slot: {}", e.getMessage());
            return false;
        }
    }
}