package lobby_service;

import account_service.application.AccountServiceImpl;
import account_service.infrastructure.AccountServiceController;
import account_service.infrastructure.SimpleFileBasedAccountRepository;
import delivery_service.application.*;
import delivery_service.domain.Address;
import delivery_service.domain.DeliveryId;
import delivery_service.domain.DeliveryStatus;
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
import lobby_service.application.TrackDeliveryFailedException;
import lobby_service.infrastructure.AccountServiceProxy;
import lobby_service.infrastructure.DeliveryServiceProxy;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class TrackDeliverySteps {

    private String currentPage = "";
    private String lastInfo = "";
    private String lastError = "";
    private String userSessionId = "";
    private String trackingSessionId = "";
    private String deliveryId = "";

    private final LobbyServiceImpl lobbyService;
    private final DeliveryServiceImpl deliveryService;
    private final DeliveryRepository deliveryRepository;

    public TrackDeliverySteps() {
        this.lobbyService = new LobbyServiceImpl();
        this.lobbyService.bindAccountService(new AccountServiceProxy("http://localhost:9000"));
        var accountService = new AccountServiceImpl();
        accountService.bindAccountRepository(new SimpleFileBasedAccountRepository());
        Vertx.vertx().deployVerticle(new AccountServiceController(accountService, 9000));
        this.lobbyService.bindDeliveryService(new DeliveryServiceProxy("http://localhost:9002"));
        this.deliveryService = new DeliveryServiceImpl();
        this.deliveryRepository = new FileBasedDeliveryRepository();
        this.deliveryService.bindDeliveryRepository(this.deliveryRepository);
        Vertx.vertx().deployVerticle(new DeliveryServiceController(this.deliveryService, 9002));
        this.createStubDelivery();
    }

    private void createStubDelivery() {
        try {
            this.userSessionId = this.lobbyService.login("user-1", "Secret#123");
            this.deliveryId = this.lobbyService.createNewDelivery(
                    this.userSessionId,
                    2.0,
                    new Address("via Emilia", 9),
                    new Address("via Veneto", 5),
                    Optional.empty()
            ).id();
        } catch (final LoginFailedException | CreateDeliveryFailedException e) {
            Assertions.fail(e);
        }
        this.lastInfo = "Delivery created";
    }

    /* Scenario: Successful delivery tracking */

    @Given("I am on delivery tracking page")
    public void iAmOnDeliveryTrackingPage() {
        this.currentPage = "track-delivery";
    }

    @And("I have created a delivery with id {string}")
    public void iHaveCreatedADeliveryWithId(final String deliveryId) {
        this.deliveryId = deliveryId;
        assertThat(this.deliveryRepository.isPresent(new DeliveryId(this.deliveryId)));
    }

    @When("I track the delivery with id {string}")
    public void iTrackTheDeliveryWithId(final String deliveryId) {
        try {
            this.trackingSessionId = this.lobbyService.trackDelivery(this.userSessionId,
                    new lobby_service.domain.DeliveryId(deliveryId));
        } catch (final TrackDeliveryFailedException e) {
            this.lastError = e.getMessage();
        }
        this.lastInfo = deliveryId + " tracked";
    }

    @Then("I should see the delivery state and the time left")
    public void iShouldSeeTheDeliveryStateAndTheTimeLeft() {
        try {
            final DeliveryStatus status = this.deliveryService.getDeliveryStatus(new DeliveryId(this.deliveryId),
                    this.trackingSessionId);
            assertThat(status).isNotNull();
        } catch (final DeliveryNotFoundException | TrackingSessionNotFoundException e) {
            Assertions.fail(e);
        }
        assertThat(this.lastError).isEqualTo("");
    }

    /* Scenario: Delivery tracking fails with invalid id */

    @And("I have not a delivery with id {string} in my list")
    public void iHaveNotADeliveryWithIdInMyList(final String deliveryId) {
        this.deliveryId = deliveryId;
        assertThat(!this.deliveryRepository.isPresent(new DeliveryId(this.deliveryId)));
    }

    @Then("I should see an error which says {string}")
    public void iShouldSeeAnErrorWhichSays(final String error) {
        assertThat(this.lastError).isEqualTo(error);
    }

    @And("I should not see the delivery state and the time left")
    public void iShouldNotSeeTheDeliveryStateAndTheTimeLeft() {
        Assertions.assertThrows(DeliveryNotFoundException.class, () ->
                this.deliveryService.getDeliveryStatus(new DeliveryId(this.deliveryId), this.trackingSessionId));
    }
}
