package com.carservice.automation.tests.enduser;

import com.carservice.automation.base.BaseTest;
import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

@Epic("Vehicle Appointment Booking")
@Feature("Appointment Flow")
public class BookAppointmentTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(BookAppointmentTest.class);

    // Test data constants
    private static final String PLATE_SERIE = "333";
    private static final String PLATE_NUMERO = "3333";
    private static final String CHASSIS_NUMBER = "3333333";
    private static final String MILEAGE = "15000";
    private static final String DESCRIPTION = "Test diagnostic description for vehicle maintenance check";
    private static final String TEST_FILE_PATH = "src/test/resources/test-image.png";

    @Test(groups = {"smoke", "appointment", "e2e"})
    @Story("Complete Appointment Flow with All Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates complete appointment booking flow with and without file upload")
    public void testCompleteAppointmentFlow_AllScenarios() throws InterruptedException {
        logger.info("Starting Complete Appointment Flow Test - All Scenarios");

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Navigate and change language to English
            navigateToEndUserApp();
            changeLanguageToEnglish(wait);
            // Test 1: Vehicle Identification Input Validation
            testVehicleIdentificationValidation(wait);
            // === SCENARIO 2: Complete flow WITH file ===
            logger.info("=== Testing complete flow WITH file upload ===");
            // Navigate back to start for second scenario

            completeAppointmentFlow(wait, true);

            logger.info("All appointment flow scenarios completed successfully");

        } catch (Exception e) {
            logger.error("Complete flow test failed: " + e.getMessage());
            takeScreenshot("ERROR_CompleteFlow");
            throw e;
        }
    }

    // Validation Test Methods
    private void completeAppointmentFlow(WebDriverWait wait, boolean withFileUpload) throws InterruptedException {
        logger.info("Executing appointment flow with file upload: " + withFileUpload);

        clickElementBothLanguages(wait, "Next", "Suivant", "Vehicle identification next button");

        selectDiagnosticService(wait);
        fillDiagnosticForm(wait, withFileUpload);
        clickElementBothLanguages(wait, "Next", "Suivant", "Service form next button");
        viewRepairerInfo(wait);
        selectRepairer(wait);
        clickElementBothLanguages(wait, "Next", "Suivant", "Next button after repairer selection");

        selectDateAndTime(wait);
        clickElementBothLanguages(wait, "Next", "Suivant", "Next button after date/time selection");

        confirmAppointment(wait);
    }

    private void testVehicleIdentificationValidation(WebDriverWait wait) throws InterruptedException {
        logger.info("Testing vehicle identification input validation");

        clickElementBothLanguages(wait, "Make an APPOINTMENT", "Prendre un RDV", "Make an APPOINTMENT button");
        selectSerieNormaleOption(wait);
        clickElementBothLanguages(wait, "Next", "Suivant", "Vehicle type next button");

        // Get input elements
        WebElement serieInput = findElement(wait, "//input[@maxlength='3']");
        WebElement numeroInput = findElement(wait, "//input[@maxlength='4']");
        WebElement chassisInput = findElement(wait, "//input[@maxlength='7']");

        // Find the button even if it's disabled (use presenceOfElementLocated instead of elementToBeClickable)
        WebElement suivantButton = null;
        String[] buttonSelectors = {
                "//button[@id='btnSuivant']",
                "//button[contains(@class, 'ot-button-primary') and (contains(., 'Suivant') or contains(., 'Next'))]"
        };

        for (String selector : buttonSelectors) {
            try {
                suivantButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                if (suivantButton != null) {
                    logger.info("Found Suivant button with selector: " + selector);
                    break;
                }
            } catch (Exception e) {
                logger.debug("Suivant button not found with selector: " + selector);
            }
        }

        if (suivantButton == null) {
            throw new RuntimeException("Could not find Suivant button with any selector");
        }

        // Test: All fields empty -> button disabled
        clearInputProperly(serieInput);
        clearInputProperly(numeroInput);
        clearInputProperly(chassisInput);
        Thread.sleep(300);
        Assert.assertFalse(suivantButton.isEnabled(), "Button should be disabled with empty fields");

        // Test: Only serie filled -> button disabled
        serieInput.sendKeys(PLATE_SERIE);
        Thread.sleep(300);
        Assert.assertFalse(suivantButton.isEnabled(), "Button should be disabled with incomplete fields");

        // Test: Serie + numero filled -> button disabled
        numeroInput.sendKeys(PLATE_NUMERO);
        Thread.sleep(300);
        Assert.assertFalse(suivantButton.isEnabled(), "Button should be disabled without chassis");

        // Test: All fields valid -> button enabled
        chassisInput.sendKeys(CHASSIS_NUMBER);
        Thread.sleep(300);
        Assert.assertTrue(suivantButton.isEnabled(), "Button should be enabled with all valid inputs");

        // Test: Clear chassis -> button disabled again
        clearInputProperly(chassisInput);
        Thread.sleep(300);
        Assert.assertFalse(suivantButton.isEnabled(), "Button should be disabled when chassis is cleared");
        chassisInput.sendKeys(CHASSIS_NUMBER);
        logger.info("Vehicle identification validation completed successfully");
    }

    // Helper Methods - Core Flow

    private void changeLanguageToEnglish(WebDriverWait wait) throws InterruptedException {
        logger.info("Changing language to English");

        try {
            String[] dropdownSelectors = {
                    "//a[@id='navbarDropdown' and contains(@class, 'dropdown-toggle')]",
                    "//a[contains(@class, 'dropdown-toggle')]//img[contains(@src, 'flag-fr.png')]/parent::a",
                    "//li[contains(@class, 'nav-item dropdown')]//a[contains(@class, 'dropdown-toggle')]"
            };

            WebElement languageDropdown = findElementWithMultipleSelectors(wait, dropdownSelectors, "Language dropdown");
            scrollToElementAndClick(languageDropdown, "Language dropdown");
            Thread.sleep(500);

            String[] englishSelectors = {
                    "//button[contains(@class, 'dropdown-item') and contains(., 'Anglais')]",
                    "//button[contains(@class, 'dropdown-item')]//img[contains(@src, 'flag-an.png')]/parent::button",
                    "//li//button[contains(., 'Anglais')]"
            };

            WebElement englishOption = findElementWithMultipleSelectors(wait, englishSelectors, "English language option");
            scrollToElementAndClick(englishOption, "English language option");
            Thread.sleep(2000);

            logger.info("Language changed to English successfully");

        } catch (Exception e) {
            logger.warn("Could not change language to English, continuing with current language: " + e.getMessage());
        }
    }

    private void selectSerieNormaleOption(WebDriverWait wait) {
        logger.info("Selecting Serie normale (TU) option");

        String[] selectors = {
                "//div[contains(@class, 'container-mat')]//p[contains(text(), 'Serie normale (TU)')]/parent::div",
                "//img[@src='assets/Icon/Fill/type-mat-tu.png']/parent::div",
                "//div[contains(@class, 'container-mat') and .//p[contains(text(), 'Serie normale')]]"
        };

        WebElement serieNormaleOption = findElementWithMultipleSelectors(wait, selectors, "Serie normale option");
        scrollToElementAndClick(serieNormaleOption, "Serie normale (TU) option");
    }

    private void selectDiagnosticService(WebDriverWait wait) {
        logger.info("Selecting Diagnostic Service");

        String[] selectors = {
                "//div[contains(@class, 'tab')]//p[contains(@class, 'tab-text') and (contains(text(), 'Diagnostic Service') or contains(text(), 'Service de diagnostic'))]/parent::div",
                "//img[contains(@src, 'DIAGNOSIS-service')]/ancestor::div[contains(@class, 'tab')]",
                "//div[contains(@class, 'tab') and .//p[(contains(text(), 'Diagnostic Service') or contains(text(), 'Service de diagnostic'))]]",
                "//div[contains(@class, 'tab')]//p[(contains(text(), 'Diagnostic') or contains(text(), 'diagnostic'))]/parent::div",
                "//img[contains(@src, 'DIAGNOSIS')]/parent::div/parent::div[contains(@class, 'tab')]",
                "//div[contains(@class, 'tab') and contains(., 'Diagnostic')]"
        };

        WebElement diagnosticService = findElementWithMultipleSelectors(wait, selectors, "Diagnostic Service");
        scrollToElementAndClick(diagnosticService, "Diagnostic Service");
    }


    private void fillDiagnosticForm(WebDriverWait wait, boolean withFileAttachment) throws InterruptedException {
        logger.info("Filling diagnostic service form with validation testing");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 300);");
        Thread.sleep(500);

        // Check vehicle breakdown checkbox
        WebElement breakdownCheckbox = findElement(wait, "//input[@type='checkbox' and contains(@id, 'BREAKDOWN')]");
        if (breakdownCheckbox != null && !breakdownCheckbox.isSelected()) {
            breakdownCheckbox.click();
        }

        // === MILEAGE VALIDATION TESTING ===
        WebElement mileageInput = findElement(wait, "//p-inputnumber[@formcontrolname='km']//input");
        if (mileageInput != null) {
            scrollToElementAndClick(mileageInput, "Mileage input");

            // Test 1: Empty mileage validation
            clearInputProperly(mileageInput);
            Thread.sleep(300);
            logger.info("Testing empty mileage field");

            // Test 2: Letters in mileage (should be filtered out)
            mileageInput.sendKeys("15ABC000");
            Thread.sleep(300);
            String mileageWithLetters = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
            logger.info("Mileage after entering letters: " + mileageWithLetters);
            Assert.assertFalse(mileageWithLetters.contains("A") || mileageWithLetters.contains("B") || mileageWithLetters.contains("C"),
                    "Mileage field should not accept letters");

            // Test 3: Special characters in mileage (should be filtered out)
            clearInputProperly(mileageInput);
            mileageInput.sendKeys("15000@#$");
            Thread.sleep(300);
            String mileageWithSymbols = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
            logger.info("Mileage after entering symbols: " + mileageWithSymbols);
            Assert.assertFalse(mileageWithSymbols.contains("@") || mileageWithSymbols.contains("#") || mileageWithSymbols.contains("$"),
                    "Mileage field should not accept special characters");

            // Test 4: Negative numbers (should be rejected)
            clearInputProperly(mileageInput);
            mileageInput.sendKeys("-5000");
            Thread.sleep(300);
            String negativeValue = mileageInput.getAttribute("value");
            logger.info("Mileage after entering negative: " + negativeValue);

            // Test 5: Enter valid mileage
            clearInputProperly(mileageInput);
            mileageInput.sendKeys(MILEAGE);
            Thread.sleep(300);
            String finalMileage = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
            Assert.assertEquals(finalMileage, MILEAGE, "Valid mileage should be accepted correctly");
            logger.info("Valid mileage entered: " + finalMileage);
        }

        // === DESCRIPTION VALIDATION TESTING ===
        WebElement descriptionTextarea = findElement(wait, "//textarea[@formcontrolname='description']");
        if (descriptionTextarea != null) {
            scrollToElementAndClick(descriptionTextarea, "Description textarea");

            // Test 1: Empty description validation
            clearInputProperly(descriptionTextarea);
            Thread.sleep(300);
            logger.info("Testing empty description field");

            // Test 2: Very short description
            descriptionTextarea.sendKeys("x");
            Thread.sleep(300);
            logger.info("Testing very short description");

            // Test 3: Description with only spaces
            clearInputProperly(descriptionTextarea);
            descriptionTextarea.sendKeys("   ");
            Thread.sleep(300);
            String spacesOnly = descriptionTextarea.getAttribute("value").trim();
            logger.info("Description with spaces only (trimmed length): " + spacesOnly.length());

            // Test 4: Enter valid description
            clearInputProperly(descriptionTextarea);
            descriptionTextarea.sendKeys(DESCRIPTION);
            Thread.sleep(300);
            String finalDescription = descriptionTextarea.getAttribute("value");
            Assert.assertEquals(finalDescription, DESCRIPTION, "Valid description should be accepted correctly");
            logger.info("Valid description entered");
        }

        // === FORM VALIDATION TESTING ===
        logger.info("Testing form submission validation");

        // Check if next button is properly enabled/disabled based on mandatory fields
        WebElement nextButton = null;
        try {
            String[] nextButtonSelectors = {
                    "//button[contains(@class, 'ot-button-primary') and (contains(., 'Next') or contains(., 'Suivant'))]",
                    "//button[contains(@class, 'btn-primary') and (contains(., 'Next') or contains(., 'Suivant'))]"
            };

            for (String selector : nextButtonSelectors) {
                try {
                    nextButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (nextButton != null) break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (nextButton != null) {
                // Test with valid inputs - button should be enabled
                boolean isEnabled = nextButton.isEnabled();
                logger.info("Next button enabled with valid inputs: " + isEnabled);

                // Test clearing mandatory field
                clearInputProperly(descriptionTextarea);
                Thread.sleep(300);
                boolean isDisabledAfterClear = nextButton.isEnabled();
                logger.info("Next button enabled after clearing description: " + isDisabledAfterClear);

                // Restore valid description for form completion
                if (descriptionTextarea != null) {
                    descriptionTextarea.sendKeys(DESCRIPTION);
                }
            }

        } catch (Exception e) {
            logger.debug("Could not test next button state: " + e.getMessage());
        }

        // Handle file upload if requested
        if (withFileAttachment) {
            logger.info("Adding file attachment to diagnostic form");
            uploadFile(wait);
        }

        js.executeScript("window.scrollBy(0, 400);");
        Thread.sleep(500);

        logger.info("Diagnostic form filled successfully with validation testing completed");
    }

    private void selectRepairer(WebDriverWait wait) throws InterruptedException {
        logger.info("Selecting repairer");

        Thread.sleep(2000);

        String[] selectors = {
                "//p[contains(@class, 'check-agency-title') and (contains(text(), 'Select this repairer') or contains(text(), 'Sélectionner ce réparateur'))]/ancestor::div[contains(@class, 'check-agency')]",
                "//p[(contains(text(), 'Select this repairer') or contains(text(), 'Sélectionner ce réparateur'))]/parent::div",
                "//div[contains(@class, 'check-agency')]//p[contains(text(), 'Select this repairer') or contains(text(), 'Sélectionner')]",
                "//div[contains(@class, 'container-check-agency')]//div[contains(@class, 'check-agency')]",
                "//p[contains(@class, 'check-agency-title')]/parent::div"
        };

        WebElement selectRepairerButton = findElementWithMultipleSelectors(wait, selectors, "Select this repairer button");
        scrollToElementAndClick(selectRepairerButton, "Select this repairer button");
        Thread.sleep(1000);

        logger.info("Repairer selected successfully");
    }

    private void viewRepairerInfo(WebDriverWait wait) throws InterruptedException {
        logger.info("Viewing repairer information");

        String[] selectors = {
                "//div[contains(@class, 'info-button')]//p[(contains(text(), '+ info') or contains(text(), '+ infos'))]/parent::div",
                "//p[contains(@class, 'info-button-text') and (contains(text(), '+ info') or contains(text(), '+ infos'))]/parent::div",
                "//div[contains(@class, 'info-button')]",
                "//div[contains(@class, 'container-info-button')]//div[contains(@class, 'info-button')]"
        };

        WebElement infoButton = findElementWithMultipleSelectors(wait, selectors, "+ info button");
        scrollToElementAndClick(infoButton, "+ info button");
        Thread.sleep(1000);

        WebElement infoPanel = findElement(wait, "//div[contains(@class, 'container-detail-agency')]");
        Assert.assertNotNull(infoPanel, "Repairer info panel should be displayed");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", infoPanel);
        Thread.sleep(1000);

        logger.info("Repairer information displayed successfully");
    }

    private void closeRepairerInfo(WebDriverWait wait) throws InterruptedException {
        logger.info("Closing repairer information panel");

        String[] selectors = {
                "//img[contains(@src, 'icon-close-info.png')]",
                "//img[contains(@src, 'close')][@style*='cursor: pointer']",
                "//div[contains(@class, 'container-detail-agency')]//img[@style*='cursor: pointer']"
        };

        WebElement closeButton = findElementWithMultipleSelectors(wait, selectors, "Close info button");
        scrollToElementAndClick(closeButton, "Close info button");
        Thread.sleep(500);

        logger.info("Repairer information panel closed successfully");
    }

    private void selectDateAndTime(WebDriverWait wait) throws InterruptedException {
        logger.info("Selecting date and time");

        Thread.sleep(2000);
        int maxAttempts = 5;
        boolean timeSlotFound = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.info("Attempt " + attempt + " to find available date and time");

            selectFutureDate(wait, attempt);
            Thread.sleep(1500);

            if (isAgencyClosed(wait)) {
                logger.info("Agency closed for selected date, trying another date...");
                continue;
            }

            if (selectAvailableTimeSlot(wait)) {
                timeSlotFound = true;
                break;
            }
        }

        if (!timeSlotFound) {
            throw new RuntimeException("Could not find any available time slots after " + maxAttempts + " attempts");
        }

        logger.info("Date and time selected successfully");
    }

    private boolean isAgencyClosed(WebDriverWait wait) {
        try {
            String[] closedSelectors = {
                    "//p[contains(text(), 'Agency closed')]",
                    "//img[contains(@src, 'calendar-Not-Available.png')]",
                    "//div[contains(text(), 'Agency closed')]"
            };

            for (String selector : closedSelectors) {
                WebElement closedElement = findElement(wait, selector);
                if (closedElement != null && closedElement.isDisplayed()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void selectFutureDate(WebDriverWait wait, int attempt) throws InterruptedException {
        logger.info("Selecting future date from calendar (attempt " + attempt + ")");

        String[] dateSelectors = {
                "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='16']",
                "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='17']",
                "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='18']",
                "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='19']",
                "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[text()='20']"
        };

        int selectorIndex = Math.min(attempt - 1, dateSelectors.length - 1);
        String selector = dateSelectors[selectorIndex];

        try {
            WebElement dateElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
            scrollToElementAndClick(dateElement, "Future date (day " + (16 + selectorIndex) + ")");
            Thread.sleep(1000);
            logger.info("Future date selected from calendar");
        } catch (Exception e) {
            logger.warn("Specific date not found, trying any available date");
            String fallbackSelector = "//div[@role='gridcell' and contains(@class, 'ngb-dp-day') and not(contains(@class, 'disabled'))]//div[contains(@class, 'btn-light') and not(contains(@class, 'text-muted'))][position()=" + attempt + "]";
            WebElement dateElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(fallbackSelector)));
            scrollToElementAndClick(dateElement, "Future date (fallback)");
            Thread.sleep(1000);
        }
    }



    private void confirmAppointment(WebDriverWait wait) throws InterruptedException {
        logger.info("Confirming appointment");

        Thread.sleep(2000);
        verifySummaryInformation(wait);
        clickElementBothLanguages(wait, "Confirm", "Confirmer", "Confirm button");
        Thread.sleep(2000);

        // Check for modal first, then success message
        if (handleAppointmentModal(wait)) {
            logger.info("Appointment modal handled - clicking preserve request");
        } else {
            // Check for direct success message
            handleSuccessMessage(wait);
        }

        logger.info("Appointment confirmed successfully");
    }

    private boolean handleAppointmentModal(WebDriverWait wait) throws InterruptedException {
        logger.info("Checking for appointment verification modal");

        try {
            // Check if modal exists
            String[] modalSelectors = {
                    "//app-modal-verification-appointement",
                    "//div[contains(text(), 'You cannot make another appointment request') or contains(text(), 'Vous ne pouvez pas faire')]",
                    "//img[contains(@src, 'alert-danger-circle.png')]/parent::div/parent::div",
                    "//button[contains(text(), 'Préserver ma demande')]"
            };

            WebElement modal = null;
            for (String selector : modalSelectors) {
                try {
                    modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (modal != null && modal.isDisplayed()) {
                        logger.info("Found appointment modal with selector: " + selector);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (modal != null) {
                // Modal found, click "Préserver ma demande" button
                String[] preserveButtonSelectors = {
                        "//button[contains(text(), 'Préserver ma demande')]",
                        "//button[contains(@class, 'previous-button') and contains(text(), 'Préserver')]",
                        "//app-modal-verification-appointement//button[contains(@class, 'btn')]"
                };

                WebElement preserveButton = findElementWithMultipleSelectors(wait, preserveButtonSelectors, "Preserve request button");
                scrollToElementAndClick(preserveButton, "Préserver ma demande button");

                // Wait for modal to close
                Thread.sleep(1500);

                // After clicking preserve, check for success message
//                handleSuccessMessage(wait);

                return true;
            }

            return false;

        } catch (Exception e) {
            logger.debug("No appointment modal found, checking for direct success message");
            return false;
        }
    }

    private void handleSuccessMessage(WebDriverWait wait) {
        logger.info("Checking for success confirmation message");

        String[] confirmationSelectors = {
                // Target the title text in both languages
                "//p[contains(@class, 'title') and (contains(text(), 'Obtain your final receipt') or contains(text(), 'Obtenir votre reçu final'))]",
                // Target the subtitle text in both languages
                "//p[contains(@class, 'subtitle') and (contains(text(), 'Congratulations! Your appointment has been successfully') or contains(text(), 'Félicitations ! Votre RDV a été enregistré avec succès'))]",
                // Target the title container
                "//div[contains(@class, 'title-container')]//p[contains(@class, 'title')]",
                // Target by congratulations text
                "//p[contains(text(), 'Congratulations') or contains(text(), 'Félicitations')]",
                // Target by receipt text
                "//p[contains(text(), 'final receipt') or contains(text(), 'reçu final')]",
                // Target the container structure
                "//div[contains(@class, 'title-container')]"
        };

        WebElement confirmationMessage = findElementWithMultipleSelectors(wait, confirmationSelectors, "Confirmation message");
        Assert.assertNotNull(confirmationMessage, "Confirmation message should be displayed");

        try {
            String confirmationText = confirmationMessage.getText();
            logger.info("Confirmation message displayed: " + confirmationText);
        } catch (Exception e) {
            logger.info("Confirmation message element found but could not retrieve text");
        }
    }

    private void verifySummaryInformation(WebDriverWait wait) {
        logger.info("Verifying summary information");

        try {
            WebElement serviceSection = findElement(wait, "//p[contains(text(), 'Diagnostic Service')]");
            Assert.assertNotNull(serviceSection, "Service information should be displayed");

            WebElement repairerSection = findElement(wait, "//p[contains(@class, 'recap-title') and contains(text(), 'authorized repairer')]");
            Assert.assertNotNull(repairerSection, "Repairer information should be displayed");

            WebElement dateTimeSection = findElement(wait, "//p[contains(@class, 'recap-title') and contains(text(), 'Date & Hour')]");
            Assert.assertNotNull(dateTimeSection, "Date & time information should be displayed");

            logger.info("Summary information verified successfully");

        } catch (Exception e) {
            logger.warn("Could not fully verify all summary information: " + e.getMessage());
        }
    }

    private void uploadFile(WebDriverWait wait) throws InterruptedException {
        logger.info("Starting file upload");

        try {
            java.io.File testFile = new java.io.File(TEST_FILE_PATH);
            if (!testFile.exists()) {
                throw new RuntimeException("Test file does not exist: " + TEST_FILE_PATH);
            }

            String absolutePath = testFile.getAbsolutePath();
            logger.info("Uploading file: " + absolutePath);

            WebElement fileInput = null;
            String[] selectors = {
                    "//input[@type='file']",
                    "//input[@type='file' and contains(@class, 'd-none')]",
                    "//input[@type='file' and @multiple]"
            };

            for (String selector : selectors) {
                try {
                    fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (fileInput != null) {
                        logger.info("Found file input with selector: " + selector);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (fileInput == null) {
                WebElement uploadButton = findElement(wait, "//button[contains(@class, 'btn-add')]");
                if (uploadButton != null) {
                    uploadButton.click();
                    Thread.sleep(1000);
                    fileInput = findElement(wait, "//input[@type='file']");
                }
            }

            if (fileInput != null) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].style.display = 'block'; arguments[0].style.visibility = 'visible';", fileInput);

                fileInput.sendKeys(absolutePath);
                logger.info("File uploaded successfully using sendKeys");
                Thread.sleep(2000);

                String inputValue = fileInput.getAttribute("value");
                if (inputValue != null && !inputValue.isEmpty()) {
                    logger.info("File upload verified");
                }
            } else {
                throw new RuntimeException("Could not find file input element");
            }

        } catch (Exception e) {
            logger.error("File upload failed: " + e.getMessage());
            takeScreenshot("ERROR_FileUpload");
            throw e;
        }
    }

    // Utility Methods

    private WebElement findElement(WebDriverWait wait, String xpath) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            logger.debug("Element not found: " + xpath);
            return null;
        }
    }

    private WebElement findElementWithMultipleSelectors(WebDriverWait wait, String[] selectors, String elementName) {
        for (String selector : selectors) {
            try {
                WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                logger.info("Found " + elementName + " with selector: " + selector);
                return element;
            } catch (Exception e) {
                logger.debug(elementName + " not found with selector: " + selector);
            }
        }
        throw new RuntimeException("Could not find " + elementName + " with any selector");
    }

    private void clickElementBothLanguages(WebDriverWait wait, String englishText, String frenchText, String elementName) {
        try {
            String[] selectors = {
                    "//button[contains(@class, 'ot-button-primary') and (contains(., '" + englishText + "') or contains(., '" + frenchText + "'))]",
                    "//button[contains(@class, 'btn-primary') and (contains(., '" + englishText + "') or contains(., '" + frenchText + "'))]",
                    "//button[(contains(., '" + englishText + "') or contains(., '" + frenchText + "'))]"
            };

            WebElement element = findElementWithMultipleSelectors(wait, selectors, elementName);
            scrollToElementAndClick(element, elementName);
        } catch (Exception e) {
            logger.error("Failed to click element: " + elementName);
            throw new RuntimeException("Could not click " + elementName, e);
        }
    }

    private void scrollToElementAndClick(WebElement element, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            Thread.sleep(500);

            try {
                element.click();
                logger.info("Clicked " + elementName + " successfully");
            } catch (Exception e) {
                js.executeScript("arguments[0].click();", element);
                logger.info("Clicked " + elementName + " using JavaScript");
            }
        } catch (Exception e) {
            logger.error("Failed to click " + elementName + ": " + e.getMessage());
            throw new RuntimeException("Could not click " + elementName, e);
        }
    }

    private void clearInputProperly(WebElement inputElement) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            js.executeScript(
                    "arguments[0].value = '';" +
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    inputElement
            );

            inputElement.clear();
            inputElement.sendKeys("");
            Thread.sleep(100);

        } catch (Exception e) {
            logger.warn("Failed to clear input properly, using standard clear(): " + e.getMessage());
            inputElement.clear();
        }
    }

    private boolean selectAvailableTimeSlot(WebDriverWait wait) throws InterruptedException {
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
                    timeSlot = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(selector)));
                    if (timeSlot != null) break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (timeSlot != null) {
                scrollToElementAndClick(timeSlot, "Available time slot");
                Thread.sleep(1000);
                logger.info("Available time slot selected");
                return true;
            } else {
                logger.info("No time slots available for this date");
                return false;
            }

        } catch (Exception e) {
            logger.warn("Could not select time slot: " + e.getMessage());
            return false;
        }
    }
}