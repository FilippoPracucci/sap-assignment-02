package delivery_service.domain;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class MutableDeliveryStatusTest {

    private MutableDeliveryStatus deliveryStatus;
    private final DeliveryId id = new DeliveryId("delivery-0");

    @BeforeEach
    public void setUp() {
        this.deliveryStatus = new DeliveryStatusImpl(id);
    }

    @Test
    public void testSetUp() {
        assertAll(
                () -> assertEquals(this.id, this.deliveryStatus.getId()),
                () -> assertEquals(DeliveryState.READY_TO_SHIP , this.deliveryStatus.getState()),
                () -> assertFalse(this.deliveryStatus.isTimeLeftAvailable()),
                () -> assertThrows(DeliveryNotShippedYetException.class, this.deliveryStatus::getTimeLeft)
        );
    }

    @Test
    public void testAddDeliveryTime() {
        this.deliveryStatus.setTimeLeft(new DeliveryTime(0, 5));
        this.deliveryStatus.addDeliveryTime(new DeliveryTime(1, 3));
        try {
            assertEquals(new DeliveryTime(1, 8), this.deliveryStatus.getTimeLeft());
            this.deliveryStatus.addDeliveryTime(new DeliveryTime(0, 23));
            assertEquals(new DeliveryTime(2, 7), this.deliveryStatus.getTimeLeft());
        } catch (final DeliveryNotShippedYetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSubDeliveryTime() {
        this.deliveryStatus.setTimeLeft(new DeliveryTime(1, 5));
        this.deliveryStatus.subDeliveryTime(new DeliveryTime(1, 3));
        try {
            assertEquals(new DeliveryTime(0, 2), this.deliveryStatus.getTimeLeft());
            this.deliveryStatus.subDeliveryTime(new DeliveryTime(0, 3));
            assertEquals(new DeliveryTime(0, 0), this.deliveryStatus.getTimeLeft());
        } catch (final DeliveryNotShippedYetException e) {
            throw new RuntimeException(e);
        }
    }

}
