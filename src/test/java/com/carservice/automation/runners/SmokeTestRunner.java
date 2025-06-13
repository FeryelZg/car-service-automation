package com.carservice.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Dedicated Smoke Test Runner for quick smoke testing
 * Run only scenarios tagged with @smoke
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.carservice.automation.stepdefinitions",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/smoke-report.html",
                "json:target/cucumber-reports/json/SmokeTest.json",
                "junit:target/cucumber-reports/xml/SmokeTest.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "@smoke", // Only smoke tests
        monochrome = true,
        publish = false,
        dryRun = false
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}