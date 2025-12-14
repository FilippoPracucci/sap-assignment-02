Feature: User creates a delivery
  As a user,
  I want to send a package
  so that I can deliver objects to other places.

  Scenario: Successful delivery creation
    Given I am on delivery creation page
    When I create a delivery with weight "2" kg, starting place "via Emilia, 9", destination place "via Veneto, 5" to ship immediately
    Then I should see a confirmation that the delivery has been created and receive its identifier

  Scenario: Delivery creation fails with invalid shipping time
    Given I am on delivery creation page
    When I create a delivery with weight "2" kg, starting place "via Emilia, 9", destination place "via Veneto, 5" to ship in "30" days
    Then I should see the error "Invalid shipping time"
    And the delivery should not be created
