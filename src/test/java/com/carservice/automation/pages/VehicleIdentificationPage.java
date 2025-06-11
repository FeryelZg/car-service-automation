package com.carservice.automation.pages;

import com.carservice.automation.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

/**
 * Page Object class for Vehicle Identification functionality
 */
public class VehicleIdentificationPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(VehicleIdentificationPage.class);

    // Locators
    private static final String LANGUAGE_DROPDOWN_XPATH = "//a[@id='navbarDropdown' and contains(@class, 'dropdown-toggle')]";
    private static final String ENGLISH_OPTION_XPATH = "//button[contains(@class, 'dropdown-item') and contains(., 'Anglais')]";
    private static final String MAKE_APPOINTMENT_BUTTON_XPATH = "//button[contains(., 'Make an APPOINTMENT') or contains(., 'Prendre un RDV')]";
    private static final String SERIE_NORMALE_OPTION_XPATH = "//div[contains(@class, 'container-mat')]//p[contains(text(), 'Serie normale (TU)')]/parent::div";
    private static final String NEXT_BUTTON_XPATH = "//button[contains(@class, 'ot-button-primary') and (contains(., 'Next') or contains(., 'Suivant'))]";

    // Input field locators
    private static final String SERIE_INPUT_XPATH = "//input[@maxlength='3']";
    private static final String NUMERO_INPUT_XPATH = "//input[@maxlength='4']";
    private static final String CHASSIS_INPUT_XPATH = "//input[@maxlength='7']";

    // Test data from config
    private final String plateSerie;
    private final String plateNumero;
    private final String chassisNumber;

    public VehicleIdentificationPage(WebDriver driver) {
        super(driver);
        this.plateSerie = configReader.getProperty("vehicle.plate.serie");
        this.plateNumero = configReader.getProperty("vehicle.plate.numero");
        this.chassisNumber = configReader.getProperty("vehicle.chassis.number");
    }

    /**
     * Change application language to English
     */
    public void changeLanguageToEnglish() {
        logger.info("Changing language to English");

        try {
            WebElement languageDropdown = findElementWithWait(LANGUAGE_DROPDOWN_XPATH);
            clickElement(languageDropdown, "Language dropdown");

            waitForElement(500);

            WebElement englishOption = findElementWithWait(ENGLISH_OPTION_XPATH);
            clickElement(englishOption, "English language option");

            waitForElement(2000);
            logger.info("Language changed to English successfully");

        } catch (Exception e) {
            logger.warn("Could not change language to English, continuing with current language: {}", e.getMessage());
        }
    }

    /**
     * Click the Make Appointment button
     */
    public void clickMakeAppointmentButton() {
        logger.info("Clicking Make Appointment button");
        WebElement makeAppointmentBtn = findElementWithWait(MAKE_APPOINTMENT_BUTTON_XPATH);
        clickElement(makeAppointmentBtn, "Make an APPOINTMENT button");
    }

    /**
     * Select Serie Normale option
     */
    public void selectSerieNormaleOption() {
        logger.info("Selecting Serie normale (TU) option");
        WebElement serieNormaleOption = findElementWithWait(SERIE_NORMALE_OPTION_XPATH);
        clickElement(serieNormaleOption, "Serie normale (TU) option");
    }

    /**
     * Click the Next button
     */
    public void clickNextButton() {
        logger.info("Clicking Next button");
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);
        clickElement(nextButton, "Next button");
    }

    /**
     * Validate that empty fields disable the next button
     */
    public void validateEmptyFieldsDisableButton() {
        logger.info("Testing empty fields validation");

        WebElement serieInput = findElementWithWait(SERIE_INPUT_XPATH);
        WebElement numeroInput = findElementWithWait(NUMERO_INPUT_XPATH);
        WebElement chassisInput = findElementWithWait(CHASSIS_INPUT_XPATH);
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);

        // Clear all fields
        clearInput(serieInput);
        clearInput(numeroInput);
        clearInput(chassisInput);

        waitForElement(300);

        Assert.assertFalse(nextButton.isEnabled(), "Button should be disabled with empty fields");
        logger.info("Empty fields validation passed");
    }

    /**
     * Validate that incomplete fields disable the next button
     */
    public void validateIncompleteFieldsDisableButton() {
        logger.info("Testing incomplete fields validation");

        WebElement serieInput = findElementWithWait(SERIE_INPUT_XPATH);
        WebElement numeroInput = findElementWithWait(NUMERO_INPUT_XPATH);
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);

        // Fill only serie
        serieInput.sendKeys(plateSerie);
        waitForElement(300);
        Assert.assertFalse(nextButton.isEnabled(), "Button should be disabled with incomplete fields");

        // Fill serie + numero
        numeroInput.sendKeys(plateNumero);
        waitForElement(300);
        Assert.assertFalse(nextButton.isEnabled(), "Button should be disabled without chassis");

        logger.info("Incomplete fields validation passed");
    }

    /**
     * Validate that complete fields enable the next button
     */
    public void validateCompleteFieldsEnableButton() {
        logger.info("Testing complete fields validation");

        WebElement chassisInput = findElementWithWait(CHASSIS_INPUT_XPATH);
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);

        // Fill chassis to complete all fields
        chassisInput.sendKeys(chassisNumber);
        waitForElement(300);

        Assert.assertTrue(nextButton.isEnabled(), "Button should be enabled with all valid inputs");
        logger.info("Complete fields validation passed");
    }

    /**
     * Validate that clearing chassis field disables the button again
     */
    public void validateClearingFieldsDisableButton() {
        logger.info("Testing field clearing validation");

        WebElement chassisInput = findElementWithWait(CHASSIS_INPUT_XPATH);
        WebElement nextButton = findElementWithWait(NEXT_BUTTON_XPATH);

        // Clear chassis field
        clearInput(chassisInput);
        waitForElement(300);

        Assert.assertFalse(nextButton.isEnabled(), "Button should be disabled when chassis is cleared");

        // Restore chassis for next steps
        chassisInput.sendKeys(chassisNumber);
        waitForElement(300);

        logger.info("Field clearing validation passed");
    }

    /**
     * Fill vehicle identification form with valid data
     */
    public void fillVehicleIdentificationForm() {
        logger.info("Filling vehicle identification form with valid data");

        WebElement serieInput = findElementWithWait(SERIE_INPUT_XPATH);
        WebElement numeroInput = findElementWithWait(NUMERO_INPUT_XPATH);
        WebElement chassisInput = findElementWithWait(CHASSIS_INPUT_XPATH);

        clearInput(serieInput);
        serieInput.sendKeys(plateSerie);

        clearInput(numeroInput);
        numeroInput.sendKeys(plateNumero);

        clearInput(chassisInput);
        chassisInput.sendKeys(chassisNumber);

        logger.info("Vehicle identification form filled successfully");
    }
}