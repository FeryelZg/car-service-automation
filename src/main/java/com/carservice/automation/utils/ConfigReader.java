package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private Properties properties;
    private Properties environmentProperties;

    public ConfigReader() {
        loadProperties();
    }

    /**
     * Load properties from config files
     */
    private void loadProperties() {
        properties = new Properties();
        environmentProperties = new Properties();

        try {
            // Load main config file
            loadMainConfig();

            // Load environment specific config
            loadEnvironmentConfig();

        } catch (IOException e) {
            logger.error("Failed to load configuration files: " + e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Load main configuration file
     */
    private void loadMainConfig() throws IOException {
        InputStream inputStream = null;
        try {
            // Try to load from classpath first
            inputStream = getClass().getClassLoader().getResourceAsStream("config/config.properties");

            if (inputStream == null) {
                // Try to load from file system
                inputStream = new FileInputStream("src/test/resources/config/config.properties");
            }

            properties.load(inputStream);
            logger.info("Main configuration loaded successfully");

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Load environment specific configuration
     */
    private void loadEnvironmentConfig() throws IOException {
        String environment = properties.getProperty("environment", "dev");
        String envConfigFile = "config/environments/" + environment + ".properties";

        InputStream inputStream = null;
        try {
            // Try to load from classpath first
            inputStream = getClass().getClassLoader().getResourceAsStream(envConfigFile);

            if (inputStream == null) {
                // Try to load from file system
                inputStream = new FileInputStream("src/test/resources/" + envConfigFile);
            }

            if (inputStream != null) {
                environmentProperties.load(inputStream);
                logger.info("Environment configuration loaded for: " + environment);
            } else {
                logger.warn("Environment configuration file not found: " + envConfigFile);
            }

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Get property value, check environment config first, then main config
     * @param key Property key
     * @return Property value or null if not found
     */
    public String getProperty(String key) {
        String value = environmentProperties.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key);
        }

        if (value == null) {
            logger.warn("Property not found: " + key);
        }

        return value;
    }

    /**
     * Get property value with default
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get property as integer
     * @param key Property key
     * @return Integer value
     */
    public int getIntProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property is not a valid integer: " + key + " = " + value);
        }
    }

    /**
     * Get property as boolean
     * @param key Property key
     * @return Boolean value
     */
    public boolean getBooleanProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property not found: " + key);
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Check if property exists
     * @param key Property key
     * @return true if property exists, false otherwise
     */
    public boolean hasProperty(String key) {
        return getProperty(key) != null;
    }
}