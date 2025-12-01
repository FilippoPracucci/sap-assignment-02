Feature: User registration
  As a person,
  I want to create an account
  So that I can login and use the system for delivering packages.

  Scenario: Successful registration
    Given I am on the registration page
    And I have not an account
    When I create an account with a username "marco" and a valid password "Secret#123"
    Then I should see a confirmation that my account has been created and receive my identifier