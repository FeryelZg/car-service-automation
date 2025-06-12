
// ========== SELECTORS MANAGEMENT ==========
package com.carservice.automation.selectors;

/**
 * BackofficeSelectors - Centralized selector management for your car service application
 * Organized by page/functionality with multiple fallback selectors
 */
public class BackofficeSelectors {

    // Login Page Selectors (based on your existing code patterns)
    public static class Login {
        public static final String[] USERNAME_INPUT = {
                "//tui-input[@formcontrolname='username']//input[contains(@id, 'tui_')]",
                "//tui-input[@formcontrolname='username']//input[@type='text' and contains(@class, 't-input')]",
                "//input[@type='text' and preceding-sibling::*//label[contains(text(), 'Nom d')]]",
                "//div[contains(@class, 't-content')]//input[@type='text'][1]",
                "//input[@aria-describedby and @type='text' and not(@tuimaskaccessor)]"
        };

        public static final String[] PASSWORD_INPUT = {
                "//tui-input-password[@formcontrolname='password']//input[contains(@id, 'tui_')]",
                "//tui-input-password[@formcontrolname='password']//input[@type='password' and contains(@class, 't-input')]",
                "//input[@type='password' and preceding-sibling::*//label[contains(text(), 'Mot de passe')]]",
                "//input[@type='password' and @aria-describedby]",
                "//tui-input-password//input[@type='password'][1]"
        };

        public static final String[] LOGIN_BUTTON = {
                "//button[@tuibutton]//span[@class='t-content' and contains(text(), 'Se connecter')]",
                "//button[@tuibutton and @data-appearance='primary' and @data-size='l']",
                "//button[@tuibutton]//tui-wrapper[@data-appearance='primary']",
                "//button[@tuibutton and @type='button' and contains(@class, 'w-100')]",
                "//button//span[contains(text(), 'Se connecter')]/ancestor::button",
                "//button[.//span[contains(text(), 'Se connecter')]]"
        };

        public static final String[] PAGE_TITLE = {
                "//span[contains(text(), 'Authentification')]",
                "//h1[contains(text(), 'Login')]",
                "//div[contains(@class, 'auth-title')]",
                "//*[contains(text(), 'Authentification')]"
        };
    }

    // Workspace Selection Selectors
    public static class Workspace {
        public static final String[] PAGE_TITLE = {
                "//span[contains(text(), 'Espace de travail')]"
        };

        public static final String[] DROPDOWN = {
                "//tui-select[@tuitextfieldsize='m']//input[@readonly]",
                "//input[contains(@id, 'tui_interactive_') and @readonly]",
                "//tui-select//input[@readonly]",
                "//div[contains(@class, 't-wrapper')]//input[@readonly]"
        };

        public static final String[] START_BUTTON = {
                "//button[contains(., 'Commencer')]"
        };

        public static final String[] LOGOUT_BUTTON = {
                "//button[contains(., 'Déconnexion')]"
        };

        public static String[] getWorkspaceOption(String workspaceName) {
            return new String[]{
                    String.format("//tui-select-option[contains(text(), '%s')]", workspaceName),
                    String.format("//*[contains(text(), '%s')]", workspaceName),
                    String.format("//tui-select-option[contains(normalize-space(text()), '%s')]", workspaceName),
                    String.format("//tui-select-option[starts-with(normalize-space(text()), '%s')]", workspaceName),
                    String.format("//div[contains(text(), '%s')] | //tui-select-option[contains(text(), '%s')]", workspaceName, workspaceName)
            };
        }

        public static final String[] DASHBOARD_INDICATORS = {
                "//div[contains(@class, 'main-side-menu')]",
                "//span[contains(text(), 'HAVAL')]",
                "//nav[contains(@class, 'workspace-services')]"
        };
    }

    // Interventions Page Selectors (based on your InterventionsPage patterns)
    public static class Interventions {
        public static final String[] MENU_NAVIGATION = {
                "//a[contains(@href, 'appointments')]//span[contains(text(), 'Mes interventions')]",
                "//span[contains(text(), 'Mes interventions')]/ancestor::a",
                "//div[contains(@class, 'sidebar-menu')]//span[contains(text(), 'Mes interventions')]"
        };

        public static final String[] AGENCY_DROPDOWN = {
                "//tui-select[@formcontrolname='agency']//input",
                "//form//tui-select[1]//input[@readonly]"
        };

        public static final String[] ATLAS_AUTO_OPTION = {
                "//div[contains(@class, 'card-item')]//span[contains(text(), 'Atlas Auto')]",
                "//div[contains(text(), 'Atlas Auto')]"
        };

        public static final String[] SERVICE_DIAGNOSTIQUE_RADIO = {
                "//input[@name='filterType' and @type='radio'][1]",
                "//tui-radio[1]//input[@type='radio']"
        };

        public static final String SERVICE_DIAGNOSTIQUE_LABEL = "//label[contains(text(), 'Service Diagnostique')]";

        // Appointment/Intervention card selectors
        public static final String APPOINTMENT_CARDS = "//div[contains(@class, 'appointment-card') or contains(@class, 'intervention-card')]";
        public static final String PLATE_NUMBER_IN_CARD = ".//span[contains(@class, 'plate-number') or contains(@class, 'vehicle-plate')]";
        public static final String MILEAGE_IN_CARD = ".//span[contains(@class, 'mileage') or contains(@class, 'vehicle-mileage')]";
        public static final String SERVICE_TYPE_IN_CARD = ".//div[contains(@class, 'service-type') or contains(text(), 'Service')]";

        // Calendar selectors
        public static final String DAY_COLUMNS = "//td[contains(@class, 'fc-timegrid-col')]";
        public static final String TIME_SLOTS = "//div[contains(@class, 'fc-timegrid-slot') and not(contains(@class, 'fc-timegrid-slot-minor'))]";
        public static final String ELEVEN_AM_SLOT = "//div[@data-time='11:00:00' or contains(@class, 'fc-timegrid-slot')]";
        public static final String ALL_TIME_SLOTS = "//div[contains(@class, 'fc-timegrid-slot-lane')]//div[@data-time]";

        // Modal selectors
        public static final String CONFIRMATION_MODAL = "//div[contains(@class, 'modal') or contains(@class, 'dialog')]";
        public static final String MODAL_TITLE = "//div[contains(@class, 'modal-title') or contains(@class, 'dialog-title')]";
        public static final String CONFIRM_BUTTON = "//button//span[contains(text(), 'Confirmer')]/ancestor::button";

        // Events in calendar
        public static final String CALENDAR_EVENTS = "//div[contains(@class, 'fc-timegrid-event-harness')]";

        public static String getEventByPlateNumber(String plateNumber) {
            return String.format("//div[contains(@class, 'fc-timegrid-event-harness')]//span[contains(text(), '%s')]", plateNumber);
        }
    }
}
