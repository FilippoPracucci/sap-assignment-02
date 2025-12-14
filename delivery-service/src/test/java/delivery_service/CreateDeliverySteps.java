package delivery_service;

import delivery_service.domain.Address;
import delivery_service.domain.DeliveryDetailImpl;
import delivery_service.domain.DeliveryId;
import delivery_service.domain.TimeConverter;
import delivery_service.infrastructure.DeliveryJsonConverter;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.json.JsonObject;

import java.net.http.HttpResponse;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateDeliverySteps {

    private String currentPage = "";
    private HttpResponse<String> response;

    /* Scenario: Successful delivery creation */

    @Given("I am on delivery creation page")
    public void iAmOnDeliveryCreationPage() {
        this.currentPage = "create-delivery";
    }

    private void createDelivery(
            final String weight,
            final String startingPlace,
            final String destinationPlace,
            final Optional<Calendar> expectedShippingMoment
    ) {
        assertThat(this.currentPage).isEqualTo("create-delivery");
        final String[] startingStreet = startingPlace.split(", ");
        final String[] destinationStreet = destinationPlace.split(", ");
        try {
            final JsonObject body = DeliveryJsonConverter.toJson(
                    new DeliveryDetailImpl(
                            new DeliveryId(""),
                            Double.parseDouble(weight),
                            new Address(startingStreet[0], Integer.parseInt(startingStreet[1])),
                            new Address(destinationStreet[0], Integer.parseInt(destinationStreet[1])),
                            expectedShippingMoment.orElse(TimeConverter.getNowAsCalendar())
                    )
            );
            body.remove("deliveryId");
            if (expectedShippingMoment.isEmpty()) {
                body.remove("expectedShippingMoment");
            }
            this.response = SetupSteps.doPost(SetupSteps.DELIVERIES_RESOURCE_PATH, body);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @When("I create a delivery with weight {string} kg, starting place {string}, destination place {string} to ship " +
            "immediately")
    public void iCreateADeliveryWithWeightStartingPlaceDestinationPlaceToShipImmediately(
            final String weight,
            final String startingPlace,
            final String destinationPlace
    ) {
        this.createDelivery(weight, startingPlace, destinationPlace, Optional.empty());
    }

    @Then("I should see a confirmation that the delivery has been created and receive its identifier")
    public void iShouldSeeAConfirmationThatTheDeliveryHasBeenCreatedAndReceiveItsIdentifier() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertTrue(new JsonObject(this.response.body()).containsKey("deliveryId"));
    }

    @When("I create a delivery with weight {string} kg, starting place {string}, destination place {string} to ship in {string} days")
    public void iCreateADeliveryWithWeightKgStartingPlaceDestinationPlaceToShipInDays(
            final String weight,
            final String startingPlace,
            final String destinationPlace,
            final String expectedShippingMoment) {
        this.createDelivery(weight, startingPlace, destinationPlace,
                Optional.of(GregorianCalendar.from(
                        TimeConverter.getNowAsZonedDateTime().plusDays(Integer.parseInt(expectedShippingMoment))
                ))
        );
    }

    @Then("I should see the error {string}")
    public void iShouldSeeTheError(final String message) {
        assertThat(new JsonObject(this.response.body()).containsKey("error") ? "Invalid shipping time" : "")
                .isEqualTo(message);
    }

    @And("the delivery should not be created")
    public void theDeliveryShouldNotBeCreated() {
        assertFalse(new JsonObject(this.response.body()).containsKey("deliveryId"));
    }
}
