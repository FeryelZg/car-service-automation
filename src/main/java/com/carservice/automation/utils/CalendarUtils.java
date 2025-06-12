package com.carservice.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for calendar-related operations
 */
public class CalendarUtils {

    private static final Logger logger = LogManager.getLogger(CalendarUtils.class);

    // Working hours constraints
    public static final LocalTime WORKING_START_TIME = LocalTime.of(11, 0);
    public static final LocalTime WORKING_END_TIME = LocalTime.of(18, 0);

    // Day names mapping for FullCalendar week view
    public static final String[] WEEK_DAY_NAMES = {
            "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday"
    };

    // Sunday index in the week view (0-based)
    public static final int SUNDAY_INDEX = 3;

    /**
     * Check if a time string is within working hours
     */
    public static boolean isWithinWorkingHours(String timeStr) {
        try {
            LocalTime time = LocalTime.parse(timeStr);
            return !time.isBefore(WORKING_START_TIME) && !time.isAfter(WORKING_END_TIME);
        } catch (Exception e) {
            logger.warn("Invalid time format: {}", timeStr);
            return false;
        }
    }

    /**
     * Check if a day index represents a working day (not Sunday)
     */
    public static boolean isWorkingDay(int dayIndex) {
        return dayIndex != SUNDAY_INDEX;
    }

    /**
     * Get day name from day index
     */
    public static String getDayName(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < WEEK_DAY_NAMES.length) {
            return WEEK_DAY_NAMES[dayIndex];
        }
        return "Unknown";
    }

    /**
     * Format time for display
     */
    public static String formatTimeForDisplay(String timeStr) {
        try {
            LocalTime time = LocalTime.parse(timeStr);
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return timeStr;
        }
    }

    /**
     * Check if a time slot is available (not disabled or occupied)
     */
    public static boolean isSlotAvailable(WebDriver driver, WebElement slot) {
        try {
            // Check if slot has disabled class or is marked as non-business
            String classAttribute = slot.getAttribute("class");
            if (classAttribute != null &&
                    (classAttribute.contains("disabled") ||
                            classAttribute.contains("fc-non-business"))) {
                return false;
            }

            // Check for existing events at this time
            String timeStr = slot.getAttribute("data-time");
            if (timeStr == null) {
                return false;
            }

            // Look for events in the calendar at this time
            List<WebElement> eventsAtTime = driver.findElements(By.xpath(
                    "//div[contains(@class, 'fc-event') and contains(@data-time, '" + timeStr + "')]"));

            return eventsAtTime.isEmpty();

        } catch (Exception e) {
            logger.debug("Error checking slot availability: {}", e.getMessage());
            return true; // Default to available if we can't determine
        }
    }

    /**
     * Get current time for comparison
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }

    /**
     * Check if a time slot is in the future
     */
    public static boolean isSlotInFuture(String timeStr) {
        try {
            LocalTime slotTime = LocalTime.parse(timeStr);
            LocalTime currentTime = getCurrentTime();
            return slotTime.isAfter(currentTime);
        } catch (Exception e) {
            logger.warn("Error comparing times: {}", e.getMessage());
            return true; // Default to future if we can't determine
        }
    }

    /**
     * Find the best time slot based on preferences
     */
    public static WebElement findBestTimeSlot(WebDriver driver, List<WebElement> availableSlots) {
        WebElement bestSlot = null;
        LocalTime earliestTime = null;

        for (WebElement slot : availableSlots) {
            String timeStr = slot.getAttribute("data-time");
            if (timeStr == null) continue;

            try {
                LocalTime slotTime = LocalTime.parse(timeStr);

                // Prefer earlier times within working hours
                if (isWithinWorkingHours(timeStr) &&
                        (earliestTime == null || slotTime.isBefore(earliestTime))) {
                    earliestTime = slotTime;
                    bestSlot = slot;
                }
            } catch (Exception e) {
                logger.debug("Error processing slot time {}: {}", timeStr, e.getMessage());
            }
        }

        return bestSlot;
    }

    /**
     * Scroll calendar to a specific time
     */
    public static void scrollToTime(WebDriver driver, String targetTime) {
        try {
            WebElement timeSlot = driver.findElement(By.xpath(
                    "//td[@data-time='" + targetTime + "' and contains(@class, 'fc-timegrid-slot-label')]"));

            if (timeSlot != null) {
                // Scroll element into view
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", timeSlot);

                Thread.sleep(1000); // Wait for scroll to complete
                logger.info("Scrolled to time: {}", targetTime);
            }
        } catch (Exception e) {
            logger.warn("Could not scroll to time {}: {}", targetTime, e.getMessage());
        }
    }

    /**
     * Get all time slots for a specific day column
     */
    public static List<WebElement> getTimeSlotsForDay(WebDriver driver, int dayIndex) {
        List<WebElement> daySlots = new ArrayList<>();

        try {
            // This would need to be refined based on the actual calendar structure
            // For now, return all slots and filter by day logic elsewhere
            List<WebElement> allSlots = driver.findElements(By.xpath(
                    "//td[contains(@class, 'fc-timegrid-slot-lane') and @data-time]"));

            // Additional logic would be needed to filter by day column
            daySlots.addAll(allSlots);

        } catch (Exception e) {
            logger.error("Error getting time slots for day {}: {}", dayIndex, e.getMessage());
        }

        return daySlots;
    }

    /**
     * Validate calendar constraints for a time slot
     */
    public static boolean validateSlotConstraints(String timeStr, int dayIndex) {
        // Check working hours
        if (!isWithinWorkingHours(timeStr)) {
            logger.debug("Slot {} is outside working hours", timeStr);
            return false;
        }

        // Check working days
        if (!isWorkingDay(dayIndex)) {
            logger.debug("Slot on day {} is not a working day", getDayName(dayIndex));
            return false;
        }

        // Check future time (optional - depends on requirements)
        if (!isSlotInFuture(timeStr)) {
            logger.debug("Slot {} is in the past", timeStr);
            return false;
        }

        return true;
    }

    /**
     * Log calendar slot information for debugging
     */
    public static void logSlotInfo(String timeStr, int dayIndex, boolean available) {
        logger.debug("Calendar Slot - Time: {}, Day: {} ({}), Available: {}, Working Hours: {}",
                timeStr,
                dayIndex,
                getDayName(dayIndex),
                available,
                isWithinWorkingHours(timeStr));
    }
}