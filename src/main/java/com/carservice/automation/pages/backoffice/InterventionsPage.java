package com.carservice.automation.pages.backoffice;

import com.carservice.automation.base.BasePage;
import com.carservice.automation.utils.AllureUtils;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.carservice.automation.utils.InterventionConstants.*;


/**
 * Page Object class for Interventions/Appointments management with calendar scheduling
 */
public class InterventionsPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(InterventionsPage.class);


    // Expected appointment data
    private final String expectedPlateNumber;
    private final String expectedMileage;

    public InterventionsPage(WebDriver driver) {
        super(driver);
        this.expectedPlateNumber = configReader.getProperty("vehicle.plate.numero") + "TU" + configReader.getProperty("vehicle.plate.serie");
        this.expectedMileage = configReader.getProperty("vehicle.mileage");
    }

    @Step("Navigate to interventions page")
    public void navigateToInterventions() {
        logger.info("Navigating to interventions page");

        String[] menuSelectors = {
                INTERVENTIONS_MENU_XPATH,
                SIDEBAR_MENU_ITEM_XPATH,
                "//a[contains(@href, 'appointments')]//span[contains(text(), 'Mes interventions')]"
        };

        WebElement interventionsMenu = findElementWithMultipleSelectors(menuSelectors, "Interventions menu");
        clickElement(interventionsMenu, "Mes interventions menu");
        waitForElement(LONG_WAIT);

        AllureUtils.attachScreenshot("Navigated to interventions page");
        logger.info("Successfully navigated to interventions page");
    }

    @Step("Select Atlas Auto agency")
    public void selectAtlasAutoAgency() {
        logger.info("Selecting Atlas Auto agency");

        WebElement agencyDropdown = findAgencyDropdown();
        clickElement(agencyDropdown, "Agency dropdown");
        waitForElement(SHORT_WAIT);

        String[] atlasSelectors = {
                ATLAS_AUTO_OPTION_XPATH,
                "//div[contains(@class, 'card-item')]//span[contains(text(), 'Atlas Auto')]",
                "//div[contains(text(), 'Atlas Auto')]"
        };

        WebElement atlasOption = findElementWithMultipleSelectors(atlasSelectors, "Atlas Auto option");
        clickElement(atlasOption, "Atlas Auto agency");
        waitForElement(MEDIUM_WAIT);

        AllureUtils.addParameter("Selected Agency", "Atlas Auto");
        logger.info("Atlas Auto agency selected successfully");
    }

    /**
     * Select Service Diagnostique filter
     */
    @Step("Select Service Diagnostique filter")
    public void selectServiceDiagnostique() {
        logger.info("Selecting Service Diagnostique filter");

        String[] serviceSelectors = {
                SERVICE_DIAGNOSTIQUE_RADIO_XPATH,
                "//input[@name='filterType' and @type='radio'][1]",
                "//tui-radio[1]//input[@type='radio']"
        };

        WebElement serviceRadio = null;
        for (String selector : serviceSelectors) {
            try {
                serviceRadio = findElementWithWait(selector);
                if (serviceRadio != null) {
                    logger.info("Found service radio with selector: {}", selector);
                    break;
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (serviceRadio == null) {
            logger.info("Radio button not found, trying to click label");
            WebElement serviceLabel = findElementWithWait(SERVICE_DIAGNOSTIQUE_LABEL_XPATH);
            clickElement(serviceLabel, "Service Diagnostique label");
        } else {
            clickElement(serviceRadio, "Service Diagnostique radio button");
        }

        waitForElement(2000);
        AllureUtils.addParameter("Selected Service", "Service Diagnostique");
        logger.info("Service Diagnostique filter selected successfully");
    }

    @Step("Find target intervention card")
    public WebElement findTargetInterventionCard() {
        logger.info("Finding target intervention card for scheduling");

        waitForElement(MEDIUM_WAIT);
        List<WebElement> appointmentCards = driver.findElements(By.xpath(APPOINTMENT_CARDS_XPATH));
        logger.info("Found {} appointment cards to check", appointmentCards.size());

        for (int i = 0; i < appointmentCards.size(); i++) {
            WebElement card = appointmentCards.get(i);
            if (isTargetAppointment(card)) {
                logger.info("Found target intervention card at index {}", i);
                AllureUtils.addParameter("Target Card Index", String.valueOf(i));
                AllureUtils.attachScreenshot("Target intervention card found");
                return card;
            }
        }

        logger.warn("Target intervention card not found");
        AllureUtils.attachScreenshot("Target intervention card not found");
        return null;
    }

    @Step("Find available calendar time slot")
    public CalendarSlot findAvailableTimeSlot() {
        logger.info("Finding available time slot for intervention scheduling");

        scrollToWorkingHours();
        List<CalendarSlot> availableSlots = findAllAvailableSlots();

        if (availableSlots.isEmpty()) {
            logger.error("No available time slots found");
            AllureUtils.attachScreenshot("No available slots");
            return null;
        }

        CalendarSlot selectedSlot = availableSlots.get(0);
        logger.info("Selected time slot: {} on day {}", selectedSlot.time, selectedSlot.dayIndex);
        AllureUtils.addParameter("Selected Time Slot", selectedSlot.time);
        AllureUtils.addParameter("Selected Day", selectedSlot.dayName);

        return selectedSlot;
    }

    @Step("Drag intervention to calendar time slot")
    public boolean dragInterventionToCalendarSlot() {
        logger.info("Starting drag and drop intervention to calendar");

        WebElement interventionCard = findTargetInterventionCard();
        if (interventionCard == null) {
            logger.error("Could not find target intervention card");
            return false;
        }

        CalendarSlot targetSlot = findAvailableTimeSlot();
        if (targetSlot == null) {
            logger.error("Could not find available time slot");
            return false;
        }

        return performDragAndDrop(interventionCard, targetSlot);
    }

    @Step("Handle appointment confirmation modal")
    public boolean handleConfirmationModal() {
        logger.info("Handling confirmation modal");

        try {
            waitForElement(MEDIUM_WAIT);

            WebElement modal = findElementWithWait(CONFIRMATION_MODAL_XPATH);
            if (modal == null) {
                logger.error("Confirmation modal not found");
                AllureUtils.attachScreenshot("Modal not found");
                return false;
            }

            AllureUtils.attachScreenshot("Confirmation modal appeared");
            verifyModalContent();

            String[] confirmSelectors = {
                    CONFIRM_BUTTON_XPATH,
                    "//button//span[contains(text(), 'Confirmer')]/parent::span/parent::button",
                    "//button[contains(@class, 'primary')]//span[contains(text(), 'Confirmer')]"
            };

            WebElement confirmButton = findElementWithMultipleSelectors(confirmSelectors, "Confirm button");
            if (confirmButton == null) {
                logger.error("Confirm button not found in modal");
                AllureUtils.attachScreenshot("Confirm button not found");
                return false;
            }

            clickElement(confirmButton, "Confirm button");
            waitForElement(LONG_WAIT);

            AllureUtils.attachScreenshot("After confirmation");
            logger.info("Confirmation modal handled successfully");
            return true;

        } catch (Exception e) {
            logger.error("Error handling confirmation modal: {}", e.getMessage());
            AllureUtils.attachScreenshot("Modal handling error");
            return false;
        }
    }

    @Step("Complete intervention scheduling in calendar")
    public boolean scheduleInterventionInCalendar() {
        logger.info("Starting complete intervention scheduling flow");

        try {
            boolean dragSuccessful = dragInterventionToCalendarSlot();
            if (!dragSuccessful) {
                logger.error("Drag and drop operation failed");
                AllureUtils.addParameter("Scheduling Result", "FAILED - Drag and drop unsuccessful");
                return false;
            }

            boolean confirmationSuccessful = handleConfirmationModal();
            if (!confirmationSuccessful) {
                logger.error("Confirmation modal handling failed");
                AllureUtils.addParameter("Scheduling Result", "FAILED - Confirmation unsuccessful");
                return false;
            }

            boolean verificationSuccessful = verifyInterventionScheduled();
            AllureUtils.addParameter("Scheduling Result", verificationSuccessful ? "SUCCESS" : "FAILED - Verification unsuccessful");

            if (verificationSuccessful) {
                AllureUtils.logStep("Intervention successfully scheduled in calendar");
                logger.info("Complete intervention scheduling flow completed successfully");
            }

            return verificationSuccessful;

        } catch (Exception e) {
            logger.error("Error in complete scheduling flow: {}", e.getMessage());
            AllureUtils.addParameter("Scheduling Result", "FAILED - Exception: " + e.getMessage());
            AllureUtils.attachScreenshot("Scheduling flow error");
            return false;
        }
    }

    @Step("Verify appointment exists in interventions")
    public boolean verifyAppointmentExists() {
        logger.info("Verifying appointment exists in interventions list");

        waitForElement(LONG_WAIT);
        List<WebElement> appointmentCards = driver.findElements(By.xpath(APPOINTMENT_CARDS_XPATH));
        logger.info("Found {} appointment cards", appointmentCards.size());

        if (appointmentCards.isEmpty()) {
            logger.warn("No appointment cards found");
            return false;
        }

        return appointmentCards.stream().anyMatch(this::isTargetAppointment);
    }

    // Private helper methods

    private boolean isTargetAppointment(WebElement card) {
        try {
            WebElement plateElement = card.findElement(By.xpath(PLATE_NUMBER_XPATH));
            String plateNumber = plateElement.getText().trim();

            if (!plateNumber.contains(expectedPlateNumber)) {
                return false;
            }

            List<WebElement> serviceElements = card.findElements(By.xpath(SERVICE_TYPE_XPATH));
            if (serviceElements.isEmpty()) {
                return false;
            }

            WebElement mileageElement = card.findElement(By.xpath(MILEAGE_XPATH));
            String mileageText = mileageElement.getText().trim();

            return mileageText.contains(expectedMileage);

        } catch (Exception e) {
            logger.debug("Error checking appointment card: {}", e.getMessage());
            return false;
        }
    }

    private void scrollToWorkingHours() {
        logger.info("Scrolling calendar to working hours");

        try {
            WebElement elevenAmSlot = findElementWithWait(ELEVEN_AM_SLOT_XPATH);
            if (elevenAmSlot != null) {
                scrollToElement(elevenAmSlot);
                waitForElement(SHORT_WAIT);
                logger.info("Scrolled to 11:00 AM slot");
            } else {
                scrollPage(300);
            }
        } catch (Exception e) {
            logger.warn("Could not scroll to working hours: {}", e.getMessage());
            scrollPage(300);
        }
    }

    private List<CalendarSlot> findAllAvailableSlots() {
        List<CalendarSlot> availableSlots = new ArrayList<>();
        LocalTime currentTime = LocalTime.now();

        try {
            List<WebElement> dayColumns = driver.findElements(By.xpath(DAY_COLUMNS_XPATH));
            logger.info("Found {} day columns in calendar", dayColumns.size());

            for (int dayIndex = 0; dayIndex < dayColumns.size(); dayIndex++) {
                WebElement dayColumn = dayColumns.get(dayIndex);
                DayInfo dayInfo = extractDayInfoFromColumn(dayColumn, dayIndex);

                if (dayInfo == null || isWeekendDay(dayInfo)) {
                    continue;
                }

                List<WebElement> timeSlotsForDay = getTimeSlotsForDay();

                for (WebElement slot : timeSlotsForDay) {
                    String timeStr = slot.getAttribute("data-time");
                    if (timeStr == null) continue;

                    try {
                        LocalTime slotTime = LocalTime.parse(timeStr);

                        if (isSlotValid(slotTime, dayInfo, currentTime, slot)) {
                            CalendarSlot calendarSlot = createCalendarSlot(slot, timeStr, dayInfo);
                            availableSlots.add(calendarSlot);
                            logger.info("Found available time slot: {} on {} ({})",
                                    calendarSlot.time, calendarSlot.dayName, calendarSlot.dateStr);
                            return availableSlots; // Return first available slot
                        }

                    } catch (Exception e) {
                        logger.debug("Error processing time slot {} on {}: {}", timeStr, dayInfo.dayName, e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error finding available slots: {}", e.getMessage());
        }

        logger.info("Found {} available time slot(s)", availableSlots.size());
        return availableSlots;
    }

    private List<WebElement> getTimeSlotsForDay() {
        List<WebElement> timeSlotsForDay = new ArrayList<>();

        try {
            List<WebElement> availableTimeSlots = driver.findElements(By.xpath(ALL_TIME_SLOTS_XPATH));
            logger.debug("Found {} major (bookable) time slots", availableTimeSlots.size());

            Set<String> seenTimes = new HashSet<>();
            for (WebElement slot : availableTimeSlots) {
                String timeStr = slot.getAttribute("data-time");
                if (timeStr != null && !timeStr.isEmpty() && !seenTimes.contains(timeStr)) {
                    seenTimes.add(timeStr);
                    timeSlotsForDay.add(slot);
                    logger.debug("Added bookable time slot: {}", timeStr);
                }
            }

            logger.debug("Found {} unique bookable time slots", timeSlotsForDay.size());

        } catch (Exception e) {
            logger.error("Error getting time slots: {}", e.getMessage());
        }

        return timeSlotsForDay;
    }

    private boolean isSlotValid(LocalTime slotTime, DayInfo dayInfo, LocalTime currentTime, WebElement slot) {
        return isWithinWorkingHours(slotTime) &&
                isSlotClickableAndAvailable(slot) &&
                !shouldSkipSlot(slotTime, dayInfo, currentTime) &&
                isSlotAvailable(slot, slotTime.toString());
    }

    private boolean isWithinWorkingHours(LocalTime slotTime) {
        return !slotTime.isBefore(WORKING_START_TIME) && !slotTime.isAfter(WORKING_END_TIME);
    }

    private boolean isWeekendDay(DayInfo dayInfo) {
        return "sunday".equals(dayInfo.dayType) || "saturday".equals(dayInfo.dayType);
    }

    private CalendarSlot createCalendarSlot(WebElement slot, String timeStr, DayInfo dayInfo) {
        CalendarSlot calendarSlot = new CalendarSlot();
        calendarSlot.element = slot;
        calendarSlot.time = timeStr;
        calendarSlot.dayIndex = dayInfo.columnIndex;
        calendarSlot.dayName = dayInfo.dayName;
        calendarSlot.dayType = dayInfo.dayType;
        calendarSlot.dateStr = dayInfo.dateStr;
        return calendarSlot;
    }

    private boolean isSlotClickableAndAvailable(WebElement slot) {
        try {
            if (!slot.isDisplayed() || !slot.isEnabled()) {
                return false;
            }

            String classAttr = slot.getAttribute("class");
            if (classAttr != null) {
                return !classAttr.contains("disabled") &&
                        !classAttr.contains("fc-non-business") &&
                        !classAttr.contains("fc-past") &&
                        !classAttr.contains("fc-timegrid-slot-minor");
            }

            return true;

        } catch (Exception e) {
            logger.debug("Error checking if slot is clickable: {}", e.getMessage());
            return false;
        }
    }

    private boolean isSlotAvailable(WebElement slot, String timeStr) {
        try {
            List<WebElement> events = driver.findElements(By.xpath(
                    "//div[contains(@class, 'fc-timegrid-col-events')]//div[contains(@class, 'fc-event') and contains(@style, 'top')]"));
            return events.isEmpty();
        } catch (Exception e) {
            logger.debug("Error checking slot availability for {}: {}", timeStr, e.getMessage());
            return true;
        }
    }

    private DayInfo extractDayInfoFromColumn(WebElement column, int columnIndex) {
        try {
            String classAttr = column.getAttribute("class");
            String dataDate = column.getAttribute("data-date");

            DayInfo dayInfo = new DayInfo();
            dayInfo.columnIndex = columnIndex;
            dayInfo.dateStr = dataDate;

            if (classAttr != null) {
                dayInfo.dayType = extractDayTypeFromClass(classAttr);
                dayInfo.dayName = capitalizeDayType(dayInfo.dayType);
                dayInfo.isToday = classAttr.contains("fc-day-today");
            }

            return dayInfo;

        } catch (Exception e) {
            logger.debug("Error extracting day info from column: {}", e.getMessage());
            return null;
        }
    }

    private String extractDayTypeFromClass(String classAttr) {
        if (classAttr.contains("fc-day-sun")) return "sunday";
        if (classAttr.contains("fc-day-sat")) return "saturday";
        if (classAttr.contains("fc-day-mon")) return "monday";
        if (classAttr.contains("fc-day-tue")) return "tuesday";
        if (classAttr.contains("fc-day-wed")) return "wednesday";
        if (classAttr.contains("fc-day-thu")) return "thursday";
        if (classAttr.contains("fc-day-fri")) return "friday";
        return "unknown";
    }

    private String capitalizeDayType(String dayType) {
        if (dayType == null || dayType.isEmpty()) return "Unknown";
        return dayType.substring(0, 1).toUpperCase() + dayType.substring(1);
    }

    private boolean shouldSkipSlot(LocalTime slotTime, DayInfo dayInfo, LocalTime currentTime) {
        if (isWeekendDay(dayInfo)) {
            logger.debug("Skipping weekend slot: {}", dayInfo.dayName);
            return true;
        }

        if (dayInfo.isToday) {
            LocalTime bufferTime = currentTime.plusMinutes(MINIMUM_BOOKING_BUFFER_MINUTES);
            if (slotTime.isBefore(bufferTime)) {
                logger.debug("Skipping today's slot {} as it's too close to current time {}", slotTime, currentTime);
                return true;
            }

            if (slotTime.isAfter(LocalTime.of(12, 0))) {
                logger.debug("Skipping today's afternoon slot {}", slotTime);
                return true;
            }
        }

        return false;
    }

    private boolean performDragAndDrop(WebElement sourceElement, CalendarSlot targetSlot) {
        logger.info("Performing drag and drop to time slot: {}", targetSlot.time);

        try {
            Actions actions = new Actions(driver);
            WebElement dropTarget = findDropTarget(targetSlot);

            if (dropTarget == null) {
                dropTarget = targetSlot.element;
            }

            prepareElementsForDragDrop(sourceElement, dropTarget);
            AllureUtils.attachScreenshot("Before drag and drop");

            // Try multiple drag and drop strategies
            return tryDragDropStrategies(actions, sourceElement, dropTarget);

        } catch (Exception e) {
            logger.error("Drag and drop operation failed: {}", e.getMessage());
            AllureUtils.attachScreenshot("Drag and drop failed");
            return false;
        }
    }

    private void prepareElementsForDragDrop(WebElement sourceElement, WebElement dropTarget) {
        scrollToElement(sourceElement);
        waitForElement(500);
        scrollToElement(dropTarget);
        waitForElement(500);
    }

    private boolean tryDragDropStrategies(Actions actions, WebElement sourceElement, WebElement dropTarget) {
        // Strategy 1: JavaScript-based drag and drop
        if (tryJavaScriptDragDrop(sourceElement, dropTarget)) {
            return true;
        }

        // Strategy 2: Standard Selenium drag and drop
        if (tryStandardDragDrop(actions, sourceElement, dropTarget)) {
            return true;
        }

        // Strategy 3: Click and hold sequence
        if (tryClickAndHoldDragDrop(actions, sourceElement, dropTarget)) {
            return true;
        }

        AllureUtils.attachScreenshot("All drag and drop methods failed");
        logger.error("All drag and drop methods failed");
        return false;
    }

    private boolean tryJavaScriptDragDrop(WebElement sourceElement, WebElement dropTarget) {
        try {
            String jsScript =
                    "function simulateDragDrop(sourceEl, targetEl) {" +
                            "    var dragStartEvent = new MouseEvent('dragstart', {bubbles: true, cancelable: true});" +
                            "    var dropEvent = new MouseEvent('drop', {bubbles: true, cancelable: true});" +
                            "    var dragEndEvent = new MouseEvent('dragend', {bubbles: true, cancelable: true});" +
                            "    sourceEl.dispatchEvent(dragStartEvent);" +
                            "    targetEl.dispatchEvent(dropEvent);" +
                            "    sourceEl.dispatchEvent(dragEndEvent);" +
                            "}" +
                            "simulateDragDrop(arguments[0], arguments[1]);";

            jsExecutor.executeScript(jsScript, sourceElement, dropTarget);
            waitForElement(MEDIUM_WAIT);

            if (verifyDragDropSuccess()) {
                logger.info("JavaScript drag and drop successful");
                AllureUtils.attachScreenshot("After successful JS drag and drop");
                return true;
            }
        } catch (Exception e) {
            logger.warn("JavaScript drag and drop failed: {}", e.getMessage());
        }
        return false;
    }

    private boolean tryStandardDragDrop(Actions actions, WebElement sourceElement, WebElement dropTarget) {
        try {
            actions.dragAndDrop(sourceElement, dropTarget).perform();
            waitForElement(MEDIUM_WAIT);

            if (verifyDragDropSuccess()) {
                logger.info("Standard drag and drop successful");
                AllureUtils.attachScreenshot("After successful standard drag and drop");
                return true;
            }
        } catch (Exception e) {
            logger.warn("Standard drag and drop failed: {}", e.getMessage());
        }
        return false;
    }

    private boolean tryClickAndHoldDragDrop(Actions actions, WebElement sourceElement, WebElement dropTarget) {
        try {
            actions.clickAndHold(sourceElement)
                    .pause(500)
                    .moveToElement(dropTarget)
                    .pause(500)
                    .release()
                    .perform();
            waitForElement(MEDIUM_WAIT);

            if (verifyDragDropSuccess()) {
                logger.info("Click and hold drag and drop successful");
                AllureUtils.attachScreenshot("After successful click-hold drag and drop");
                return true;
            }
        } catch (Exception e) {
            logger.warn("Click and hold drag and drop failed: {}", e.getMessage());
        }
        return false;
    }

    private boolean verifyDragDropSuccess() {
        boolean modalPresent = isConfirmationModalPresent();
        boolean eventInCalendar = isInterventionScheduledInCalendar();

        logger.info("Drag drop verification - Modal present: {}, Event in calendar: {}", modalPresent, eventInCalendar);
        return modalPresent || eventInCalendar;
    }

    private boolean isInterventionScheduledInCalendar() {
        try {
            String eventXPath = String.format(
                    "//div[contains(@class, 'fc-timegrid-event-harness')]//span[contains(text(), '%s')]",
                    expectedPlateNumber);

            WebElement scheduledEvent = driver.findElement(By.xpath(eventXPath));

            if (scheduledEvent != null && scheduledEvent.isDisplayed()) {
                logger.info("Found scheduled intervention in calendar with plate: {}", expectedPlateNumber);
                return true;
            }

        } catch (Exception e) {
            logger.debug("Intervention not found in calendar: {}", e.getMessage());
        }

        return false;
    }

    private void verifyModalContent() {
        logger.info("Verifying modal content");

        try {
            WebElement modalTitle = findElementWithWait(MODAL_TITLE_XPATH);
            Assert.assertNotNull(modalTitle, "Modal title should be present");

            WebElement serviceName = findElementWithWait("//span[contains(text(), 'Service Diagnostique')]");
            Assert.assertNotNull(serviceName, "Service name should be displayed in modal");

            AllureUtils.addParameter("Modal Verification", "All required elements found");
            logger.info("Modal content verification completed");

        } catch (Exception e) {
            logger.warn("Modal content verification failed: {}", e.getMessage());
            AllureUtils.addParameter("Modal Verification", "FAILED: " + e.getMessage());
        }
    }

    private boolean verifyInterventionScheduled() {
        logger.info("Verifying intervention was scheduled successfully");

        try {
            waitForElement(LONG_WAIT);

            boolean eventInCalendar = isInterventionScheduledInCalendar();
            if (eventInCalendar) {
                logger.info("Intervention successfully scheduled - found in calendar");
                AllureUtils.attachScreenshot("Intervention scheduled in calendar");
                return true;
            }

            boolean cardStillInRequested = verifyAppointmentExists();
            if (!cardStillInRequested) {
                logger.info("Intervention card no longer in requested state - likely scheduled");
                AllureUtils.attachScreenshot("Intervention removed from requested list");
                return true;
            }

            logger.warn("Could not verify intervention scheduling");
            AllureUtils.attachScreenshot("Scheduling verification failed");
            return false;

        } catch (Exception e) {
            logger.error("Error verifying intervention scheduling: {}", e.getMessage());
            return false;
        }
    }

    private WebElement findAgencyDropdown() {
        String[] selectors = {
                AGENCY_DROPDOWN_XPATH,
                "//tui-select[@formcontrolname='agency']//input",
                "//form//tui-select[1]//input[@readonly]"
        };

        return findElementWithMultipleSelectors(selectors, "Agency dropdown");
    }

    private boolean isConfirmationModalPresent() {
        try {
            WebElement modal = driver.findElement(By.xpath(CONFIRMATION_MODAL_XPATH));
            return modal != null && modal.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement findDropTarget(CalendarSlot targetSlot) {
        try {
            String[] dropTargetSelectors = {
                    "//td[contains(@class, 'fc-timegrid-col') and not(contains(@class, 'fc-day-sun'))]//div[contains(@class, 'fc-timegrid-col-frame')]",
                    "//div[contains(@class, 'fc-timegrid-body')]",
                    "//td[contains(@class, 'fc-timegrid-col fc-day') and not(contains(@class, 'fc-day-sun'))]"
            };

            for (String selector : dropTargetSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.xpath(selector));
                    if (!elements.isEmpty()) {
                        for (WebElement element : elements) {
                            String classAttr = element.getAttribute("class");
                            if (classAttr != null && !classAttr.contains("fc-day-sun")) {
                                logger.info("Found drop target with selector: {}", selector);
                                return element;
                            }
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            logger.warn("Error finding drop target: {}", e.getMessage());
        }

        return null;
    }

    // Inner classes

    public static class DayInfo {
        public int columnIndex;
        public String dayType;
        public String dayName;
        public String dateStr;
        public boolean isToday;

        @Override
        public String toString() {
            return String.format("DayInfo{columnIndex=%d, dayType='%s', dayName='%s', dateStr='%s', isToday=%s}",
                    columnIndex, dayType, dayName, dateStr, isToday);
        }
    }

    public static class CalendarSlot {
        public WebElement element;
        public String time;
        public int dayIndex;
        public String dayName;
        public String dayType;
        public String dateStr;

        @Override
        public String toString() {
            return String.format("CalendarSlot{time='%s', dayIndex=%d, dayName='%s', dayType='%s', dateStr='%s'}",
                    time, dayIndex, dayName, dayType, dateStr);
        }
    }
}