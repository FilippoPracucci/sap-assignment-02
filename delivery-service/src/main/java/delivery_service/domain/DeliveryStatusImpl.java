package delivery_service.domain;

import java.util.Optional;

class DeliveryStatusImpl implements MutableDeliveryStatus {

    private final DeliveryId id;
    private DeliveryState state;
    private Optional<DeliveryTime> timeLeft;

    public DeliveryStatusImpl(final DeliveryId id) {
        this.id = id;
        this.state = DeliveryState.READY_TO_SHIP;
        this.timeLeft = Optional.empty();
    }

    @Override
    public DeliveryState getState() {
        return this.state;
    }

    @Override
    public DeliveryTime getTimeLeft() throws DeliveryNotShippedYetException {
        return this.timeLeft.orElseThrow(DeliveryNotShippedYetException::new);
    }

    @Override
    public boolean isTimeLeftAvailable() {
        return this.timeLeft.isPresent();
    }

    @Override
    public DeliveryId getId() {
        return this.id;
    }

    @Override
    public void setDeliveryState(final DeliveryState state) {
        this.state = state;
        if (this.state.equals(DeliveryState.DELIVERED)) {
            this.timeLeft = Optional.empty();
        }
    }

    @Override
    public void setTimeLeft(final DeliveryTime timeLeft) {
        this.timeLeft = Optional.of(timeLeft);
    }

    @Override
    public void addDeliveryTime(final DeliveryTime timeLeftToAdd) {
        this.timeLeft.ifPresent(t -> this.timeLeft = Optional.of(t.add(timeLeftToAdd)));
    }

    @Override
    public void subDeliveryTime(final DeliveryTime timeLeftToSub) {
        this.timeLeft.ifPresent(t -> this.timeLeft = Optional.of(t.sub(timeLeftToSub)));
    }
}
