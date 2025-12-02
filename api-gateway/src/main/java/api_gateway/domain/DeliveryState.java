package api_gateway.domain;

import java.util.Arrays;

public enum DeliveryState {
    READY_TO_SHIP("ready-to-ship"),
    SHIPPING("shipping"),
    DELIVERED("delivered");

    private final String label;

    DeliveryState(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static DeliveryState valueOfLabel(final String label) {
        return Arrays.stream(DeliveryState.values())
                .filter(deliveryState -> deliveryState.label.equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
