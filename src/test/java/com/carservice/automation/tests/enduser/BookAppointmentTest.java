package com.carservice.automation.tests.enduser;

import com.carservice.automation.base.BaseTest;
import com.carservice.automation.pages.enduser.VehicleIdentificationPage;
import com.carservice.automation.pages.enduser.AppointmentFormPage;
import com.carservice.automation.pages.enduser.RepairerSelectionPage;
import com.carservice.automation.pages.enduser.AppointmentConfirmationPage;
import com.carservice.automation.utils.AllureUtils;
import io.qameta.allure.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

/**
 * Test class for vehicle appointment booking functionality
 * Tests the complete end-to-end flow of booking an appointment
 */
@Epic("Vehicle Appointment Booking")
@Feature("End-to-End Diagnostic Service Appointment Flow")
public class BookAppointmentTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(BookAppointmentTest.class);

    // Page Objects
    private VehicleIdentificationPage vehicleIdentificationPage;
    private AppointmentFormPage appointmentFormPage;
    private RepairerSelectionPage repairerSelectionPage;
    private AppointmentConfirmationPage confirmationPage;

    @Test(groups = {"e2e", "appointment", "enduser"})
    @Story("Complete Appointment Flow with All Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates complete appointment booking flow with and without file upload")
    @Link(name = "Test Case", url = "https://your-test-management-tool.com/testcase/123")
    @Issue("APP-456")
    @TmsLink("TC-001")
    public void testCompleteAppointmentFlow_AllScenarios() {
        AllureUtils.startTestCase("Complete Appointment Flow Test - All Scenarios");

        try {
            initializeTestEnvironment();
            performVehicleIdentificationValidation();
            executeCompleteAppointmentFlow();

            AllureUtils.addTestResult("All appointment flow scenarios completed successfully");
            logger.info("All appointment flow scenarios completed successfully");

        } catch (Exception e) {
            AllureUtils.addFailureInfo("Complete flow test failed", e);
            logger.error("Complete flow test failed: {}", e.getMessage());
            takeScreenshot("ERROR_CompleteFlow");
            throw new RuntimeException("Appointment flow test failed", e);
        }
    }

    @Step("Initialize test environment and page objects")
    private void initializeTestEnvironment() {
        AllureUtils.addEnvironmentInfo();
        AllureUtils.addTestData();

        initializePageObjects();
        setupTestEnvironment();
    }

    @Step("Initialize all page objects")
    private void initializePageObjects() {
        vehicleIdentificationPage = new VehicleIdentificationPage(driver);
        appointmentFormPage = new AppointmentFormPage(driver);
        repairerSelectionPage = new RepairerSelectionPage(driver);
        confirmationPage = new AppointmentConfirmationPage(driver);

        AllureUtils.logStep("Page objects initialized successfully");
    }

    @Step("Setup test environment and navigate to application")
    private void setupTestEnvironment() {
        navigateToEndUserApp();
        vehicleIdentificationPage.changeLanguageToEnglish();

        AllureUtils.attachScreenshot("Application loaded");
        AllureUtils.logStep("Test environment setup completed");
    }

    @Step("Perform vehicle identification validation tests")
    private void performVehicleIdentificationValidation() {
        AllureUtils.logStep("Starting vehicle identification validation");

        vehicleIdentificationPage.clickMakeAppointmentButton();
        vehicleIdentificationPage.selectSerieNormaleOption();
        vehicleIdentificationPage.clickNextButton();

        // Test input validation scenarios with Allure reporting
        testEmptyFieldsValidation();
        testIncompleteFieldsValidation();
        testCompleteFieldsValidation();
        testFieldClearingValidation();

        AllureUtils.logStep("Vehicle identification validation completed successfully");
    }

    @Step("Test empty fields disable button functionality")
    private void testEmptyFieldsValidation() {
        vehicleIdentificationPage.validateEmptyFieldsDisableButton();
        AllureUtils.logStep("Empty fields validation passed");
    }

    @Step("Test incomplete fields disable button functionality")
    private void testIncompleteFieldsValidation() {
        vehicleIdentificationPage.validateIncompleteFieldsDisableButton();
        AllureUtils.logStep("Incomplete fields validation passed");
    }

    @Step("Test complete fields enable button functionality")
    private void testCompleteFieldsValidation() {
        vehicleIdentificationPage.validateCompleteFieldsEnableButton();
        AllureUtils.logStep("Complete fields validation passed");
    }

    @Step("Test field clearing disables button functionality")
    private void testFieldClearingValidation() {
        vehicleIdentificationPage.validateClearingFieldsDisableButton();
        AllureUtils.logStep("Field clearing validation passed");
    }

    @Step("Execute complete appointment flow with file upload")
    private void executeCompleteAppointmentFlow() {
        AllureUtils.logStep("Starting complete appointment flow with file upload");

        proceedWithVehicleIdentification();
        fillAppointmentForm();
        selectRepairerAndSchedule();
        confirmAndValidateAppointment();

        AllureUtils.logStep("Complete appointment flow executed successfully");
    }

    @Step("Proceed with vehicle identification")
    private void proceedWithVehicleIdentification() {
        vehicleIdentificationPage.clickNextButton();
        AllureUtils.attachScreenshot("Vehicle identification completed");
    }

    @Step("Fill appointment form with validation and file upload")
    private void fillAppointmentForm() {
        appointmentFormPage.selectDiagnosticService();
        AllureUtils.attachScreenshot("Diagnostic service selected");

        appointmentFormPage.fillDiagnosticFormWithValidation(true);
        AllureUtils.attachScreenshot("Appointment form filled");

        appointmentFormPage.clickNextButton();
        AllureUtils.logStep("Appointment form completed with validation and file upload");
    }

    @Step("Select repairer and schedule appointment")
    private void selectRepairerAndSchedule() {
        repairerSelectionPage.viewRepairerInfo();
        AllureUtils.attachScreenshot("Repairer information viewed");

        repairerSelectionPage.selectRepairer();
        AllureUtils.attachScreenshot("Repairer selected");

        repairerSelectionPage.clickNextButton();

        repairerSelectionPage.selectDateAndTime();
        AllureUtils.attachScreenshot("Date and time selected");

        repairerSelectionPage.clickNextButton();
        AllureUtils.logStep("Repairer selected and appointment scheduled");
    }

    @Step("Confirm appointment and validate success")
    private void confirmAndValidateAppointment() {
        confirmationPage.verifySummaryInformation();
        AllureUtils.attachScreenshot("Summary information verified");

        confirmationPage.confirmAppointment();
        AllureUtils.attachScreenshot("Appointment confirmation clicked");

        confirmationPage.handlePostConfirmationFlow();
        AllureUtils.attachScreenshot("Appointment confirmed successfully");

        // Add confirmation details to Allure report
        String confirmationMessage = confirmationPage.getConfirmationMessage();
        AllureUtils.addParameter("Confirmation Message", confirmationMessage);

        AllureUtils.logStep("Appointment confirmed and validated successfully");
    }
}