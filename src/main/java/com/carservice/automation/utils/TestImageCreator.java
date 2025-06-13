package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to create test images for file upload testing
 */
public class TestImageCreator {

    private static final Logger logger = LogManager.getLogger(TestImageCreator.class);

    /**
     * Create a test image file if it doesn't exist
     * @param fileName Name of the image file
     * @return Path to the created image file
     */
    public static String createTestImage(String fileName) {
        try {
            // Try multiple possible locations
            String[] possiblePaths = {
                    "src/test/resources/" + fileName,
                    "test-classes/" + fileName,
                    System.getProperty("user.dir") + "/src/test/resources/" + fileName,
                    System.getProperty("java.io.tmpdir") + "/" + fileName
            };

            for (String pathStr : possiblePaths) {
                Path path = Paths.get(pathStr);

                // Create parent directories if they don't exist
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }

                // Create the image file if it doesn't exist
                if (!Files.exists(path)) {
                    createSimpleTestImage(path.toString());
                }

                // Check if file exists and is readable
                if (Files.exists(path) && Files.isReadable(path)) {
                    logger.info("✅ Test image found/created at: {}", path.toAbsolutePath());
                    return path.toAbsolutePath().toString();
                }
            }

            // If all else fails, create in temp directory
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            createSimpleTestImage(tempPath);
            logger.info("✅ Test image created in temp directory: {}", tempPath);
            return tempPath;

        } catch (Exception e) {
            logger.error("❌ Failed to create test image: {}", e.getMessage());
            throw new RuntimeException("Could not create test image", e);
        }
    }

    /**
     * Create a simple test image with text
     * @param filePath Full path where to create the image
     */
    private static void createSimpleTestImage(String filePath) throws IOException {
        int width = 400;
        int height = 300;

        // Create a BufferedImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Set background color
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set text properties
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        // Add text
        String text = "TEST IMAGE";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int x = (width - textWidth) / 2;
        int y = (height - textHeight) / 2 + fm.getAscent();

        g2d.drawString(text, x, y);

        // Add border
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(5, 5, width - 10, height - 10);

        // Add timestamp
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String timestamp = "Created: " + java.time.LocalDateTime.now().toString();
        g2d.drawString(timestamp, 10, height - 10);

        g2d.dispose();

        // Save the image
        File file = new File(filePath);
        ImageIO.write(bufferedImage, "png", file);

        logger.info("✅ Test image created: {} ({}x{} pixels)", filePath, width, height);
    }

    /**
     * Get the correct file path for the test image based on execution context
     * @param fileName Name of the image file
     * @return Absolute path to the image file
     */
    public static String getTestImagePath(String fileName) {
        // First try to find existing file
        String[] possiblePaths = {
                "src/test/resources/" + fileName,
                System.getProperty("user.dir") + "/src/test/resources/" + fileName,
                System.getProperty("user.dir") + "/target/test-classes/" + fileName,
                TestImageCreator.class.getClassLoader().getResource(fileName) != null ?
                        TestImageCreator.class.getClassLoader().getResource(fileName).getPath() : null
        };

        for (String pathStr : possiblePaths) {
            if (pathStr != null) {
                File file = new File(pathStr);
                if (file.exists() && file.canRead()) {
                    logger.info("📁 Found existing test image: {}", file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }
        }

        // If not found, create it
        logger.info("📁 Test image not found, creating new one: {}", fileName);
        return createTestImage(fileName);
    }
}