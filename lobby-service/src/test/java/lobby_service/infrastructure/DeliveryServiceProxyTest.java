package lobby_service.infrastructure;

import delivery_service.application.DeliveryServiceMock;
import delivery_service.infrastructure.DeliveryServiceController;
import io.vertx.core.Vertx;
import lobby_service.application.DeliveryService;
import lobby_service.domain.Address;
import lobby_service.domain.DeliveryId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DeliveryServiceProxyTest {

    private final static Logger logger = Logger.getLogger("[DeliveryServiceProxyTest]");
    private static final String DELIVERY_SERVICE_ADDRESS = "http://localhost";
    private static final int DELIVERY_SERVICE_PORT = 9002;

    private DeliveryServiceController deliveryServiceController;
    private DeliveryServiceProxy proxy;
    private Vertx vertx;

    @BeforeEach
    public void setUp() {
        final Synchronizer sync = new Synchronizer();
        final DeliveryService deliveryService = new DeliveryServiceMock();
        this.vertx = Vertx.vertx();
        this.deliveryServiceController = new DeliveryServiceController(deliveryService, DELIVERY_SERVICE_PORT);
        vertx.deployVerticle(this.deliveryServiceController)
                .onSuccess((res) -> sync.notifySync());
        try {
            sync.awaitSync();
            logger.info("setup completed.");
        } catch (Exception ex) {
            logger.info("sync failed.");
            ex.printStackTrace();
        }
        this.proxy = new DeliveryServiceProxy(DELIVERY_SERVICE_ADDRESS + ":" + DELIVERY_SERVICE_PORT);
        logger.info("setup completed.");
    }

    @Test
    public void testCreateNewDelivery() {
        try {
            final DeliveryId deliveryId = this.proxy.createNewDelivery(
                    10.0,
                    new Address("Via Emilia", 1),
                    new Address("Via Roma", 20),
                    Optional.empty()
            );
            assertEquals("delivery-0", deliveryId.id());
        } catch (final Exception ex) {
            ex.printStackTrace();
            fail("Delivery creation failed.");
        }
    }

    @Test
    public void testTrackDelivery() {
        try {
            final String trackingSessionId = this.proxy.trackDelivery(new DeliveryId("delivery-0"));
            assertEquals("tracking-session-0", trackingSessionId);
        } catch (final Exception ex) {
            ex.printStackTrace();
            fail("Tracking delivery failed.");
        }
    }

    @AfterEach
    public void tearDown() {
        this.vertx.undeploy(this.deliveryServiceController.deploymentID());
    }
}
