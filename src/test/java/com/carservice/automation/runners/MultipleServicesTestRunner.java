package com.carservice.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Dedicated Multiple Services Test Runner for multiple services appointment booking
 * Run only scenarios tagged with @multipleservices
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.carservice.automation.stepdefinitions",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/multiple-services-report.html",
                "json:target/cucumber-reports/json/MultipleServicesTest.json",
                "junit:target/cucumber-reports/xml/MultipleServicesTest.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        tags = "@multipleservices", // Only multiple services tests
        monochrome = true,
        publish = false,
        dryRun = false
)
public class MultipleServicesTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}