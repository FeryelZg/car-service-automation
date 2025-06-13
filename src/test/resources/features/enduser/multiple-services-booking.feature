Feature: Multiple Services Appointment Booking
  As a vehicle owner
  I want to book multiple services appointment
  So that I can get multiple services done at once at an authorized repairer

  Background:
    Given I am on the vehicle appointment booking page
    And I have switched the language to English

  @smoke @multipleservices
  Scenario: Complete multiple services appointment booking
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    And I fill multiple services form with selected options
    And I click the "Next" button
    And I select a repairer
    And I click the "Next" button
    And I select a date and time
    And I click the "Next" button
    And I verify the summary information
    And I confirm the appointment
    Then I should see the appointment confirmation
    And the multiple services appointment should be successfully created

  @regression @multipleservices @validation
  Scenario: Multiple services form validation and dropdown testing
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    Then the multiple services form should be displayed
    When I test multiple services dropdown functionality
    And I test mileage dropdown functionality
    Then I should be able to select multiple services
    And I should be able to select mileage from dropdown

  @endtoend @multipleservices
  Scenario: Multiple services appointment with specific selections
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    And I fill multiple services form with specific selections:
      | Services | Fast Service,Bodywork repair service,Mechanical Repair Service |
      | Mileage  | 60 000                                        |
      | With File| false                                        |
    And I click the "Next" button
    And I select a repairer
    And I click the "Next" button
    And I select a date and time
    And I click the "Next" button
    And I verify the summary information
    And I confirm the appointment
    Then I should see the appointment confirmation
    And the multiple services appointment should be successfully created

  @smoke @multipleservices @nofile
  Scenario: Multiple services appointment without file upload
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    And I fill multiple services form without file upload
    And I click the "Next" button
    And I select a repairer
    And I click the "Next" button
    And I select a date and time
    And I click the "Next" button
    And I verify the summary information
    And I confirm the appointment
    Then I should see the appointment confirmation
    And the multiple services appointment should be successfully created

  @regression @multipleservices @dropdown
  Scenario: Test different service combinations
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    And I select services from dropdown:
      | Fast Service        |
      | Bodywork repair service  |
      | Mechanical Repair Service     |
    And I select mileage "90 000"
    And I fill multiple services form with selected options
    Then the multiple services form should be displayed
    And I should be able to select multiple services

  @regression @multipleservices @mileage
  Scenario Outline: Test different mileage selections
    When I click the "Make Appointment" button
    And I select "Serie Normale" option
    And I click the "Next" button
    And I fill vehicle identification form with valid data
    And I click the "Next" button
    And I select "Multiple Services" option
    And I fill multiple services form with specific selections:
      | Services | Fast Service           |
      | Mileage  | <mileage>           |
      | With File| false               |
    Then I should be able to select mileage from dropdown

    Examples:
      | mileage |
      | 30000   |
      | 60 000   |
      | 90 000   |
      | 120 000  |