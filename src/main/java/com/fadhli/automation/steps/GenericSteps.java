package com.fadhli.automation.steps;

import com.fadhli.automation.util.ElementHelper;
import com.fadhli.automation.util.ScreenshotUtil;
import com.fadhli.automation.util.DriverProvider;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generic step definitions for Cucumber BDD automation
 * Provides common steps for web automation testing
 */
public class GenericSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericSteps.class);
    
    private WebDriver driver;
    
    @Before
    public void setup() {
        logger.info("Setting up WebDriver for test execution");
        DriverProvider.initializeDriver();
        driver = DriverProvider.getDriver();
        logStep("WebDriver initialized successfully");
    }
    
    @After
    public void tearDown(io.cucumber.java.Scenario scenario) {
        logStep("Test execution completed: " + scenario.getName());
        
        if (scenario.isFailed()) {
            logStep("Test failed - capturing screenshot");
            ScreenshotUtil.captureFailureScreenshot(driver, scenario.getName().replaceAll("\\s+", "_"));
        }
        
        DriverProvider.quitDriver();
        logStep("WebDriver closed successfully");
    }
    
    // =============================================================================
    // NAVIGATION STEPS
    // =============================================================================
    
    @Given("I open the page {string}")
    public void iOpenThePage(String url) {
        logStep("Open page: " + url);
        driver.get(url);
    }
    
    @Given("I navigate to {string}")
    public void iNavigateTo(String url) {
        logStep("Navigate to: " + url);
        driver.get(url);
    }
    
    @Given("I am on the page {string}")
    public void iAmOnThePage(String url) {
        logStep("Navigate to page: " + url);
        driver.get(url);
    }
    
    // =============================================================================
    // CLICK ACTIONS
    // =============================================================================
    
    @When("I click on element {string}")
    public void iClickOnElement(String locator) {
        logStep("Click element: " + locator);
        WebElement element = ElementHelper.findClickableElement(driver, locator);
        element.click();
    }
    
    @When("I click on {string}")
    public void iClickOn(String locator) {
        logStep("Click on: " + locator);
        WebElement element = ElementHelper.findClickableElement(driver, locator);
        element.click();
    }
    
    @When("I click the {string} button")
    public void iClickTheButton(String buttonText) {
        logStep("Click button: " + buttonText);
        // Try multiple strategies to find button
        WebElement element;
        try {
            // Try by text content first
            element = ElementHelper.findClickableElement(driver, "//button[contains(text(),'" + buttonText + "')]");
        } catch (Exception e) {
            try {
                // Try by value attribute
                element = ElementHelper.findClickableElement(driver, "//input[@type='button' and @value='" + buttonText + "']");
            } catch (Exception e2) {
                try {
                    // Try by aria-label
                    element = ElementHelper.findClickableElement(driver, "//*[@aria-label='" + buttonText + "']");
                } catch (Exception e3) {
                    // Fallback to generic button with text
                    element = ElementHelper.findClickableElement(driver, "//*[contains(text(),'" + buttonText + "')]");
                }
            }
        }
        element.click();
    }
    
    @When("I click the {string} link")
    public void iClickTheLink(String linkText) {
        logStep("Click link: " + linkText);
        WebElement element = ElementHelper.findClickableElement(driver, "linkText:" + linkText);
        element.click();
    }
    
    // =============================================================================
    // INPUT ACTIONS
    // =============================================================================
    
    @When("I type {string} into element {string}")
    public void iTypeIntoElement(String text, String locator) {
        logStep("Type '" + text + "' into: " + locator);
        WebElement element = ElementHelper.findElement(driver, locator);
        element.clear();
        element.sendKeys(text);
    }
    
    @When("I enter {string} into {string}")
    public void iEnterInto(String text, String locator) {
        logStep("Enter '" + text + "' into: " + locator);
        WebElement element = ElementHelper.findElement(driver, locator);
        element.clear();
        element.sendKeys(text);
    }
    
    @When("I fill {string} with {string}")
    public void iFillWith(String locator, String text) {
        logStep("Fill '" + locator + "' with: " + text);
        WebElement element = ElementHelper.findElement(driver, locator);
        element.clear();
        element.sendKeys(text);
    }
    
    @When("I clear the {string} field")
    public void iClearTheField(String locator) {
        logStep("Clear field: " + locator);
        WebElement element = ElementHelper.findElement(driver, locator);
        element.clear();
    }
    
    // =============================================================================
    // VERIFICATION STEPS
    // =============================================================================
    
    @Then("I should see text {string} on element {string}")
    public void iShouldSeeTextOnElement(String expectedText, String locator) {
        logStep("Verify text '" + expectedText + "' on: " + locator);
        WebElement element = ElementHelper.findVisibleElement(driver, locator);
        String actualText = element.getText();
        try {
            assertEquals(expectedText, actualText, "Text does not match on element: " + locator);
        } catch (AssertionError e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "text_verification_failed");
            logStep("Assertion failed: Expected '" + expectedText + "' but got '" + actualText + "'");
            throw e;
        }
    }
    
    @Then("I should see {string}")
    public void iShouldSee(String expectedText) {
        logStep("Verify page contains text: " + expectedText);
        try {
            WebElement element = ElementHelper.findElement(driver, "//*[contains(text(),'" + expectedText + "')]");
            assertTrue(element.isDisplayed(), "Text not visible: " + expectedText);
        } catch (Exception e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "text_not_found");
            logStep("Text not found on page: " + expectedText);
            throw new AssertionError("Text not found on page: " + expectedText, e);
        }
    }

    @Then("I should see {string} on element {string}")
    public void iShouldSeeOnElement(String expectedText, String locator) {
        logStep("Verify element " + locator + " contains text: " + expectedText);
        try {
            WebElement element = ElementHelper.findElement(driver, locator);
            String actualText = element.getText();
            assertTrue(actualText.contains(expectedText),
                    "Expected text '" + expectedText + "' not found on element '" + locator + "'");
        } catch (Exception e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "text_not_found");
            throw new AssertionError("Text not found on element: " + locator, e);
        }
    }
    
    @Then("the page title should be {string}")
    public void thePageTitleShouldBe(String expectedTitle) {
        logStep("Verify page title: " + expectedTitle);
        String actualTitle = driver.getTitle();
        try {
            assertEquals(expectedTitle, actualTitle, "Page title does not match");
        } catch (AssertionError e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "title_verification_failed");
            logStep("Title verification failed: Expected '" + expectedTitle + "' but got '" + actualTitle + "'");
            throw e;
        }
    }
    
    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expectedText) {
        logStep("Verify page title contains: " + expectedText);
        String actualTitle = driver.getTitle();
        try {
            assertTrue(actualTitle.contains(expectedText), 
                "Page title '" + actualTitle + "' does not contain '" + expectedText + "'");
        } catch (AssertionError e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "title_contains_failed");
            logStep("Title verification failed: '" + actualTitle + "' does not contain '" + expectedText + "'");
            throw e;
        }
    }
    
    @Then("element {string} should be visible")
    public void elementShouldBeVisible(String locator) {
        logStep("Verify element is visible: " + locator);
        try {
            WebElement element = ElementHelper.findVisibleElement(driver, locator);
            assertTrue(element.isDisplayed(), "Element is not visible: " + locator);
        } catch (Exception e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "element_not_visible");
            logStep("Element not visible: " + locator);
            throw new AssertionError("Element not visible: " + locator, e);
        }
    }
    
    @Then("element {string} should not be visible")
    public void elementShouldNotBeVisible(String locator) {
        logStep("Verify element is not visible: " + locator);
        try {
            boolean exists = ElementHelper.elementExists(driver, locator, 2);
            assertTrue(!exists || !ElementHelper.findElement(driver, locator).isDisplayed(), 
                "Element should not be visible: " + locator);
        } catch (Exception e) {
            // Element not found is expected, so this passes
            logStep("Element correctly not visible: " + locator);
        }
    }
    
    @Then("element {string} should contain text {string}")
    public void elementShouldContainText(String locator, String expectedText) {
        logStep("Verify element '" + locator + "' contains text: " + expectedText);
        WebElement element = ElementHelper.findElement(driver, locator);
        String actualText = element.getText();
        try {
            assertTrue(actualText.contains(expectedText), 
                "Element text '" + actualText + "' does not contain '" + expectedText + "'");
        } catch (AssertionError e) {
            ScreenshotUtil.captureFailureScreenshot(driver, "text_contains_failed");
            logStep("Text verification failed: '" + actualText + "' does not contain '" + expectedText + "'");
            throw e;
        }
    }
    
    // =============================================================================
    // WAIT STEPS
    // =============================================================================
    
    @Then("I wait for {int} seconds")
    public void iWaitForSeconds(int seconds) throws InterruptedException {
        logStep("Wait for " + seconds + " seconds");
        Thread.sleep(seconds * 1000L);
    }
    
    @When("I wait for {int} milliseconds")
    public void iWaitForMilliseconds(int milliseconds) throws InterruptedException {
        logStep("Wait for " + milliseconds + " milliseconds");
        Thread.sleep(milliseconds);
    }
    
    @Then("I wait for element {string} to be visible")
    public void iWaitForElementToBeVisible(String locator) {
        logStep("Wait for element to be visible: " + locator);
        ElementHelper.findVisibleElement(driver, locator, 30); // 30 second timeout
    }
    
    @Then("I wait for element {string} to be clickable")
    public void iWaitForElementToBeClickable(String locator) {
        logStep("Wait for element to be clickable: " + locator);
        ElementHelper.findClickableElement(driver, locator, 30); // 30 second timeout
    }
    
    // =============================================================================
    // BROWSER ACTIONS
    // =============================================================================
    
    @When("I refresh the page")
    public void iRefreshThePage() {
        logStep("Refresh the page");
        driver.navigate().refresh();
    }
    
    @When("I go back")
    public void iGoBack() {
        logStep("Navigate back");
        driver.navigate().back();
    }
    
    @When("I go forward")
    public void iGoForward() {
        logStep("Navigate forward");
        driver.navigate().forward();
    }
    
    @When("I maximize the window")
    public void iMaximizeTheWindow() {
        logStep("Maximize browser window");
        driver.manage().window().maximize();
    }
    
    // =============================================================================
    // UTILITY METHODS
    // =============================================================================
    
    /**
     * Log step execution with consistent formatting
     */
    private void logStep(String message) {
        String logMessage = "🔹 STEP: " + message;
        System.out.println(logMessage);
        logger.info(message);
    }
    
    /**
     * Get current WebDriver instance (for advanced steps)
     */
    protected WebDriver getDriver() {
        return driver;
    }
}