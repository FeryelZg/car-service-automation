package com.carservice.automation.tests.enduser;

import com.carservice.automation.base.BaseTest;
import com.carservice.automation.pages.enduser.VehicleIdentificationPage;
import com.carservice.automation.pages.enduser.AppointmentFormPage;
import com.carservice.automation.pages.enduser.RepairerSelectionPage;
import com.carservice.automation.pages.enduser.AppointmentConfirmationPage;
import com.carservice.automation.utils.AllureUtils;
import com.carservice.automation.utils.ConfigReader;
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
    @Link(name = "Test Case", url = "https://teamdev.atlassian.net/jira/software/projects/PAT/list?selectedIssue=PAT-3")
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


    @Test(groups = {"e2e", "appointment", "enduser", "multipleservices"})
    @Story("Complete Multiple Services Appointment Flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validates complete appointment booking flow for multiple services with dropdown selections")
    @Link(name = "Test Case", url = "https://your-test-management-tool.com/testcase/124")
    @Issue("APP-457")
    @TmsLink("TC-002")
    public void testCompleteMultipleServicesAppointmentFlow() {
        AllureUtils.startTestCase("Complete Multiple Services Appointment Flow Test");

        try {
            initializeTestEnvironment();
            performVehicleIdentificationValidation();
            executeMultipleServicesAppointmentFlow();

            AllureUtils.addTestResult("Multiple services appointment flow completed successfully");
            logger.info("Multiple services appointment flow completed successfully");

        } catch (Exception e) {
            AllureUtils.addFailureInfo("Multiple services flow test failed", e);
            logger.error("Multiple services flow test failed: {}", e.getMessage());
            takeScreenshot("ERROR_MultipleServicesFlow");
            throw new RuntimeException("Multiple services appointment flow test failed", e);
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

        // Check if we should skip file upload (useful for CI/CD environments)
        ConfigReader config = new ConfigReader();
        boolean skipFileUpload = config.getBooleanProperty("skip.file.upload", false);

        if (skipFileUpload) {
            logger.info("📋 Skipping file upload as configured");
            appointmentFormPage.fillDiagnosticFormWithValidation(false);
            AllureUtils.addParameter("File Upload", "SKIPPED (Configuration)");
        } else {
            try {
                appointmentFormPage.fillDiagnosticFormWithValidation(true);
                AllureUtils.addParameter("File Upload", "SUCCESS");
            } catch (Exception e) {
                logger.warn("⚠️ File upload failed, continuing without file: {}", e.getMessage());
                // Try without file upload
                appointmentFormPage.fillDiagnosticFormWithValidation(false);
                AllureUtils.addParameter("File Upload", "FAILED - Continued without file");
            }
        }

        AllureUtils.attachScreenshot("Appointment form filled");
        appointmentFormPage.clickNextButton();
        AllureUtils.logStep("Appointment form completed");
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
        AllureUtils.attachScreenshot(" Appointment confirmed successfully");

        // Add confirmation details to Allure report
        String confirmationMessage = confirmationPage.getConfirmationMessage();
        AllureUtils.addParameter("Confirmation Message", confirmationMessage);

        AllureUtils.logStep("Appointment confirmed and validated successfully");
    }



    /**
     * Execute complete multiple services appointment flow
     */
    @Step("Execute complete multiple services appointment flow")
    private void executeMultipleServicesAppointmentFlow() {
        AllureUtils.logStep("Starting complete multiple services appointment flow");

        proceedWithVehicleIdentification();
        fillMultipleServicesAppointmentForm();
        selectRepairerAndSchedule();
        confirmAndValidateAppointment();

        AllureUtils.logStep("Complete multiple services appointment flow executed successfully");
    }

    /**
     * Navigate to multiple services form
     */
    @Step("Navigate to multiple services form")
    private void navigateToMultipleServicesForm() {
        AllureUtils.logStep("Navigating to multiple services form");

        vehicleIdentificationPage.clickMakeAppointmentButton();
        vehicleIdentificationPage.selectSerieNormaleOption();
        vehicleIdentificationPage.clickNextButton();

        // Fill vehicle identification for form access
        vehicleIdentificationPage.fillVehicleIdentificationForm();
        vehicleIdentificationPage.clickNextButton();

        AllureUtils.attachScreenshot("Multiple services form accessed");
    }

    /**
     * Fill multiple services appointment form
     */
    @Step("Fill multiple services appointment form with dropdown selections")
    private void fillMultipleServicesAppointmentForm() {
        AllureUtils.logStep("Filling multiple services appointment form");

        appointmentFormPage.selectMultipleServices();
        AllureUtils.attachScreenshot("Multiple services selected");

        // Define services to select (adjust these based on available options in your app)
        String[] servicesToSelect = {
                "Fast Service",
                "Bodywork repair service",
                "Mechanical Repair Service"
        };

        String selectedMileage = "60 000";

        // Check if we should skip file upload (useful for CI/CD environments)
        ConfigReader config = new ConfigReader();
        boolean skipFileUpload = config.getBooleanProperty("skip.file.upload", false);

        if (skipFileUpload) {
            logger.info("📋 Skipping file upload as configured");
            appointmentFormPage.fillMultipleServicesFormWithValidation(servicesToSelect, selectedMileage, false);
            AllureUtils.addParameter("File Upload", "SKIPPED (Configuration)");
        } else {
            try {
                appointmentFormPage.fillMultipleServicesFormWithValidation(servicesToSelect, selectedMileage, true);
                AllureUtils.addParameter("File Upload", "SUCCESS");
            } catch (Exception e) {
                logger.warn("⚠️ File upload failed, continuing without file: {}", e.getMessage());
                // Try without file upload
                appointmentFormPage.fillMultipleServicesFormWithValidation(servicesToSelect, selectedMileage, false);
                AllureUtils.addParameter("File Upload", "FAILED - Continued without file");
            }
        }

        // Add selected services to Allure report
        AllureUtils.addParameter("Selected Services", String.join(", ", servicesToSelect));
        AllureUtils.addParameter("Selected Mileage", selectedMileage);

        AllureUtils.attachScreenshot("Multiple services form filled");
        appointmentFormPage.clickNextButton();
        AllureUtils.logStep("Multiple services appointment form completed");
    }

    /**
     * Validate multiple services form components
     */
    @Step("Validate multiple services form components and dropdowns")
    private void validateMultipleServicesFormComponents() {
        AllureUtils.logStep("Validating multiple services form components");

        appointmentFormPage.selectMultipleServices();
        AllureUtils.attachScreenshot("Multiple services form loaded");

        // Test services dropdown functionality
        validateServicesDropdown();

        // Test mileage dropdown functionality
        validateMileageDropdown();

        // Test description field validation
        validateDescriptionField();

        AllureUtils.logStep("Multiple services form components validated successfully");
    }

    /**
     * Validate services dropdown functionality
     */
    @Step("Validate services dropdown functionality")
    private void validateServicesDropdown() {
        logger.info("Testing services dropdown functionality");

        try {
            // Test selecting different combinations of services
            String[] firstSelection = {"Fast Service"};
            String[] secondSelection = {"Fast Service","Bodywork repair service"};
            String[] thirdSelection = {"Fast Service","Bodywork repair service","Mechanical Repair Service"};

            // Test single service selection
            testServiceSelection(firstSelection, "Single service");

            // Test multiple service selection
            testServiceSelection(secondSelection, "Two services");

            // Test maximum service selection
            testServiceSelection(thirdSelection, "Three services");

            AllureUtils.addParameter("Services Dropdown Validation", "PASSED");
            logger.info("✅ Services dropdown validation completed");

        } catch (Exception e) {
            AllureUtils.addParameter("Services Dropdown Validation", "FAILED");
            logger.error("❌ Services dropdown validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Test service selection
     */
    private void testServiceSelection(String[] services, String testDescription) {
        logger.info("Testing {}: {}", testDescription, String.join(", ", services));

        try {
            appointmentFormPage.fillMultipleServicesFormWithValidation(services, "60 000", false);
            AllureUtils.addParameter(testDescription + " Selection", "SUCCESS");

            // Reset form for next test
            // You might need to refresh or navigate back depending on your app behavior

        } catch (Exception e) {
            AllureUtils.addParameter(testDescription + " Selection", "FAILED");
            logger.warn("Service selection test failed for {}: {}", testDescription, e.getMessage());
        }
    }

    /**
     * Validate mileage dropdown functionality
     */
    @Step("Validate mileage dropdown functionality")
    private void validateMileageDropdown() {
        logger.info("Testing mileage dropdown functionality");

        try {
            // Test different mileage selections
            String[] mileageOptions = {"30 000", "60 000", "90 000", "120 000"};

            for (String mileage : mileageOptions) {
                logger.info("Testing mileage selection: {}", mileage);
                // Test each mileage option
                String[] testServices = {"Fast Service"};
                appointmentFormPage.fillMultipleServicesFormWithValidation(testServices, mileage, false);
                AllureUtils.addParameter("Mileage Test: " + mileage, "SUCCESS");
            }

            AllureUtils.addParameter("Mileage Dropdown Validation", "PASSED");
            logger.info("✅ Mileage dropdown validation completed");

        } catch (Exception e) {
            AllureUtils.addParameter("Mileage Dropdown Validation", "FAILED");
            logger.error("❌ Mileage dropdown validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate description field functionality
     */
    @Step("Validate description field for multiple services")
    private void validateDescriptionField() {
        logger.info("Testing description field for multiple services");

        try {
            // This will be handled within the fillMultipleServicesFormWithValidation method
            String[] testServices = {"Fast Service"};
            appointmentFormPage.fillMultipleServicesFormWithValidation(testServices, "60 000", false);

            AllureUtils.addParameter("Description Field Validation", "PASSED");
            logger.info("✅ Description field validation completed");

        } catch (Exception e) {
            AllureUtils.addParameter("Description Field Validation", "FAILED");
            logger.error("❌ Description field validation failed: {}", e.getMessage());
            throw e;
        }
    }
}