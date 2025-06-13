Feature: Intervention Scheduling in Backoffice
  As a backoffice administrator
  I want to schedule intervention requests in the calendar
  So that customer appointments can be properly managed

  Background:
    Given I am logged into the backoffice application
    And I have selected the HAVAL workspace
    And I am on the interventions management page

  @smoke
  Scenario: View and filter intervention requests
    When I apply filters to locate interventions:
      | Filter Type | Value                |
      | Agency      | Atlas Auto Agency    |
      | Service     | Service Diagnostique |
    Then I should see intervention requests matching the filters
    And intervention details should be displayed correctly

  @regression
  Scenario: Schedule intervention in calendar
    Given there is an unscheduled intervention request
    When I apply filters to locate the intervention
    And I find an available time slot in the calendar
    And I drag the intervention to the available slot
    And I confirm the scheduling in the modal
    Then the intervention should be scheduled successfully
    And the calendar should show the scheduled intervention

  @constraints
  Scenario: Validate calendar scheduling constraints
    When I look for available calendar time slots
    Then available slots should respect working hours constraints:
      | Constraint    | Rule                    |
      | Working Hours | Between 11:00 and 18:00 |
      | Working Days  | Monday to Saturday      |
      | Slot Duration | Minimum 1 hour blocks   |
    And Sunday slots should not be available
    And slots outside working hours should not be selectable