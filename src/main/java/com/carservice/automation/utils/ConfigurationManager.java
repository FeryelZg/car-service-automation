package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Centralized configuration manager for step definitions
 * Provides consistent access to configuration properties across all step definitions
 */
public class ConfigurationManager {

    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class);
    private static ConfigReader configReader;

    // Default URLs if config fails
    private static final String DEFAULT_ENDUSER_URL = "https://autoteam-dev.teamdev.tn/home";
    private static final String DEFAULT_BACKOFFICE_URL = "https://autoteam-bo-dev.teamdev.tn/auth";

    static {
        try {
            configReader = new ConfigReader();
            logger.info("✅ ConfigurationManager initialized successfully");
        } catch (Exception e) {
            logger.warn("⚠️ Failed to load configuration, using defaults: {}", e.getMessage());
            configReader = null;
        }
    }

    /**
     * Get configuration property with fallback to default
     */
    public static String getProperty(String key, String defaultValue) {
        try {
            if (configReader != null) {
                String value = configReader.getProperty(key);
                if (value != null && !value.trim().isEmpty()) {
                    logger.debug("📋 Config property '{}' = '{}'", key, value);
                    return value;
                }
            }

            logger.debug("📋 Using default for '{}': {}", key, defaultValue);
            return defaultValue;

        } catch (Exception e) {
            logger.warn("⚠️ Error reading config property '{}': {}", key, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Get system property with config fallback
     */
    public static String getSystemPropertyOrConfig(String propertyName, String configKey, String defaultValue) {
        // Check system property first (for runtime overrides)
        String systemValue = System.getProperty(propertyName);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            logger.info("📋 Using system property '{}' = '{}'", propertyName, systemValue);
            return systemValue;
        }

        // Fall back to config file
        return getProperty(configKey, defaultValue);
    }

    // ============================================================================
    // URL GETTERS - SINGLE SOURCE OF TRUTH
    // ============================================================================

    /**
     * Get End User application URL with environment detection
     */
    public static String getEndUserAppUrl() {
        String url = getProperty("enduser.app.url", DEFAULT_ENDUSER_URL);

        // Log the URL being used for debugging
        logger.info("🌐 End User App URL resolved to: {}", url);

        return url;
    }

    /**
     * Get Backoffice application URL with environment detection
     */
    public static String getBackofficeAppUrl() {
        String url = getProperty("backoffice.app.url", DEFAULT_BACKOFFICE_URL);

        // Log the URL being used for debugging
        logger.info("🏢 Backoffice App URL resolved to: {}", url);

        return url;
    }

    // ============================================================================
    // BROWSER CONFIGURATION
    // ============================================================================

    /**
     * Get browser name with system property override
     */
    public static String getBrowser() {
        return getSystemPropertyOrConfig("browser", "browser", "chrome");
    }

    /**
     * Get headless mode setting
     */
    public static boolean isHeadless() {
        String headless = getSystemPropertyOrConfig("headless", "headless", "false");
        return Boolean.parseBoolean(headless);
    }

    // ============================================================================
    // TEST DATA CONFIGURATION
    // ============================================================================

    /**
     * Get admin credentials
     */
    public static String getAdminUsername() {
        return getProperty("admin.username", "admin");
    }

    public static String getAdminPassword() {
        return getProperty("admin.password", "password");
    }

    /**
     * Get vehicle test data
     */
    public static String getVehiclePlateNumero() {
        return getProperty("vehicle.plate.numero", "1234");
    }

    public static String getVehiclePlateSerie() {
        return getProperty("vehicle.plate.serie", "ABC");
    }

    public static String getVehicleChassisNumber() {
        return getProperty("vehicle.chassis.number", "1234567");
    }

    public static String getVehicleMileage() {
        return getProperty("vehicle.mileage", "15000");
    }

    public static String getVehicleDescription() {
        return getProperty("vehicle.description", "Test vehicle description");
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    /**
     * Get page load timeout for heavy applications
     */
    public static int getPageLoadTimeout() {
        return getIntProperty("page.load.timeout", 60); // Reduced from 90 to 60
    }

    /**
     * Get whether to use fast page load detection
     */
    public static boolean useFastPageLoadDetection() {
        return getBooleanProperty("fast.page.load", true);
    }
    /**
     * Get boolean property
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    /**
     * Get integer property
     */
    public static int getIntProperty(String key, int defaultValue) {
        try {
            String value = getProperty(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("⚠️ Invalid integer value for '{}', using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Log current configuration for debugging
     */
    public static void logCurrentConfiguration() {
        logger.info("📋 === CURRENT CONFIGURATION ===");
        logger.info("🌐 End User URL: {}", getEndUserAppUrl());
        logger.info("🏢 Backoffice URL: {}", getBackofficeAppUrl());
        logger.info("🌐 Browser: {}", getBrowser());
        logger.info("👻 Headless: {}", isHeadless());
        logger.info("👤 Admin Username: {}", getAdminUsername());
        logger.info("🚗 Plate Numero: {}", getVehiclePlateNumero());

        // Check if URLs are reachable (basic validation)
        validateUrls();

        logger.info("📋 === END CONFIGURATION ===");
    }

    /**
     * Basic URL validation for debugging
     */
    private static void validateUrls() {
        try {
            String endUserUrl = getEndUserAppUrl();
            String backofficeUrl = getBackofficeAppUrl();

            // Basic URL format validation
            if (!endUserUrl.startsWith("http")) {
                logger.warn("⚠️ End User URL may be invalid: {}", endUserUrl);
            }
            if (!backofficeUrl.startsWith("http")) {
                logger.warn("⚠️ Backoffice URL may be invalid: {}", backofficeUrl);
            }

            logger.info("✅ URL format validation completed");

        } catch (Exception e) {
            logger.warn("⚠️ URL validation failed: {}", e.getMessage());
        }
    }
}