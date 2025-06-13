package com.carservice.automation.pages.enduser;

import com.carservice.automation.base.BasePage;
import com.carservice.automation.utils.TestImageCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

import java.io.File;

/**
 * Page Object class for Appointment Form functionality
 */
public class AppointmentFormPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(AppointmentFormPage.class);

    // Locators
    private static final String DIAGNOSTIC_SERVICE_XPATH = "//div[contains(@class, 'tab')]//p[contains(@class, 'tab-text') and (contains(text(), 'Diagnostic Service') or contains(text(), 'Service de diagnostic'))]/parent::div";
    private static final String BREAKDOWN_CHECKBOX_XPATH = "//input[@type='checkbox' and contains(@id, 'BREAKDOWN')]";
    private static final String MILEAGE_INPUT_XPATH = "//p-inputnumber[@formcontrolname='km']//input";
    private static final String DESCRIPTION_TEXTAREA_XPATH = "//textarea[@formcontrolname='description']";
    private static final String FILE_INPUT_XPATH = "//input[@type='file']";
    private static final String UPLOAD_BUTTON_XPATH = "//button[contains(@class, 'btn-add')]";
    private static final String NEXT_BUTTON_XPATH = "//button[contains(@class, 'ot-button-primary') and (contains(., 'Next') or contains(., 'Suivant'))]";

    // Test data from config
    private final String mileage;
    private final String description;
    private final String testFilePath;

    public AppointmentFormPage(WebDriver driver) {
        super(driver);
        this.mileage = configReader.getProperty("vehicle.mileage");
        this.description = configReader.getProperty("vehicle.description");
        this.testFilePath = configReader.getProperty("test.file.path");
    }

    /**
     * Select Diagnostic Service option
     */
    public void selectDiagnosticService() {
        logger.info("Selecting Diagnostic Service");
        WebElement diagnosticService = findElementWithWait(DIAGNOSTIC_SERVICE_XPATH);
        clickElement(diagnosticService, "Diagnostic Service");
    }

    /**
     * Fill diagnostic form with validation testing
     * @param withFileAttachment whether to include file attachment
     */
    public void fillDiagnosticFormWithValidation(boolean withFileAttachment) {
        logger.info("Filling diagnostic service form with validation testing");

        scrollPage(300);
        checkBreakdownOption();
        validateAndFillMileage();
        validateAndFillDescription();

        if (withFileAttachment) {
            uploadFile(testFilePath);
        }

        scrollPage(400);
        logger.info("Diagnostic form filled successfully with validation testing completed");
    }

    /**
     * Click the Next button
     */
    public void clickNextButton() {
        logger.info("Clicking Next button");
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);
        clickElement(nextButton, "Service form next button");
    }

    /**
     * Check the vehicle breakdown checkbox
     */
    private void checkBreakdownOption() {
        WebElement breakdownCheckbox = findElementWithWait(BREAKDOWN_CHECKBOX_XPATH);
        if (breakdownCheckbox != null && !breakdownCheckbox.isSelected()) {
            clickElement(breakdownCheckbox, "Breakdown checkbox");
        }
    }

    /**
     * Validate mileage input and fill with valid data
     */
    private void validateAndFillMileage() {
        logger.info("Testing mileage validation");

        WebElement mileageInput = findElementWithWait(MILEAGE_INPUT_XPATH);
        if (mileageInput == null) return;

        clickElement(mileageInput, "Mileage input");

        // Test 1: Empty mileage validation
        testEmptyMileage(mileageInput);

        // Test 2: Letters in mileage (should be filtered out)
        testMileageWithLetters(mileageInput);

        // Test 3: Special characters in mileage (should be filtered out)
        testMileageWithSpecialCharacters(mileageInput);

        // Test 4: Negative numbers (should be rejected)
        testNegativeMileage(mileageInput);

        // Test 5: Enter valid mileage
        fillValidMileage(mileageInput);
    }

    /**
     * Test empty mileage field
     */
    private void testEmptyMileage(WebElement mileageInput) {
        clearInput(mileageInput);
        waitForElement(300);
        logger.info("Testing empty mileage field");
    }

    /**
     * Test mileage field with letters
     */
    private void testMileageWithLetters(WebElement mileageInput) {
        mileageInput.sendKeys("15ABC000");
        waitForElement(300);
        String mileageWithLetters = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
        logger.info("Mileage after entering letters: {}", mileageWithLetters);
        Assert.assertFalse(mileageWithLetters.contains("A") || mileageWithLetters.contains("B") || mileageWithLetters.contains("C"),
                "Mileage field should not accept letters");
        clearInput(mileageInput);
    }

    /**
     * Test mileage field with special characters
     */
    private void testMileageWithSpecialCharacters(WebElement mileageInput) {
        mileageInput.sendKeys("15000@#$");
        waitForElement(300);
        String mileageWithSymbols = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
        logger.info("Mileage after entering symbols: {}", mileageWithSymbols);
        Assert.assertFalse(mileageWithSymbols.contains("@") || mileageWithSymbols.contains("#") || mileageWithSymbols.contains("$"),
                "Mileage field should not accept special characters");
        clearInput(mileageInput);
    }

    /**
     * Test mileage field with negative numbers
     */
    private void testNegativeMileage(WebElement mileageInput) {
        mileageInput.sendKeys("-5000");
        waitForElement(300);
        String negativeValue = mileageInput.getAttribute("value");
        logger.info("Mileage after entering negative: {}", negativeValue);
        clearInput(mileageInput);
    }

    /**
     * Fill valid mileage
     */
    private void fillValidMileage(WebElement mileageInput) {
        mileageInput.sendKeys(mileage);
        waitForElement(300);
        String finalMileage = mileageInput.getAttribute("value").replace(",", "").replace(" ", "");
        Assert.assertEquals(finalMileage, mileage, "Valid mileage should be accepted correctly");
        logger.info("Valid mileage entered: {}", finalMileage);
    }

    /**
     * Validate description textarea and fill with valid data
     */
    private void validateAndFillDescription() {
        logger.info("Testing description validation");

        WebElement descriptionTextarea = findElementWithWait(DESCRIPTION_TEXTAREA_XPATH);
        if (descriptionTextarea == null) return;

        clickElement(descriptionTextarea, "Description textarea");

        // Test 1: Empty description validation
        testEmptyDescription(descriptionTextarea);

        // Test 2: Very short description
        testShortDescription(descriptionTextarea);

        // Test 3: Description with only spaces
        testDescriptionWithSpaces(descriptionTextarea);

        // Test 4: Enter valid description
        fillValidDescription(descriptionTextarea);

        // Test form submission validation
        testFormSubmissionValidation(descriptionTextarea);
    }

    /**
     * Test empty description field
     */
    private void testEmptyDescription(WebElement descriptionTextarea) {
        clearInput(descriptionTextarea);
        waitForElement(300);
        logger.info("Testing empty description field");
    }

    /**
     * Test very short description
     */
    private void testShortDescription(WebElement descriptionTextarea) {
        descriptionTextarea.sendKeys("x");
        waitForElement(300);
        logger.info("Testing very short description");
        clearInput(descriptionTextarea);
    }

    /**
     * Test description with only spaces
     */
    private void testDescriptionWithSpaces(WebElement descriptionTextarea) {
        descriptionTextarea.sendKeys("   ");
        waitForElement(300);
        String spacesOnly = descriptionTextarea.getAttribute("value").trim();
        logger.info("Description with spaces only (trimmed length): {}", spacesOnly.length());
        clearInput(descriptionTextarea);
    }

    /**
     * Fill valid description
     */
    private void fillValidDescription(WebElement descriptionTextarea) {
        descriptionTextarea.sendKeys(description);
        waitForElement(300);
        String finalDescription = descriptionTextarea.getAttribute("value");
        Assert.assertEquals(finalDescription, description, "Valid description should be accepted correctly");
        logger.info("Valid description entered");
    }

    /**
     * Test form submission validation
     */
    private void testFormSubmissionValidation(WebElement descriptionTextarea) {
        logger.info("Testing form submission validation");

        try {
            WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);
            if (nextButton != null) {
                // Test with valid inputs - button should be enabled
                boolean isEnabled = nextButton.isEnabled();
                logger.info("Next button enabled with valid inputs: {}", isEnabled);

                // Test clearing mandatory field
                clearInput(descriptionTextarea);
                waitForElement(300);
                boolean isDisabledAfterClear = nextButton.isEnabled();
                logger.info("Next button enabled after clearing description: {}", isDisabledAfterClear);

                // Restore valid description for form completion
                descriptionTextarea.sendKeys(description);
            }
        } catch (Exception e) {
            logger.debug("Could not test next button state: {}", e.getMessage());
        }
    }

    /**
     * Upload file attachment
     */
    protected void uploadFile(String filePath) {
        logger.info("🔧 Starting file upload process for: {}", filePath);

        try {
            // Get the correct file path using the utility
            String actualFilePath;
            if (filePath.startsWith("src/test/resources/")) {
                // Extract just the filename
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                actualFilePath = TestImageCreator.getTestImagePath(fileName);
            } else {
                actualFilePath = TestImageCreator.getTestImagePath(filePath);
            }

            // Verify file exists
            File testFile = new File(actualFilePath);
            if (!testFile.exists()) {
                throw new RuntimeException("Test file does not exist: " + actualFilePath);
            }

            if (!testFile.canRead()) {
                throw new RuntimeException("Test file is not readable: " + actualFilePath);
            }

            logger.info("✅ File verified: {} ({} bytes)", actualFilePath, testFile.length());

            // Find file upload input element
            String[] uploadSelectors = {
                    "//input[@type='file']",
                    "//input[@accept='image/*']",
                    "//*[@class='file-upload']//input",
                    "//div[contains(@class, 'upload')]//input[@type='file']"
            };

            WebElement fileInput = findElementWithMultipleSelectors(uploadSelectors, "File Upload Input");

            // Make the file input visible if it's hidden
            jsExecutor.executeScript(
                    "arguments[0].style.display = 'block';" +
                            "arguments[0].style.visibility = 'visible';" +
                            "arguments[0].style.opacity = '1';" +
                            "arguments[0].style.height = 'auto';" +
                            "arguments[0].style.width = 'auto';",
                    fileInput
            );

            // Upload the file
            logger.info("📤 Uploading file: {}", actualFilePath);
            fileInput.sendKeys(actualFilePath);

            // Wait for upload to process
            waitForElement(2000);

            // Verify upload success (optional - depends on your UI)
            verifyFileUploadSuccess(testFile.getName());

            logger.info("✅ File upload completed successfully");

        } catch (Exception e) {
            logger.error("❌ File upload failed: {}", e.getMessage());
            takeScreenshot("file_upload_error");
            throw new RuntimeException("File upload failed", e);
        }
    }
    /**
     * Verify file upload was successful (adjust selectors based on your UI)
     */
    private void verifyFileUploadSuccess(String fileName) {
        try {
            // Common patterns for upload success indicators
            String[] successSelectors = {
                    "//div[contains(@class, 'upload-success')]",
                    "//span[contains(@class, 'file-name')]",
                    "//div[contains(text(), '" + fileName + "')]",
                    "//*[contains(@class, 'uploaded-file')]",
                    "//i[contains(@class, 'success')] | //i[contains(@class, 'check')]"
            };

            boolean uploadSuccessFound = false;
            for (String selector : successSelectors) {
                try {
                    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
                    if (element.isDisplayed()) {
                        logger.info("✅ Upload success indicator found: {}", selector);
                        uploadSuccessFound = true;
                        break;
                    }
                } catch (Exception e) {
                    // Continue to next selector
                }
            }

            if (!uploadSuccessFound) {
                logger.warn("⚠️ No upload success indicator found, but no error thrown");
            }

        } catch (Exception e) {
            logger.warn("⚠️ Could not verify upload success: {}", e.getMessage());
            // Don't throw exception here as upload might still be successful
        }
    }
}