Feature: User tracks a delivery
  As a user,
  I want to track the delivering process
  so that I can know the process' state and the time left to complete the delivery.

  Scenario: Successful delivery tracking
    Given I am on delivery tracking page
    And I have created a delivery with id "delivery-0"
    When I track the delivery with id "delivery-0"
    Then I should see the delivery state and the time left

  Scenario: Delivery tracking fails with invalid id
    Given I am on delivery tracking page
    And I have not a delivery with id "delivery-100" in my list
    When I track the delivery with id "delivery-100"
    Then I should see an error which says "Delivery does not exist"
    And I should not see the delivery state and the time left
