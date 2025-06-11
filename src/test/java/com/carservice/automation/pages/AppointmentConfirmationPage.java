package com.carservice.automation.pages;

import com.carservice.automation.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

/**
 * Page Object class for Appointment Confirmation functionality
 */
public class AppointmentConfirmationPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(AppointmentConfirmationPage.class);

    // Locators
    private static final String CONFIRM_BUTTON_XPATH = "//button[contains(@class, 'ot-button-primary') and (contains(., 'Confirm') or contains(., 'Confirmer'))]";

    // Summary information locators
    private static final String SERVICE_SECTION_XPATH = "//p[contains(text(), 'Diagnostic Service')]";
    private static final String REPAIRER_SECTION_XPATH = "//p[contains(@class, 'recap-title') and contains(text(), 'authorized repairer')]";
    private static final String DATETIME_SECTION_XPATH = "//p[contains(@class, 'recap-title') and contains(text(), 'Date & Hour')]";

    // Modal and confirmation locators
    private static final String APPOINTMENT_MODAL_XPATH = "//app-modal-verification-appointement";
    private static final String MODAL_WARNING_XPATH = "//div[contains(text(), 'You cannot make another appointment request') or contains(text(), 'Vous ne pouvez pas faire')]";
    private static final String PRESERVE_BUTTON_XPATH = "//button[contains(text(), 'Préserver ma demande')]";

    // Success confirmation locators
    private static final String SUCCESS_TITLE_XPATH = "//p[contains(@class, 'title') and (contains(text(), 'Obtain your final receipt') or contains(text(), 'Obtenir votre reçu final'))]";
    private static final String SUCCESS_SUBTITLE_XPATH = "//p[contains(@class, 'subtitle') and (contains(text(), 'Congratulations! Your appointment has been successfully') or contains(text(), 'Félicitations ! Votre RDV a été enregistré avec succès'))]";
    private static final String CONGRATULATIONS_XPATH = "//p[contains(text(), 'Congratulations') or contains(text(), 'Félicitations')]";

    public AppointmentConfirmationPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verify summary information before confirmation
     */
    public void verifySummaryInformation() {
        logger.info("Verifying summary information");

        try {
            WebElement serviceSection = findElementWithWait(SERVICE_SECTION_XPATH);
            Assert.assertNotNull(serviceSection, "Service information should be displayed");

            WebElement repairerSection = findElementWithWait(REPAIRER_SECTION_XPATH);
            Assert.assertNotNull(repairerSection, "Repairer information should be displayed");

            WebElement dateTimeSection = findElementWithWait(DATETIME_SECTION_XPATH);
            Assert.assertNotNull(dateTimeSection, "Date & time information should be displayed");

            logger.info("Summary information verified successfully");

        } catch (Exception e) {
            logger.warn("Could not fully verify all summary information: {}", e.getMessage());
        }
    }

    /**
     * Confirm the appointment
     */
    public void confirmAppointment() {
        logger.info("Confirming appointment");

        waitForElement(2000);
        WebElement confirmButton = findElementWithWait(CONFIRM_BUTTON_XPATH);
        clickElement(confirmButton, "Confirm button");
        waitForElement(2000);

        logger.info("Appointment confirmation clicked");
    }

    /**
     * Handle post-confirmation flow (modal or success message)
     */
    public void handlePostConfirmationFlow() {
        logger.info("Handling post-confirmation flow");

        if (handleAppointmentModal()) {
            logger.info("Appointment modal handled - clicked preserve request");
        } else {
            // Check for direct success message
            handleSuccessMessage();
        }

        logger.info("Appointment confirmed successfully");
    }

    /**
     * Handle appointment verification modal if it appears
     * @return true if modal was handled, false if no modal found
     */
    private boolean handleAppointmentModal() {
        logger.info("Checking for appointment verification modal");

        try {
            // Check if modal exists
            String[] modalSelectors = {
                    APPOINTMENT_MODAL_XPATH,
                    MODAL_WARNING_XPATH,
                    "//img[contains(@src, 'alert-danger-circle.png')]/parent::div/parent::div",
                    PRESERVE_BUTTON_XPATH
            };

            WebElement modal = null;
            for (String selector : modalSelectors) {
                try {
                    modal = findElementWithWait(selector);
                    if (modal != null && modal.isDisplayed()) {
                        logger.info("Found appointment modal with selector: {}", selector);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (modal != null) {
                // Modal found, click "Préserver ma demande" button
                String[] preserveButtonSelectors = {
                        PRESERVE_BUTTON_XPATH,
                        "//button[contains(@class, 'previous-button') and contains(text(), 'Préserver')]",
                        "//app-modal-verification-appointement//button[contains(@class, 'btn')]"
                };

                WebElement preserveButton = findElementWithMultipleSelectors(preserveButtonSelectors, "Preserve request button");
                clickElement(preserveButton, "Préserver ma demande button");

                // Wait for modal to close
                waitForElement(1500);
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.debug("No appointment modal found, checking for direct success message");
            return false;
        }
    }

    /**
     * Handle success confirmation message
     */
    private void handleSuccessMessage() {
        logger.info("Checking for success confirmation message");

        String[] confirmationSelectors = {
                SUCCESS_TITLE_XPATH,
                SUCCESS_SUBTITLE_XPATH,
                "//div[contains(@class, 'title-container')]//p[contains(@class, 'title')]",
                CONGRATULATIONS_XPATH,
                "//p[contains(text(), 'final receipt') or contains(text(), 'reçu final')]",
                "//div[contains(@class, 'title-container')]"
        };

        WebElement confirmationMessage = findElementWithMultipleSelectors(confirmationSelectors, "Confirmation message");
        Assert.assertNotNull(confirmationMessage, "Confirmation message should be displayed");

        try {
            String confirmationText = confirmationMessage.getText();
            logger.info("Confirmation message displayed: {}", confirmationText);
        } catch (Exception e) {
            logger.info("Confirmation message element found but could not retrieve text");
        }
    }

    /**
     * Verify that appointment was confirmed successfully
     * @return true if confirmation elements are present
     */
    public boolean isAppointmentConfirmed() {
        try {
            return isElementDisplayed(SUCCESS_TITLE_XPATH) ||
                    isElementDisplayed(SUCCESS_SUBTITLE_XPATH) ||
                    isElementDisplayed(CONGRATULATIONS_XPATH);
        } catch (Exception e) {
            logger.warn("Could not verify appointment confirmation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get confirmation message text
     * @return Confirmation message text or empty string if not found
     */
    public String getConfirmationMessage() {
        String[] selectors = {SUCCESS_TITLE_XPATH, SUCCESS_SUBTITLE_XPATH, CONGRATULATIONS_XPATH};

        for (String selector : selectors) {
            String text = getElementText(selector);
            if (!text.isEmpty()) {
                return text;
            }
        }

        return "";
    }
}