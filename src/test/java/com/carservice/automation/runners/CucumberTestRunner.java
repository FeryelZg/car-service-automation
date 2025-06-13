package com.carservice.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber Test Runner with dynamic tag support
 * Supports system property override for tags
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.carservice.automation.stepdefinitions",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/html-report.html",
                "json:target/cucumber-reports/json/Cucumber.json",
                "junit:target/cucumber-reports/xml/Cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "@smoke or @regression", // Default tags - can be overridden by system property
        monochrome = true,
        publish = false,
        dryRun = false
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    /**
     * This method enables parallel execution of scenarios
     * Set parallel = true for parallel execution
     * Keep parallel = false for sequential execution (recommended for debugging)
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    /**
     * Static block to handle dynamic tag configuration
     * This allows overriding tags via system properties
     */
    static {
        String tagsFromProperty = System.getProperty("cucumber.filter.tags");
        if (tagsFromProperty != null && !tagsFromProperty.trim().isEmpty()) {
            System.out.println("🏷️ Using tags from system property: " + tagsFromProperty);
            // Note: In TestNG+Cucumber, tags are set at compile time via annotation
            // For runtime tag selection, use separate runner classes or profiles
        }
    }
}