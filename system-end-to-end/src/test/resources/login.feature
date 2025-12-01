Feature: User login
  As a person,
  I want to login
  so that I can access the system for delivering packages.

  Scenario: Successful login
    Given I am on the login page
    And I have an account
    When I login with my userId "user-1" and my password "Secret#123"
    Then I should access to the system for delivering packages

  Scenario: Login fails without having an account
    Given I am on the login page
    And I have not an account
    When I login with userId "user-2" and the password "Secret#123"
    Then I should see an error "Username does not exist"
    And I should not access to the system

  Scenario: Login fails with wrong password
    Given I am on the login page
    And I have an account with userId "user-1" and password "Secret#123"
    When I login with userId "user-1" and the password "1234!"
    Then I should see an error "Wrong password"
    And I should not access to the system
