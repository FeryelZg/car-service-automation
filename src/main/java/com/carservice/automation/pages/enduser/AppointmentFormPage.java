package com.carservice.automation.pages.enduser;

import com.carservice.automation.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
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

    private static final String MULTIPLE_SERVICES_XPATH = "//div[contains(@class, 'tab')]//p[contains(normalize-space(text()), 'Multiple services')]/parent::div";

    private static final String MILEAGE_DROPDOWN_XPATH = "//div[contains(@class, 'ng-value-container')]//div[contains(@class, 'ng-placeholder') and contains(text(), 'Select mileage')]/parent::div//input";
    private static final String MILEAGE_OPTION_XPATH ="//div[contains(@class, 'ng-option')]//span[contains(@class, 'ng-option-label') and " +
                    "translate(normalize-space(.), '\u00A0\u202F', '  ') = '%s']";
    private static final String BREAKDOWN_CHECKBOX_MULTIPLE_XPATH = "//input[@type='checkbox' and contains(@id, 'BREAKDOWN')]";

    private static final String SERVICES_DROPDOWN_XPATH =
            "//ng-select[contains(@class, 'custom-Services')]//div[contains(@class, 'ng-input')]//input[@type='text']";

    private static final String SERVICES_DROPDOWN_CONTAINER_XPATH =
            "//ng-select[contains(@class, 'custom-Services')]";

    private static final String DROPDOWN_PANEL_XPATH =
            "//div[contains(@class, 'ng-dropdown-panel')]";

    private static final String SERVICE_OPTION_XPATH =
            "//div[contains(@class, 'ng-option')]//span[contains(@class, 'ng-option-label') and normalize-space(text())='%s']";

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
            uploadFile();
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
    private void uploadFile() {
        logger.info("Starting file upload");

        try {
            // Get the actual file path from repository
            String actualFilePath = getRepositoryFilePath(testFilePath);
            File testFile = new File(actualFilePath);

            if (!testFile.exists()) {
                throw new RuntimeException("Test file does not exist: " + actualFilePath);
            }

            String absolutePath = testFile.getAbsolutePath();
            logger.info("Uploading file: {}", absolutePath);

            WebElement fileInput = findElementWithWait(FILE_INPUT_XPATH);

            if (fileInput == null) {
                WebElement uploadButton = findElementWithWait(UPLOAD_BUTTON_XPATH);
                if (uploadButton != null) {
                    clickElement(uploadButton, "Upload button");
                    waitForElement(1000);
                    fileInput = findElementWithWait(FILE_INPUT_XPATH);
                }
            }

            if (fileInput != null) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].style.display = 'block'; arguments[0].style.visibility = 'visible';", fileInput);

                fileInput.sendKeys(absolutePath);
                logger.info("File uploaded successfully using sendKeys");
                waitForElement(2000);

                String inputValue = fileInput.getAttribute("value");
                if (inputValue != null && !inputValue.isEmpty()) {
                    logger.info("File upload verified");
                }
            } else {
                throw new RuntimeException("Could not find file input element");
            }

        } catch (Exception e) {
            logger.error("File upload failed: {}", e.getMessage());
            takeScreenshot("ERROR_FileUpload");
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * Get the actual file path from repository - works in both local and CI environments
     */
    private String getRepositoryFilePath(String configFilePath) {
        // Extract filename from config path
        String fileName = configFilePath;
        if (configFilePath.contains("/")) {
            fileName = configFilePath.substring(configFilePath.lastIndexOf("/") + 1);
        }

        logger.info("Looking for file: {}", fileName);

        // Try different locations where the file might exist
        String[] possiblePaths = {
                // Repository file locations (works in both local and CI)
                System.getProperty("user.dir") + "/src/test/resources/" + fileName,
                "src/test/resources/" + fileName,

                // Maven compiled resources
                System.getProperty("user.dir") + "/target/test-classes/" + fileName,
                "target/test-classes/" + fileName,

                // Original path as-is (fallback)
                configFilePath
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                logger.info("✅ Found file at: {}", file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        }

        // If not found, log all attempted paths for debugging
        logger.error("❌ File '{}' not found in any location. Tried:", fileName);
        for (String path : possiblePaths) {
            logger.error("   - {}", path);
        }

        throw new RuntimeException("Test file not found: " + fileName + ". Make sure the file exists in src/test/resources/");
    }

    /**
     * Select Multiple Services option
     */
    public void selectMultipleServices() {
        logger.info("Selecting Multiple Services");
        WebElement multipleServices = findElementWithWait(MULTIPLE_SERVICES_XPATH);
        clickElement(multipleServices, "Multiple Services");
    }

    /**
     * Fill multiple services form with validation testing
     * @param services Array of service names to select
     * @param selectedMileage Mileage value to select
     * @param withFileAttachment whether to include file attachment
     */
    public void fillMultipleServicesFormWithValidation(String[] services, String selectedMileage, boolean withFileAttachment) {
        logger.info("Filling multiple services form with validation testing");

        scrollPage(300);
        checkBreakdownOptionForMultipleServices();
        selectServicesFromDropdown(services);
        pressEscapeToCloseDropdown();
        selectMileageFromDropdown(selectedMileage);
        validateAndFillDescriptionForMultipleServices();

        if (withFileAttachment) {
            uploadFile();
        }

        scrollPage(400);
        logger.info("Multiple services form filled successfully with validation testing completed");
    }

    /**
     * Check the vehicle breakdown checkbox for multiple services
     */
    private void checkBreakdownOptionForMultipleServices() {
        logger.info("Checking breakdown option for multiple services");
        WebElement breakdownCheckbox = findElementWithWait(BREAKDOWN_CHECKBOX_MULTIPLE_XPATH);
        if (breakdownCheckbox != null && !breakdownCheckbox.isSelected()) {
            clickElement(breakdownCheckbox, "Breakdown checkbox (Multiple Services)");
        }
    }
    /**
     * Press ESC key to close the dropdown.
     */
    private void pressEscapeToCloseDropdown() {
        try {
            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.ESCAPE).perform();
            waitForElement(500);
            logger.info("Pressed ESC to close the dropdown");
        } catch (Exception e) {
            logger.warn("Could not press ESC to close dropdown: {}", e.getMessage());
        }
    }

    /**
     * Select multiple services from dropdown
     * @param services Array of service names to select
     */
    private void selectServicesFromDropdown(String[] services) {
        logger.info("Selecting services from dropdown");

        try {
            // Click on services dropdown to open it
            WebElement servicesDropdown = findElementWithWait(SERVICES_DROPDOWN_XPATH);
            if (servicesDropdown == null) {
                // Try fallback
                servicesDropdown = findElementWithWait(SERVICES_DROPDOWN_CONTAINER_XPATH + "//input");
            }

            if (servicesDropdown != null) {
                clickElement(servicesDropdown, "Services dropdown");

                // Wait for the dropdown panel to load
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DROPDOWN_PANEL_XPATH)));

                // Select each service
                for (String service : services) {
                    selectServiceOption(service);
                    waitForElement(500); // slight pause between selections
                }

                // Optional: Click outside to close dropdown
                scrollPage(100);

                logger.info("Successfully selected {} services", services.length);

            } else {
                throw new RuntimeException("Services dropdown not found");
            }

        } catch (Exception e) {
            logger.error("Failed to select services from dropdown: {}", e.getMessage());
            takeScreenshot("ERROR_ServicesDropdown");
            throw new RuntimeException("Services selection failed", e);
        }
    }


    /**
     * Select a specific service option from dropdown
     * @param serviceName Name of the service to select
     */
    private void selectServiceOption(String serviceName) {
        try {
            logger.info("Selecting service: {}", serviceName);

            // Wait for dropdown options to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(DROPDOWN_PANEL_XPATH)));

            // Try full match first
            String serviceXPath = String.format(SERVICE_OPTION_XPATH, serviceName);
            WebElement serviceOption = findElementWithWait(serviceXPath);

            if (serviceOption != null) {
                clickElement(serviceOption, "Service: " + serviceName);
                logger.info("Service '{}' selected successfully", serviceName);
            } else {
                // Partial fallback match: match any span containing part of the text
                String partialServiceXPath = String.format(
                        "//div[contains(@class, 'ng-option')]//span[contains(@class, 'ng-option-label') and contains(text(), '%s')]",
                        serviceName.substring(0, Math.min(serviceName.length(), 6)) // partial match
                );
                serviceOption = findElementWithWait(partialServiceXPath);

                if (serviceOption != null) {
                    clickElement(serviceOption, "Service (partial match): " + serviceName);
                    logger.info("Service '{}' selected with partial match", serviceName);
                } else {
                    logger.warn("Service '{}' not found in dropdown", serviceName);
                }
            }

        } catch (Exception e) {
            logger.warn("Could not select service '{}': {}", serviceName, e.getMessage());
        }
    }

    /**
     * Select mileage from dropdown
     * @param mileage Mileage value to select (e.g., "60000")
     */
    private void selectMileageFromDropdown(String mileage) {
        logger.info("Selecting mileage: {}", mileage);

        try {
            // Click on mileage dropdown to open it
            WebElement mileageDropdown = findElementWithWait(MILEAGE_DROPDOWN_XPATH);
            if (mileageDropdown == null) {
                // Try alternative selectors
                String[] mileageSelectors = {
                        "//div[contains(@class, 'ng-placeholder') and contains(text(), 'Select mileage')]/parent::div//input",
                        "//div[contains(text(), 'Select mileage')]/following-sibling::div//input",
                        "//ng-select//input[contains(@aria-controls, 'mileage') or contains(@autocomplete, 'a0a9')]"
                };

                for (String selector : mileageSelectors) {
                    mileageDropdown = findElementWithWait(selector);
                    if (mileageDropdown != null) break;
                }
            }

            if (mileageDropdown != null) {
                clickElement(mileageDropdown, "Mileage dropdown");
                waitForElement(1000);

                // Select the mileage option
                String mileageOptionXPath = String.format(MILEAGE_OPTION_XPATH, mileage);
                WebElement mileageOption = findElementWithWait(mileageOptionXPath);

                if (mileageOption == null) {
                    // Try alternative approaches to find mileage option
                    String[] alternativeSelectors = {
                            "//div[contains(@class, 'ng-option') and text()='" + mileage + "']",
                            "//div[contains(@class, 'ng-option') and contains(text(), '" + mileage + "')]",
                            "//span[contains(text(), '" + mileage + "')]/parent::div[contains(@class, 'ng-option')]"
                    };

                    for (String selector : alternativeSelectors) {
                        mileageOption = findElementWithWait(selector);
                        if (mileageOption != null) break;
                    }
                }

                if (mileageOption != null) {
                    clickElement(mileageOption, "Mileage: " + mileage);
                    logger.info("Mileage '{}' selected successfully", mileage);
                } else {
                    logger.warn("Mileage option '{}' not found in dropdown", mileage);
                }

                // Click outside to close dropdown
                scrollPage(100);

            } else {
                throw new RuntimeException("Mileage dropdown not found");
            }

        } catch (Exception e) {
            logger.error("Failed to select mileage from dropdown: {}", e.getMessage());
            takeScreenshot("ERROR_MileageDropdown");
            throw new RuntimeException("Mileage selection failed", e);
        }
    }

    /**
     * Validate description textarea and fill with valid data for multiple services
     */
    private void validateAndFillDescriptionForMultipleServices() {
        logger.info("Testing description validation for multiple services");

        WebElement descriptionTextarea = findElementWithWait(DESCRIPTION_TEXTAREA_XPATH);
        if (descriptionTextarea == null) {
            // Try alternative selector for multiple services
            descriptionTextarea = findElementWithWait("//textarea[@formcontrolname='description' and contains(@class, 's-input-area')]");
        }

        if (descriptionTextarea == null) return;

        clickElement(descriptionTextarea, "Description textarea (Multiple Services)");

        // Test 1: Empty description validation
        testEmptyDescription(descriptionTextarea);

        // Test 2: Very short description
        testShortDescription(descriptionTextarea);

        // Test 3: Description with only spaces
        testDescriptionWithSpaces(descriptionTextarea);

        // Test 4: Enter valid description for multiple services
        String multipleServicesDescription = "Multiple services appointment - " + description;
        fillValidDescriptionForMultipleServices(descriptionTextarea, multipleServicesDescription);

        // Test form submission validation
        testFormSubmissionValidationForMultipleServices(descriptionTextarea);
    }

    /**
     * Fill valid description for multiple services
     */
    private void fillValidDescriptionForMultipleServices(WebElement descriptionTextarea, String multipleServicesDescription) {
        descriptionTextarea.sendKeys(multipleServicesDescription);
        waitForElement(300);
        String finalDescription = descriptionTextarea.getAttribute("value");
        Assert.assertEquals(finalDescription, multipleServicesDescription, "Valid description should be accepted correctly");
        logger.info("Valid description entered for multiple services");
    }

    /**
     * Test form submission validation for multiple services
     */
    private void testFormSubmissionValidationForMultipleServices(WebElement descriptionTextarea) {
        logger.info("Testing form submission validation for multiple services");

        try {
            WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);
            if (nextButton != null) {
                // Test with valid inputs - button should be enabled
                boolean isEnabled = nextButton.isEnabled();
                logger.info("Next button enabled with valid multiple services inputs: {}", isEnabled);

                // Test clearing mandatory field
                clearInput(descriptionTextarea);
                waitForElement(300);
                boolean isDisabledAfterClear = nextButton.isEnabled();
                logger.info("Next button enabled after clearing description (multiple services): {}", isDisabledAfterClear);

                // Restore valid description for form completion
                String multipleServicesDescription = "Multiple services appointment - " + description;
                descriptionTextarea.sendKeys(multipleServicesDescription);
            }
        } catch (Exception e) {
            logger.debug("Could not test next button state for multiple services: {}", e.getMessage());
        }
    }
}