Feature: Vehicle Appointment Booking
  As a vehicle owner
  I want to book a diagnostic service appointment
  So that I can get my vehicle serviced at an authorized repairer
  Background:
    Given I am on the vehicle appointment booking page
    And I have switched the language to English

  @smoke @appointment
  Scenario: Complete appointment booking with validation
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I perform vehicle identification validation
    And I click the "Next" button
    And I select "Diagnostic Service" option
    And I fill diagnostic form with file upload
    And I click the "Next" button
    And I select a repairer
    And I click the "Next" button
    And I select a date and time
    And I click the "Next" button
    And I verify the summary information
    And I confirm the appointment
    Then I should see the appointment confirmation
    And the appointment should be successfully created

  @regression @validation
  Scenario: Vehicle identification form validation
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    Then the "Next" button should be disabled when fields are empty
    When I test incomplete field validation
    Then the "Next" button should still be disabled
    When I test complete field validation
    Then the "Next" button should be enabled
    When I test field clearing validation
    Then the "Next" button should be disabled again

  @smoke @endtoend
  Scenario: Appointment booking without file upload
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Diagnostic Service" option
    And I fill diagnostic form without file upload
    And I click the "Next" button
    And I select a repairer
    And I click the "Next" button
    And I select a date and time
    And I click the "Next" button
    And I verify the summary information
    And I confirm the appointment
    Then I should see the appointment confirmation
    And the appointment should be successfully created