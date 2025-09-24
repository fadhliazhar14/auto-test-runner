package com.fadhli.automation.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Utility class for finding web elements with various strategies
 * Supports multiple locator types and provides smart element detection
 */
public class ElementHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ElementHelper.class);
    
    private static final int DEFAULT_TIMEOUT = 10;
    
    /**
     * Find element with smart locator detection
     * Supports: id, name, xpath, css, class, tag, linkText, partialLinkText
     */
    public static WebElement findElement(WebDriver driver, String locator) {
        return findElement(driver, locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Find element with timeout
     */
    public static WebElement findElement(WebDriver driver, String locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        By by = parseLocator(locator);
        
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
            logger.debug("Found element with locator: {}", locator);
            return element;
        } catch (Exception e) {
            logger.error("Failed to find element with locator: {}", locator, e);
            throw new RuntimeException("Element not found: " + locator, e);
        }
    }
    
    /**
     * Find clickable element
     */
    public static WebElement findClickableElement(WebDriver driver, String locator) {
        return findClickableElement(driver, locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Find clickable element with timeout
     */
    public static WebElement findClickableElement(WebDriver driver, String locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        By by = parseLocator(locator);
        
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(by));
            logger.debug("Found clickable element with locator: {}", locator);
            return element;
        } catch (Exception e) {
            logger.error("Failed to find clickable element with locator: {}", locator, e);
            throw new RuntimeException("Clickable element not found: " + locator, e);
        }
    }
    
    /**
     * Find visible element
     */
    public static WebElement findVisibleElement(WebDriver driver, String locator) {
        return findVisibleElement(driver, locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Find visible element with timeout
     */
    public static WebElement findVisibleElement(WebDriver driver, String locator, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        By by = parseLocator(locator);
        
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            logger.debug("Found visible element with locator: {}", locator);
            return element;
        } catch (Exception e) {
            logger.error("Failed to find visible element with locator: {}", locator, e);
            throw new RuntimeException("Visible element not found: " + locator, e);
        }
    }
    
    /**
     * Check if element exists
     */
    public static boolean elementExists(WebDriver driver, String locator) {
        return elementExists(driver, locator, 2); // Short timeout for existence check
    }
    
    /**
     * Check if element exists with timeout
     */
    public static boolean elementExists(WebDriver driver, String locator, int timeoutSeconds) {
        try {
            findElement(driver, locator, timeoutSeconds);
            return true;
        } catch (Exception e) {
            logger.debug("Element does not exist: {}", locator);
            return false;
        }
    }
    
    /**
     * Parse locator string to By object
     * Supports various locator strategies with prefixes:
     * - id: or #
     * - name: or [name=]
     * - xpath: or //
     * - css: or .class or [attribute]
     * - class: or .
     * - tag:
     * - linkText:
     * - partialLinkText:
     * - Default: tries id, name, class, then XPath
     */
    private static By parseLocator(String locator) {
        if (locator == null || locator.trim().isEmpty()) {
            throw new IllegalArgumentException("Locator cannot be null or empty");
        }
        
        String trimmed = locator.trim();
        
        // XPath
        if (trimmed.startsWith("xpath:") || trimmed.startsWith("//") || trimmed.startsWith("(//")) {
            String xpath = trimmed.startsWith("xpath:") ? trimmed.substring(6) : trimmed;
            logger.debug("Using XPath locator: {}", xpath);
            return By.xpath(xpath);
        }
        
        // CSS Selector
        if (trimmed.startsWith("css:") || trimmed.startsWith(".") || trimmed.contains("[") || trimmed.contains(">")) {
            String css = trimmed.startsWith("css:") ? trimmed.substring(4) : trimmed;
            logger.debug("Using CSS locator: {}", css);
            return By.cssSelector(css);
        }
        
        // ID
        if (trimmed.startsWith("id:") || trimmed.startsWith("#")) {
            String id = trimmed.startsWith("id:") ? trimmed.substring(3) : trimmed.substring(1);
            logger.debug("Using ID locator: {}", id);
            return By.id(id);
        }
        
        // Name
        if (trimmed.startsWith("name:")) {
            String name = trimmed.substring(5);
            logger.debug("Using Name locator: {}", name);
            return By.name(name);
        }
        
        // Class Name
        if (trimmed.startsWith("class:")) {
            String className = trimmed.substring(6);
            logger.debug("Using ClassName locator: {}", className);
            return By.className(className);
        }
        
        // Tag Name
        if (trimmed.startsWith("tag:")) {
            String tagName = trimmed.substring(4);
            logger.debug("Using TagName locator: {}", tagName);
            return By.tagName(tagName);
        }
        
        // Link Text
        if (trimmed.startsWith("linkText:")) {
            String linkText = trimmed.substring(9);
            logger.debug("Using LinkText locator: {}", linkText);
            return By.linkText(linkText);
        }
        
        // Partial Link Text
        if (trimmed.startsWith("partialLinkText:")) {
            String partialLinkText = trimmed.substring(16);
            logger.debug("Using PartialLinkText locator: {}", partialLinkText);
            return By.partialLinkText(partialLinkText);
        }
        
        // Auto-detect strategy for plain strings
        return autoDetectLocator(trimmed);
    }
    
    /**
     * Auto-detect locator strategy for plain strings
     */
    private static By autoDetectLocator(String locator) {
        // Try ID first (most common and fastest)
        if (isValidId(locator)) {
            logger.debug("Auto-detected as ID locator: {}", locator);
            return By.id(locator);
        }
        
        // Try name
        if (isValidName(locator)) {
            logger.debug("Auto-detected as Name locator: {}", locator);
            return By.name(locator);
        }
        
        // Try class name
        if (isValidClassName(locator)) {
            logger.debug("Auto-detected as ClassName locator: {}", locator);
            return By.className(locator);
        }
        
        // Default to XPath (most flexible but slowest)
        String xpath = "//*[contains(@id,'" + locator + "') or contains(@name,'" + locator + 
                      "') or contains(@class,'" + locator + "') or contains(text(),'" + locator + "')]";
        logger.debug("Auto-detected as XPath locator: {}", xpath);
        return By.xpath(xpath);
    }
    
    /**
     * Check if string looks like a valid ID
     */
    private static boolean isValidId(String value) {
        // IDs typically don't contain spaces or special CSS characters
        return !value.contains(" ") && !value.contains(".") && !value.contains("[") && !value.contains(">");
    }
    
    /**
     * Check if string looks like a valid name
     */
    private static boolean isValidName(String value) {
        // Names typically don't contain spaces or special CSS characters  
        return !value.contains(" ") && !value.contains(".") && !value.contains("[") && !value.contains(">");
    }
    
    /**
     * Check if string looks like a valid class name
     */
    private static boolean isValidClassName(String value) {
        // Class names typically don't contain spaces (use CSS for compound classes)
        return !value.contains(" ") && !value.contains("[") && !value.contains(">");
    }
}