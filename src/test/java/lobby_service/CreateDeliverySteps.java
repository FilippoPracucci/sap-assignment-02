package lobby_service;

import account_service.application.AccountServiceImpl;
import account_service.infrastructure.AccountServiceController;
import account_service.infrastructure.FileBasedAccountRepository;
import delivery_service.application.DeliveryRepository;
import delivery_service.application.DeliveryServiceImpl;
import delivery_service.domain.Address;
import delivery_service.domain.DeliveryId;
import delivery_service.infrastructure.DeliveryServiceController;
import delivery_service.infrastructure.FileBasedDeliveryRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.Vertx;
import lobby_service.application.CreateDeliveryFailedException;
import lobby_service.application.LobbyServiceImpl;
import lobby_service.application.LoginFailedException;
import lobby_service.infrastructure.AccountServiceProxy;
import lobby_service.infrastructure.DeliveryServiceProxy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateDeliverySteps {

    private String currentPage = "";
    private String lastInfo = "";
    private String lastError = "";
    private String userSessionId = "";
    private String deliveryId = "";

	private final LobbyServiceImpl lobbyService;
    private final DeliveryRepository deliveryRepository;

	public CreateDeliverySteps(){
        this.lobbyService = new LobbyServiceImpl();
        this.lobbyService.bindAccountService(new AccountServiceProxy("http://localhost:9000"));
        var accountService = new AccountServiceImpl();
        accountService.bindAccountRepository(new FileBasedAccountRepository());
        Vertx.vertx().deployVerticle(new AccountServiceController(accountService, 9000));
        this.lobbyService.bindDeliveryService(new DeliveryServiceProxy("http://localhost:9002"));
        final var deliveryService = new DeliveryServiceImpl();
        this.deliveryRepository = new FileBasedDeliveryRepository();
        deliveryService.bindDeliveryRepository(this.deliveryRepository);
        Vertx.vertx().deployVerticle(new DeliveryServiceController(deliveryService, 9002));
	}
	
    /* Scenario: Successful delivery creation */

    @Given("I am on delivery creation page")
    public void iAmOnDeliveryCreationPage() {
        this.currentPage = "create-delivery";
    }

    @And("I am logged in as {string} with password {string}")
    public void iAmLoggedInAsWithPassword(final String userId, final String pwd) {
        try {
            this.userSessionId = this.lobbyService.login(userId, pwd);
        } catch (final LoginFailedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDelivery(
            final String weight,
            final String startingPlace,
            final String destinationPlace,
            final Optional<Calendar> targetTime
    ) {
        final String[] startingStreet = startingPlace.split(", ");
        final String[] destinationStreet = destinationPlace.split(", ");
        try {
            this.deliveryId = this.lobbyService.createNewDelivery(
                    this.userSessionId,
                    Double.parseDouble(weight),
                    new Address(startingStreet[0], Integer.parseInt(startingStreet[1])),
                    new Address(destinationStreet[0], Integer.parseInt(destinationStreet[1])),
                    targetTime
            ).id();
        } catch (final CreateDeliveryFailedException e) {
            this.lastError = e.getMessage();
        }
        this.lastInfo = "Delivery created";
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

    @Then("I should a confirmation that the delivery has been created and receive its identifier")
    public void iShouldAConfirmationThatTheDeliveryHasBeenCreatedAndReceiveItsIdentifier() {
        assertThat(this.lastInfo).isEqualTo("Delivery created");
        assertThat(this.deliveryRepository.isPresent(new DeliveryId(this.deliveryId)));
    }

    @When("I create a delivery with weight {string} kg, starting place {string}, destination place {string} to ship in {string} days")
    public void iCreateADeliveryWithWeightKgStartingPlaceDestinationPlaceToShipInDays(
            final String weight,
            final String startingPlace,
            final String destinationPlace,
            final String targetTime) {
        System.out.println("time: " + (new Calendar.Builder().setInstant(
                Date.from(Instant.now().plus(Integer.parseInt(targetTime), ChronoUnit.DAYS))).build()).toInstant());
        this.createDelivery(weight, startingPlace, destinationPlace,
                Optional.of(new Calendar.Builder().setInstant(
                        Date.from(Instant.now().plus(Integer.parseInt(targetTime), ChronoUnit.DAYS))).build()
                )
        );
    }

    @Then("I should see the error {string}")
    public void iShouldSeeTheError(final String message) {
        assertThat(this.lastError).startsWith(message);
    }

    @And("the delivery should not be created")
    public void theDeliveryShouldNotBeCreated() {
        assertThat(this.deliveryId).isEmpty();
    }
}
