Feature: User registration, delivery creation and tracking
  As a user
  I want to track a delivery
  so that I can know the delivery state and the time left to destination.

  Scenario: Successful delivery tracking
    Given The system is running
    And The system has no users with id "user-0"
    And The system has no deliveries with id "delivery-0"
	And I registered with username "marco" and password "Secret#123"
    And I logged in with userId "user-0" and password "Secret#123"
    And I created a delivery with weight "2" kg, starting place "via Emilia, 9", destination place "via Veneto, 5" to ship immediately
    When I request to track the delivery with id "delivery-0"
    Then I should see a confirmation that the delivery has been tracked
    When I request to get the delivery status of the delivery "delivery-0"
    Then I should get the delivery status
    When I request to stop tracking the delivery with id "delivery-0"
    Then I should see a confirmation that the tracking has been stopped