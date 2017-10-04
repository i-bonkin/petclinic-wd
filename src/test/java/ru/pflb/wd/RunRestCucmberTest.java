package ru.pflb.wd;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:8445322@gmail.com">Ivan Bonkin</a>.
 */
@RunWith(Cucumber.class)
@CucumberOptions(monochrome = true,
        tags = "@all",
        features = "src/test/resources/ru/pflb/wd",
        plugin = "json:target/cucumber-report.json")
public class RunRestCucmberTest {
}
