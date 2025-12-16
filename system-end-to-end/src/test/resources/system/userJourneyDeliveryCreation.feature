Feature: User registration, login and delivery creation
  As a user
  I want to register and login
  so that I can create a delivery

  Scenario: Successful user registration and delivery creation
    Given The system is running
    And The system has no users with id "user-0"
    And The system has no deliveries with id "delivery-0"
    When I register with a username "marco" and a valid password "Secret#123"
    Then I should see a confirmation that my account was created and receive my identifier "user-0"
    When I login with userId "user-0" and password "Secret#123"
    Then I should see a confirmation that I logged in
    When I request to create a delivery with weight "2" kg, starting place "via Emilia, 9", destination place "via Veneto, 5" to ship immediately
    Then I should see a confirmation that the delivery has been created and receive its identifier "delivery-0"
    When I request to get the delivery detail of the delivery "delivery-0"
    Then I should get the delivery detail with weight "2" kg, starting place "via Emilia, 9", destination place "via Veneto, 5"