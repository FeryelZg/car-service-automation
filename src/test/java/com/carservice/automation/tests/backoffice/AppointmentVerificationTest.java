//package com.carservice.automation.tests.backoffice;
//
//import com.carservice.automation.base.BaseTest;
//import com.carservice.automation.pages.backoffice.BackofficeLoginPage;
//import com.carservice.automation.pages.backoffice.WorkspaceSelectionPage;
//import com.carservice.automation.pages.backoffice.InterventionsPage;
//import com.carservice.automation.utils.AllureUtils;
//import io.qameta.allure.*;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
///**
// * Test class for verifying appointments appear in the backoffice
// * This test validates the complete E2E flow from appointment creation to backoffice verification
// */
//@Epic("Vehicle Appointment Booking")
//@Feature("Backoffice Appointment Verification")
//public class AppointmentVerificationTest extends BaseTest {
//
//    private static final Logger logger = LogManager.getLogger(AppointmentVerificationTest.class);
//
//    // Page Objects
//    private BackofficeLoginPage loginPage;
//    private WorkspaceSelectionPage workspacePage;
//    private InterventionsPage interventionsPage;
//
//    @Test(groups = {"e2e", "backoffice", "calendar", "scheduling"})
//    @Story("Schedule Intervention in Calendar")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Validates scheduling an intervention by dragging it to a calendar time slot")
//    @Link(name = "Calendar Scheduling Test Case", url = "https://your-test-management-tool.com/testcase/126")
//    @Issue("APP-459")
//    @TmsLink("TC-004")
//    public void testInterventionSchedulingInCalendar() {
//        AllureUtils.startTestCase("Intervention Calendar Scheduling Test");
//
//        try {
//            initializeBackofficeTestEnvironment();
//            performBackofficeLogin();
//            selectWorkspaceAndEnterDashboard();
//            scheduleInterventionInCalendar("11:00");
//
//            AllureUtils.addTestResult("Intervention scheduling in calendar completed successfully");
//            logger.info("Intervention scheduling test completed successfully");
//
//        } catch (Exception e) {
//            AllureUtils.addFailureInfo("Intervention scheduling test failed", e);
//            logger.error("Intervention scheduling test failed: {}", e.getMessage());
//            takeScreenshot("ERROR_InterventionScheduling");
//            throw new RuntimeException("Intervention scheduling failed", e);
//        }
//    }
//
//    @Test(groups = {"e2e", "full_flow", "calendar"})
//    @Story("Complete E2E Flow with Calendar Scheduling")
//    @Severity(SeverityLevel.BLOCKER)
//    @Description("Complete flow: Create appointment → Verify in backoffice → Schedule in calendar")
//    @Link(name = "Complete Calendar Flow", url = "https://your-test-management-tool.com/testcase/127")
//    @Issue("APP-460")
//    @TmsLink("TC-005")
//    public void testCompleteE2EFlowWithCalendarScheduling() {
//        AllureUtils.startTestCase("Complete E2E Flow with Calendar Scheduling");
//
//        try {
//            // This test assumes an appointment was already created by the previous test
//            AllureUtils.logStep("Starting complete E2E flow with calendar scheduling");
//
//            initializeBackofficeTestEnvironment();
//            performBackofficeLogin();
//            selectWorkspaceAndEnterDashboard();
//
//            // Verify intervention exists
//            verifyAppointmentInInterventions();
//
//            // Schedule intervention in calendar
//            scheduleInterventionInCalendar("11:00");
//
//            AllureUtils.addTestResult("Complete E2E flow with calendar scheduling completed successfully");
//            AllureUtils.addParameter("Flow Type", "Creation → Verification → Calendar Scheduling");
//            AllureUtils.addParameter("Scheduled Time", "11:00");
//
//            logger.info("Complete E2E flow with calendar scheduling completed successfully");
//
//        } catch (Exception e) {
//            AllureUtils.addFailureInfo("Complete E2E calendar flow failed", e);
//            logger.error("Complete E2E calendar flow failed: {}", e.getMessage());
//            takeScreenshot("ERROR_CompleteE2ECalendarFlow");
//            throw new RuntimeException("Complete E2E calendar flow failed", e);
//        }
//    }
//
//    @Test(groups = {"e2e", "backoffice", "appointment_verification"})
//    @Story("Verify Appointment Appears in Backoffice")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Validates that appointments created in the end-user app appear correctly in the backoffice interventions")
//    @Link(name = "Backoffice Test Case", url = "https://your-test-management-tool.com/testcase/124")
//    @Issue("APP-457")
//    @TmsLink("TC-002")
//    public void testAppointmentVerificationInBackoffice() {
//        AllureUtils.startTestCase("Backoffice Appointment Verification Test");
//
//        try {
//            initializeBackofficeTestEnvironment();
//            performBackofficeLogin();
//            selectWorkspaceAndEnterDashboard();
//            verifyAppointmentInInterventions();
//
//            AllureUtils.addTestResult("Appointment verification in backoffice completed successfully");
//            logger.info("Appointment verification test completed successfully");
//
//        } catch (Exception e) {
//            AllureUtils.addFailureInfo("Backoffice verification test failed", e);
//            logger.error("Backoffice verification test failed: {}", e.getMessage());
//            takeScreenshot("ERROR_BackofficeVerification");
//            throw new RuntimeException("Backoffice appointment verification failed", e);
//        }
//    }
//
//    @Test(groups = {"smoke", "backoffice", "login"})
//    @Story("Backoffice Login Validation")
//    @Severity(SeverityLevel.CRITICAL)
//    @Description("Validates backoffice login functionality with input validation tests")
//    public void testBackofficeLoginValidation() {
//        AllureUtils.startTestCase("Backoffice Login Validation Test");
//
//        try {
//            initializeBackofficeTestEnvironment();
//            performDetailedLoginValidation();
//
//            AllureUtils.addTestResult("Backoffice login validation completed successfully");
//            logger.info("Backoffice login validation test completed successfully");
//
//        } catch (Exception e) {
//            AllureUtils.addFailureInfo("Backoffice login validation failed", e);
//            logger.error("Backoffice login validation failed: {}", e.getMessage());
//            takeScreenshot("ERROR_BackofficeLogin");
//            throw new RuntimeException("Backoffice login validation failed", e);
//        }
//    }
//
//    /**
//     * Quick verification test (without detailed validation)
//     * Useful for regression testing
//     */
//    @Test(groups = {"regression", "quick_check"})
//    @Story("Quick Appointment Verification")
//    @Severity(SeverityLevel.NORMAL)
//    @Description("Quick verification that appointments appear in backoffice without detailed validation")
//    public void testQuickAppointmentVerification() {
//        AllureUtils.startTestCase("Quick Appointment Verification Test");
//
//        try {
//            initializeBackofficePageObjects();
//            navigateToBackofficeApp();
//
//            loginPage.quickLogin();
//            workspacePage.selectHavalWorkspace();
//            workspacePage.clickStartButton();
//
//            boolean appointmentFound = interventionsPage.completeAppointmentVerification();
//            Assert.assertTrue(appointmentFound, "Appointment should be found in backoffice");
//
//            AllureUtils.addTestResult("Quick appointment verification completed");
//            AllureUtils.addParameter("Verification Type", "QUICK CHECK");
//
//            logger.info("Quick appointment verification completed successfully");
//
//        } catch (Exception e) {
//            AllureUtils.addFailureInfo("Quick verification failed", e);
//            logger.error("Quick verification failed: {}", e.getMessage());
//            takeScreenshot("ERROR_QuickVerification");
//            throw new RuntimeException("Quick appointment verification failed", e);
//        }
//    }
//
//
//    @Step("Initialize backoffice test environment")
//    private void initializeBackofficeTestEnvironment() {
//        AllureUtils.addEnvironmentInfo();
//        AllureUtils.addTestData();
//
//        initializeBackofficePageObjects();
//        navigateToBackoffice();
//    }
//
//    @Step("Initialize backoffice page objects")
//    private void initializeBackofficePageObjects() {
//        loginPage = new BackofficeLoginPage(driver);
//        workspacePage = new WorkspaceSelectionPage(driver);
//        interventionsPage = new InterventionsPage(driver);
//
//        AllureUtils.logStep("Backoffice page objects initialized successfully");
//    }
//
//    @Step("Navigate to backoffice application")
//    private void navigateToBackoffice() {
//        navigateToBackofficeApp();
//
//        // Add debug step to help troubleshoot login page issues
//        AllureUtils.logStep("Checking if login page loaded correctly");
//        AllureUtils.attachScreenshot("Backoffice page loaded");
//
//        // Add a brief wait for page to fully load
//        waitForElement(3000);
//
//        try {
//            loginPage.verifyLoginPageLoaded();
//        } catch (Exception e) {
//            logger.error("Login page verification failed, running debug analysis");
//            loginPage.debugLoginPageElements();
//            throw e;
//        }
//
//        AllureUtils.logStep("Successfully navigated to backoffice application");
//    }
//
//    /**
//     * Helper method for waiting (if not available in BaseTest)
//     */
//    private void waitForElement(long milliseconds) {
//        try {
//            Thread.sleep(milliseconds);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            logger.warn("Wait interrupted: {}", e.getMessage());
//        }
//    }
//
//    @Step("Perform backoffice login")
//    private void performBackofficeLogin() {
//        AllureUtils.logStep("Starting backoffice login process");
//
//        loginPage.quickLogin();
//        workspacePage.verifyWorkspacePageLoaded();
//
//        AllureUtils.logStep("Backoffice login completed successfully");
//    }
//
//    @Step("Perform detailed login validation")
//    private void performDetailedLoginValidation() {
//        AllureUtils.logStep("Starting detailed login validation");
//
//        loginPage.loginWithValidation();
//        workspacePage.verifyWorkspacePageLoaded();
//
//        AllureUtils.logStep("Detailed login validation completed successfully");
//    }
//
//    @Step("Select workspace and enter dashboard")
//    private void selectWorkspaceAndEnterDashboard() {
//        AllureUtils.logStep("Starting workspace selection process");
//
//        workspacePage.completeWorkspaceSelection();
//        workspacePage.verifyWorkspaceDashboardLoaded();
//
//        AllureUtils.logStep("Workspace selection completed successfully");
//    }
//
//    @Step("Verify appointment in interventions")
//    private void verifyAppointmentInInterventions() {
//        AllureUtils.logStep("Starting appointment verification in interventions");
//
//        boolean appointmentFound = interventionsPage.completeAppointmentVerification();
//
//        Assert.assertTrue(appointmentFound,
//                "Appointment should be found in backoffice interventions");
//
//        // Get detailed appointment information
//        InterventionsPage.AppointmentDetails details = interventionsPage.getAppointmentDetails();
//        if (details != null) {
//            validateAppointmentDetails(details);
//        }
//
//        AllureUtils.logStep("Appointment verification completed successfully");
//    }
//
//    @Step("Schedule intervention in calendar")
//    private void scheduleInterventionInCalendar(String timeSlot) {
//        AllureUtils.logStep("Starting intervention calendar scheduling");
//
//        boolean schedulingSuccessful = interventionsPage.completeInterventionManagementFlow(timeSlot);
//
//        Assert.assertTrue(schedulingSuccessful,
//                         "Intervention should be successfully scheduled in calendar at time slot: " + timeSlot);
//
//        AllureUtils.addParameter("Scheduling Success", "TRUE");
//        AllureUtils.addParameter("Time Slot", timeSlot);
//        AllureUtils.logStep("Intervention successfully scheduled in calendar");
//    }
//
//    @Step("Validate appointment details match expected values")
//    private void validateAppointmentDetails(InterventionsPage.AppointmentDetails details) {
//        AllureUtils.logStep("Validating appointment details");
//
//        // Get expected values from config
//        String expectedPlate = config.getProperty("vehicle.plate.numero") + "TU" + config.getProperty("vehicle.plate.serie");
//        String expectedMileage = config.getProperty("vehicle.mileage");
//
//        // Validate plate number
//        Assert.assertTrue(details.plateNumber.contains(expectedPlate),
//                         String.format("Plate number should contain %s, but found %s", expectedPlate, details.plateNumber));
//
//        // Validate mileage
//        Assert.assertTrue(details.mileage.contains(expectedMileage),
//                         String.format("Mileage should contain %s, but found %s", expectedMileage, details.mileage));
//
//        // Validate breakdown status (should be true based on our test)
//        Assert.assertTrue(details.hasBreakdownStatus,
//                         "Appointment should have breakdown status");
//
//        // Validate date and time are present
//        Assert.assertNotNull(details.date, "Appointment date should be present");
//        Assert.assertNotNull(details.time, "Appointment time should be present");
//
//        AllureUtils.addParameter("Validated Plate Number", details.plateNumber);
//        AllureUtils.addParameter("Validated Mileage", details.mileage);
//        AllureUtils.addParameter("Validated Date", details.date);
//        AllureUtils.addParameter("Validated Time", details.time);
//        AllureUtils.addParameter("Validated Breakdown Status", String.valueOf(details.hasBreakdownStatus));
//
//        AllureUtils.logStep("All appointment details validated successfully");
//
//        logger.info("Appointment details validation completed - Plate: {}, Mileage: {}, Date: {}, Time: {}",
//                   details.plateNumber, details.mileage, details.date, details.time);
//    }
//
//
//}