package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Enhanced ConfigReader utility class for handling configuration properties
 * Supports environment-specific configurations and system property overrides
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_DIR = "config/";
    private static final String MAIN_CONFIG_FILE = "config.properties";
    private static final String ENV_CONFIG_DIR = "environments/";

    private Properties properties;
    private Properties environmentProperties;
    private String currentEnvironment;

    public ConfigReader() {
        loadProperties();
    }

    /**
     * Load properties from config files with enhanced error handling
     */
    private void loadProperties() {
        properties = new Properties();
        environmentProperties = new Properties();

        try {
            loadMainConfig();
            loadEnvironmentConfig();
            logConfigurationSummary();
        } catch (IOException e) {
            logger.error("Failed to load configuration files: {}", e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Load main configuration file with multiple fallback locations
     */
    private void loadMainConfig() throws IOException {
        String configPath = CONFIG_DIR + MAIN_CONFIG_FILE;
        InputStream inputStream = null;

        try {
            // Try to load from classpath first
            inputStream = getClass().getClassLoader().getResourceAsStream(configPath);

            if (inputStream == null) {
                // Try to load from file system
                String filePath = "src/test/resources/" + configPath;
                inputStream = new FileInputStream(filePath);
                logger.debug("Loading config from file system: {}", filePath);
            } else {
                logger.debug("Loading config from classpath: {}", configPath);
            }

            properties.load(inputStream);
            logger.info("Main configuration loaded successfully from: {}", configPath);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Load environment specific configuration with fallback handling
     */
    private void loadEnvironmentConfig() throws IOException {
        currentEnvironment = getProperty("environment", "dev");
        String envConfigFile = CONFIG_DIR + ENV_CONFIG_DIR + currentEnvironment + ".properties";

        InputStream inputStream = null;
        try {
            // Try to load from classpath first
            inputStream = getClass().getClassLoader().getResourceAsStream(envConfigFile);

            if (inputStream == null) {
                // Try to load from file system
                String filePath = "src/test/resources/" + envConfigFile;
                try {
                    inputStream = new FileInputStream(filePath);
                    logger.debug("Loading environment config from file system: {}", filePath);
                } catch (Exception e) {
                    logger.warn("Environment configuration file not found: {} - using main config only", envConfigFile);
                    return;
                }
            } else {
                logger.debug("Loading environment config from classpath: {}", envConfigFile);
            }

            environmentProperties.load(inputStream);
            logger.info("Environment configuration loaded for: {}", currentEnvironment);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Get property value with priority: System Properties > Environment Config > Main Config
     * @param key Property key
     * @return Property value or null if not found
     */
    public String getProperty(String key) {
        // First check system properties (highest priority)
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            logger.debug("Property '{}' found in system properties: {}", key, systemValue);
            return systemValue;
        }

        // Then check environment-specific config
        String envValue = environmentProperties.getProperty(key);
        if (envValue != null) {
            logger.debug("Property '{}' found in environment config: {}", key, envValue);
            return envValue;
        }

        // Finally check main config
        String mainValue = properties.getProperty(key);
        if (mainValue != null) {
            logger.debug("Property '{}' found in main config: {}", key, mainValue);
            return mainValue;
        }

        logger.debug("Property not found: {}", key);
        return null;
    }

    /**
     * Get property value with default fallback
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return value;
        }

        logger.debug("Using default value for '{}': {}", key, defaultValue);
        return defaultValue;
    }

    /**
     * Get property as integer with enhanced error handling
     * @param key Property key
     * @return Integer value
     * @throws RuntimeException if property not found or invalid
     */
    public int getIntProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property is not a valid integer: " + key + " = " + value, e);
        }
    }

    /**
     * Get property as integer with default value
     * @param key Property key
     * @param defaultValue Default value if property not found or invalid
     * @return Integer value or default
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return getIntProperty(key);
        } catch (Exception e) {
            logger.debug("Using default integer value for '{}': {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get property as boolean with enhanced error handling
     * @param key Property key
     * @return Boolean value
     * @throws RuntimeException if property not found
     */
    public boolean getBooleanProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Get property as boolean with default value
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Boolean value or default
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            return getBooleanProperty(key);
        } catch (Exception e) {
            logger.debug("Using default boolean value for '{}': {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Check if property exists in any configuration source
     * @param key Property key
     * @return true if property exists, false otherwise
     */
    public boolean hasProperty(String key) {
        return getProperty(key) != null;
    }

    /**
     * Get current environment name
     * @return Current environment name
     */
    public String getCurrentEnvironment() {
        return currentEnvironment;
    }

    /**
     * Get all properties from main config (for debugging)
     * @return Properties object
     */
    public Properties getAllMainProperties() {
        return new Properties(properties);
    }

    /**
     * Get all properties from environment config (for debugging)
     * @return Properties object
     */
    public Properties getAllEnvironmentProperties() {
        return new Properties(environmentProperties);
    }

    /**
     * Log configuration summary for debugging
     */
    private void logConfigurationSummary() {
        logger.info("=== Configuration Summary ===");
        logger.info("Environment: {}", currentEnvironment);
        logger.info("Main properties loaded: {}", properties.size());
        logger.info("Environment properties loaded: {}", environmentProperties.size());

        // Log key configuration values (without sensitive data)
        String[] keyProperties = {"browser", "headless", "implicit.wait", "explicit.wait"};
        for (String key : keyProperties) {
            String value = getProperty(key);
            if (value != null) {
                logger.info("  {}: {}", key, value);
            }
        }
        logger.info("=============================");
    }

    /**
     * Refresh configuration (reload from files)
     */
    public void refresh() {
        logger.info("Refreshing configuration...");
        loadProperties();
    }
}