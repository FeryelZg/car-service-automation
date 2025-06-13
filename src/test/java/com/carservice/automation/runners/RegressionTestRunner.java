package com.carservice.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Dedicated Regression Test Runner for comprehensive testing
 * Run only scenarios tagged with @regression
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.carservice.automation.stepdefinitions",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/regression-report.html",
                "json:target/cucumber-reports/json/RegressionTest.json",
                "junit:target/cucumber-reports/xml/RegressionTest.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "@regression", // Only regression tests
        monochrome = true,
        publish = false,
        dryRun = false
)
public class RegressionTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}