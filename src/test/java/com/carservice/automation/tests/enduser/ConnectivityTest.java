package com.carservice.automation.tests.enduser;

import com.carservice.automation.base.BaseTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ConnectivityTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(ConnectivityTest.class);

    @Test(groups = {"smoke", "connectivity"}, priority = 1,
            description = "Verify AutoTeam end user application is accessible")
    public void testEndUserAppConnectivity() {
        logger.info("🧪 Testing End User App Connectivity");

        try {
            navigateToEndUserApp();

            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();

            logger.info("📍 Current URL: {}", currentUrl);
            logger.info("📄 Page Title: {}", pageTitle);

            // Basic accessibility assertions
            Assert.assertTrue(currentUrl.contains("autoteam"),
                    "URL should contain 'autoteam', actual: " + currentUrl);
            Assert.assertNotNull(pageTitle, "Page title should not be null");
            Assert.assertFalse(pageTitle.trim().isEmpty(), "Page title should not be empty");

            // Verify page has interactive elements
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            List<WebElement> links = driver.findElements(By.tagName("a"));

            int totalElements = buttons.size() + links.size();
            logger.info("📊 Found {} interactive elements (buttons: {}, links: {})",
                    totalElements, buttons.size(), links.size());

            Assert.assertTrue(totalElements > 0, "Page should have interactive elements");

            logger.info("✅ End User App connectivity test PASSED");

        } catch (Exception e) {
            logger.error("❌ End User App connectivity failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(groups = {"smoke", "connectivity"}, priority = 2,
            description = "Verify AutoTeam backoffice application is accessible")
    public void testBackofficeAppConnectivity() {
        logger.info("🧪 Testing Backoffice App Connectivity");

        try {
            navigateToBackofficeApp();

            String currentUrl = driver.getCurrentUrl();
            String pageTitle = driver.getTitle();

            logger.info("📍 Current URL: {}", currentUrl);
            logger.info("📄 Page Title: {}", pageTitle);

            // Basic accessibility assertions
            Assert.assertTrue(currentUrl.contains("autoteam"),
                    "URL should contain 'autoteam', actual: " + currentUrl);
            Assert.assertNotNull(pageTitle, "Page title should not be null");
            Assert.assertFalse(pageTitle.trim().isEmpty(), "Page title should not be empty");

            // Look for authentication-related elements (forms, inputs, buttons)
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            List<WebElement> buttons = driver.findElements(By.tagName("button"));

            logger.info("📊 Found authentication elements (forms: {}, inputs: {}, buttons: {})",
                    forms.size(), inputs.size(), buttons.size());

            // Backoffice should have some authentication interface
            Assert.assertTrue(inputs.size() > 0 || buttons.size() > 0,
                    "Backoffice should have authentication interface");

            logger.info("✅ Backoffice App connectivity test PASSED");

        } catch (Exception e) {
            logger.error("❌ Backoffice App connectivity failed: {}", e.getMessage());
            throw e;
        }
    }

    @Test(groups = {"smoke", "connectivity"}, priority = 3,
            description = "Comprehensive health check for both applications",
            dependsOnMethods = {"testEndUserAppConnectivity", "testBackofficeAppConnectivity"})
    public void testOverallSystemHealth() {
        logger.info("🧪 Running Overall System Health Check");

        try {
            // Test both applications in sequence

            // 1. Test End User App
            navigateToEndUserApp();
            String endUserUrl = driver.getCurrentUrl();
            String endUserTitle = driver.getTitle();

            boolean endUserHealthy = isApplicationHealthy(endUserUrl, endUserTitle);
            logger.info("End User App Health: {}", endUserHealthy ? "✅ HEALTHY" : "⚠️ ISSUES");

            // 2. Test Backoffice App
            navigateToBackofficeApp();
            String backofficeUrl = driver.getCurrentUrl();
            String backofficeTitle = driver.getTitle();

            boolean backofficeHealthy = isApplicationHealthy(backofficeUrl, backofficeTitle);
            logger.info("Backoffice App Health: {}", backofficeHealthy ? "✅ HEALTHY" : "⚠️ ISSUES");

            // 3. Verify applications are different
            Assert.assertNotEquals(endUserUrl, backofficeUrl,
                    "End user and backoffice should have different URLs");

            // 4. Overall health assessment
            boolean overallHealthy = endUserHealthy && backofficeHealthy;
            Assert.assertTrue(overallHealthy,
                    "Both AutoTeam applications should be healthy and accessible");

            logger.info("🎉 Overall System Health: ✅ ALL SYSTEMS OPERATIONAL");

        } catch (Exception e) {
            logger.error("❌ System health check failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if application appears healthy based on URL and title
     */
    private boolean isApplicationHealthy(String url, String title) {
        return url != null && !url.contains("error") &&
                title != null && !title.toLowerCase().contains("error") &&
                !title.toLowerCase().contains("not found") &&
                title.trim().length() > 0;
    }
}