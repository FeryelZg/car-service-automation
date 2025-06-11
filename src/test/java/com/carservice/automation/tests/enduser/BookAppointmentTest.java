package com.carservice.automation.tests.enduser;

import com.carservice.automation.base.BaseTest;
import com.carservice.automation.pages.VehicleIdentificationPage;
import com.carservice.automation.pages.AppointmentFormPage;
import com.carservice.automation.pages.RepairerSelectionPage;
import com.carservice.automation.pages.AppointmentConfirmationPage;
import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

/**
 * Test class for vehicle appointment booking functionality
 * Tests the complete end-to-end flow of booking an appointment
 */
@Epic("Vehicle Appointment Booking")
@Feature("Appointment Flow")
public class BookAppointmentTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(BookAppointmentTest.class);

    // Page Objects
    private VehicleIdentificationPage vehicleIdentificationPage;
    private AppointmentFormPage appointmentFormPage;
    private RepairerSelectionPage repairerSelectionPage;
    private AppointmentConfirmationPage confirmationPage;

    @Test(groups = {"smoke", "appointment", "e2e"})
    @Story("Complete Appointment Flow with All Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates complete appointment booking flow")
    public void testCompleteAppointmentFlow_AllScenarios() {
        logger.info("Starting Complete Appointment Flow Test - All Scenarios");

        try {
            initializePageObjects();
            setupTestEnvironment();

            // Test vehicle identification validation
            testVehicleIdentificationValidation();

            // Test complete flow with file upload
            testCompleteFlowWithFileUpload();

            logger.info("All appointment flow scenarios completed successfully");

        } catch (Exception e) {
            logger.error("Complete flow test failed: {}", e.getMessage());
            takeScreenshot("ERROR_CompleteFlow");
            throw new RuntimeException("Appointment flow test failed", e);
        }
    }

    /**
     * Initialize all page objects
     */
    private void initializePageObjects() {
        vehicleIdentificationPage = new VehicleIdentificationPage(driver);
        appointmentFormPage = new AppointmentFormPage(driver);
        repairerSelectionPage = new RepairerSelectionPage(driver);
        confirmationPage = new AppointmentConfirmationPage(driver);
    }

    /**
     * Setup test environment and navigate to application
     */
    private void setupTestEnvironment() {
        navigateToEndUserApp();
        vehicleIdentificationPage.changeLanguageToEnglish();
    }

    /**
     * Test vehicle identification input validation
     */
    private void testVehicleIdentificationValidation() {
        logger.info("Testing vehicle identification input validation");

        vehicleIdentificationPage.clickMakeAppointmentButton();
        vehicleIdentificationPage.selectSerieNormaleOption();
        vehicleIdentificationPage.clickNextButton();

        // Test input validation scenarios
        vehicleIdentificationPage.validateEmptyFieldsDisableButton();
        vehicleIdentificationPage.validateIncompleteFieldsDisableButton();
        vehicleIdentificationPage.validateCompleteFieldsEnableButton();
        vehicleIdentificationPage.validateClearingFieldsDisableButton();

        logger.info("Vehicle identification validation completed successfully");
    }

    /**
     * Test complete appointment flow with file upload
     */
    private void testCompleteFlowWithFileUpload() {
        logger.info("Testing complete flow WITH file upload");

        // Vehicle identification (already filled from validation test)
        vehicleIdentificationPage.clickNextButton();

        // Service selection and form filling
        appointmentFormPage.selectDiagnosticService();
        appointmentFormPage.fillDiagnosticFormWithValidation(true);
        appointmentFormPage.clickNextButton();

        // Repairer selection
        repairerSelectionPage.viewRepairerInfo();
        repairerSelectionPage.selectRepairer();
        repairerSelectionPage.clickNextButton();

        // Date and time selection
        repairerSelectionPage.selectDateAndTime();
        repairerSelectionPage.clickNextButton();

        // Confirmation
        confirmationPage.verifySummaryInformation();
        confirmationPage.confirmAppointment();
        confirmationPage.handlePostConfirmationFlow();

        logger.info("Complete flow with file upload completed successfully");
    }
}