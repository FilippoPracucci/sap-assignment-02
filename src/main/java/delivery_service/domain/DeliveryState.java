package delivery_service.domain;

public enum DeliveryState {
    READY_TO_SHIP("ready-to-ship"),
    SHIPPING("shipping"),
    DELIVERED("delivered");

    final String label;

    DeliveryState(final String label) {
        this.label = label;
    }
}
