package delivery_service.application;

import delivery_service.domain.Address;
import delivery_service.domain.DeliveryDetail;
import delivery_service.domain.DeliveryId;
import delivery_service.infrastructure.FileBasedDeliveryRepository;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryServiceTest {

    private static final double WEIGHT = 5.0;

    private final Address startingPlace = new Address("via Roma", 50);
    private final Address destinationPlace = new Address("via Piave", 25);
    private DeliveryServiceImpl deliveryService;
    private DeliveryId id;

    @BeforeEach
    public void setUp() {
        this.deliveryService = new DeliveryServiceImpl();
        this.deliveryService.bindDeliveryRepository(new FileBasedDeliveryRepository());
        this.id = this.deliveryService.createNewDelivery(WEIGHT, this.startingPlace, this.destinationPlace,
                Optional.empty());
    }

    @Test
    public void testCreateNewDelivery() {
        try {
            final DeliveryDetail deliveryDetail = this.deliveryService.getDeliveryDetail(this.id);
            assertAll(
                    () -> assertEquals(this.id, deliveryDetail.getId()),
                    () -> assertEquals(WEIGHT, deliveryDetail.weight()),
                    () -> assertEquals(this.startingPlace, deliveryDetail.startingPlace()),
                    () -> assertEquals(this.destinationPlace, deliveryDetail.destinationPlace())
            );
        } catch (final DeliveryNotFoundException e) {
            fail(e);
        }
    }

    @Test
    public void testTrackDelivery() {
        try {
            final TrackingSession trackingSession = this.deliveryService.trackDelivery(this.id, null);
            assertEquals(this.deliveryService.getTrackingSession(trackingSession.getId()), trackingSession);
        } catch (final DeliveryNotFoundException | TrackingSessionNotFoundException e) {
            fail(e);
        }
    }

    @Test
    public void testStopTrackingDelivery() {
        try {
            final TrackingSession trackingSession = this.deliveryService.trackDelivery(this.id, null);
            this.deliveryService.stopTrackingDelivery(this.id, trackingSession.getId());
            assertThrows(TrackingSessionNotFoundException.class,
                    () -> this.deliveryService.getTrackingSession(trackingSession.getId()));
        } catch (final DeliveryNotFoundException | TrackingSessionNotFoundException e) {
            fail(e);
        }
    }
}
