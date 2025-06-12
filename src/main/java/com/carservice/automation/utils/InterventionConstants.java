package com.carservice.automation.utils;

import java.time.LocalTime;

public class InterventionConstants {

    // Working hours configuration
    public static final LocalTime WORKING_START_TIME = LocalTime.of(11, 0);
    public static final LocalTime WORKING_END_TIME = LocalTime.of(18, 0);
    public static final int MINIMUM_BOOKING_BUFFER_MINUTES = 30;

    // Wait times
    public static final int SHORT_WAIT = 1000;
    public static final int MEDIUM_WAIT = 2000;
    public static final int LONG_WAIT = 3000;

    // Sidebar menu locators
    public static final String INTERVENTIONS_MENU_XPATH = "//span[contains(text(), 'Mes interventions')]";
    public static final String SIDEBAR_MENU_ITEM_XPATH = "//a[@href='/public/appointments']";

    // Filter form locators
    public static final String AGENCY_DROPDOWN_XPATH = "//tui-select[@formcontrolname='agency']//input[@readonly]";
    public static final String ATLAS_AUTO_OPTION_XPATH = "//span[contains(text(), 'Atlas Auto')]";
    public static final String SERVICE_DIAGNOSTIQUE_RADIO_XPATH = "//div[contains(text(), 'Service Diagnostique')]/preceding-sibling::tui-radio//input[@type='radio']";
    public static final String SERVICE_DIAGNOSTIQUE_LABEL_XPATH = "//div[contains(@class, 't-label') and contains(text(), 'Service Diagnostique')]";

    // Appointment card locators
    public static final String APPOINTMENT_CARDS_XPATH = "//div[contains(@class, 'event-card')]";
    public static final String PLATE_NUMBER_XPATH = ".//span[contains(@class, 'car-plate')]";
    public static final String SERVICE_TYPE_XPATH = ".//span[contains(text(), 'Service Diagnostique')]";
    public static final String MILEAGE_XPATH = ".//span[contains(text(), 'KM')]";

    // Calendar locators
    public static final String ALL_TIME_SLOTS_XPATH = "//td[contains(@class, 'fc-timegrid-slot-lane') and @data-time and not(contains(@class, 'fc-timegrid-slot-minor'))]";
    public static final String DAY_COLUMNS_XPATH = "//td[contains(@class, 'fc-timegrid-col fc-day')]";
    public static final String ELEVEN_AM_SLOT_XPATH = "//td[@data-time='11:00:00' and contains(@class, 'fc-timegrid-slot-label')]";

    // Modal confirmation locators
    public static final String CONFIRMATION_MODAL_XPATH = "//app-dialog";
    public static final String MODAL_TITLE_XPATH = "//h1[contains(text(), 'Êtes vous sur de confirmer le rendez-vous')]";
    public static final String CONFIRM_BUTTON_XPATH = "//button//span[contains(text(), 'Confirmer')]";

}
