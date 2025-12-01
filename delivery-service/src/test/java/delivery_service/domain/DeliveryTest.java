package delivery_service.domain;

import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryTest {

    private static final double WEIGHT = 5.0;

    private final Address startingPlace = new Address("via Roma", 50);
    private final Address destinationPlace = new Address("via Piave", 25);
    private final DeliveryId id = new DeliveryId("delivery-0");

    @Test
    public void testDeliveryCreation() {
        final Delivery delivery = this.createDelivery();
        assertAll(
                () -> assertEquals(this.id, delivery.getId()),
                () -> assertEquals(WEIGHT, delivery.getDeliveryDetail().weight()),
                () -> assertEquals(this.startingPlace, delivery.getDeliveryDetail().startingPlace()),
                () -> assertEquals(this.destinationPlace, delivery.getDeliveryDetail().destinationPlace()),
                () -> assertEquals(DeliveryState.DELIVERED, delivery.getDeliveryStatus().getState())
        );
    }

    @Test
    public void testNotifyDeliveryEvent() {
        final var delivery = this.createDelivery();
        final DeliveryTime timeLeft = new DeliveryTime(0, 5);
        final DeliveryTime timeElapsed = new DeliveryTime(0, 1);
        try {
            delivery.notifyDeliveryEvent(new Shipped(this.id, timeLeft));
            assertEquals(DeliveryState.SHIPPING, delivery.getDeliveryStatus().getState());
            assertEquals(timeLeft, delivery.getDeliveryStatus().getTimeLeft());
            delivery.notifyDeliveryEvent(new TimeElapsed(this.id, timeElapsed));
            assertEquals(timeLeft.sub(timeElapsed), delivery.getDeliveryStatus().getTimeLeft());
            delivery.notifyDeliveryEvent(new Delivered(this.id));
            assertEquals(DeliveryState.DELIVERED, delivery.getDeliveryStatus().getState());
            assertFalse(delivery.getDeliveryStatus().isTimeLeftAvailable());
        } catch (final DeliveryNotShippedYetException e) {
            fail(e);
        }
    }

    private DeliveryImpl createDelivery() {
        return new DeliveryImpl(
                this.id,
                WEIGHT,
                this.startingPlace,
                this.destinationPlace,
                Optional.empty(),
                DeliveryState.DELIVERED
        );
    }
}
