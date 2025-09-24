package com.fadhli.automation.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebDriver management utility
 * Provides centralized WebDriver instance management
 */
public class DriverProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverProvider.class);
    
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    /**
     * Browser types
     */
    public enum BrowserType {
        CHROME, FIREFOX
    }
    
    /**
     * Initialize WebDriver with default configuration (Chrome)
     */
    public static void initializeDriver() {
        initializeDriver(BrowserType.CHROME, false, 10);
    }
    
    /**
     * Initialize WebDriver with configuration
     */
    public static void initializeDriver(BrowserType browserType, boolean headless, int timeoutSeconds) {
        if (driverThreadLocal.get() != null) {
            logger.warn("WebDriver already initialized for current thread");
            return;
        }
        
        try {
            WebDriver driver;
            
            switch (browserType) {
                case CHROME -> driver = createChromeDriver(headless);
                case FIREFOX -> driver = createFirefoxDriver(headless);
                default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
            }
            
            driverThreadLocal.set(driver);
            logger.info("WebDriver initialized successfully: {} (headless: {})", browserType, headless);
            
        } catch (Exception e) {
            logger.error("Failed to initialize WebDriver", e);
            throw new RuntimeException("WebDriver initialization failed", e);
        }
    }
    
    /**
     * Get current WebDriver instance
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new RuntimeException("WebDriver not initialized. Call initializeDriver() first.");
        }
        return driver;
    }
    
    /**
     * Check if WebDriver is initialized
     */
    public static boolean isDriverInitialized() {
        return driverThreadLocal.get() != null;
    }
    
    /**
     * Quit WebDriver and cleanup
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.error("Error quitting WebDriver", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
    
    /**
     * Create Chrome WebDriver
     */
    private static WebDriver createChromeDriver(boolean headless) {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Standard Chrome options for stability
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        
        return new ChromeDriver(options);
    }
    
    /**
     * Create Firefox WebDriver
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
        }
        
        return new FirefoxDriver(options);
    }
}