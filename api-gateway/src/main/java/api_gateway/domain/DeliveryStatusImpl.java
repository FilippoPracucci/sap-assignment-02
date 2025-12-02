package api_gateway.domain;

import java.util.Optional;

public class DeliveryStatusImpl implements DeliveryStatus {

    private final DeliveryId id;
    private DeliveryState state;
    private Optional<DeliveryTime> timeLeft;

    public DeliveryStatusImpl(final DeliveryId id, final DeliveryState state, final Optional<DeliveryTime> timeLeft) {
        this.id = id;
        this.state = state;
        this.timeLeft = timeLeft;
    }

    @Override
    public DeliveryState getState() {
        return this.state;
    }

    @Override
    public Optional<DeliveryTime> getTimeLeft() {
        return this.timeLeft;
    }

    @Override
    public DeliveryId getId() {
        return this.id;
    }
}
