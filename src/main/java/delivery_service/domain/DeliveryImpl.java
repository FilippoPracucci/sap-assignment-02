package delivery_service.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class DeliveryImpl implements Delivery {

    private final DeliveryId id;
    private final DeliveryDetail deliveryDetail;
    private final DeliveryStatus deliveryStatus;
    private final List<DeliveryObserver> observers;
    private boolean isInTracking;

    public DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Date expectedShippingDate
    ) {
       this(deliveryId, weight, startingPlace, destinationPlace, Optional.of(expectedShippingDate));
    }

    public DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace
    ) {
        this(deliveryId, weight, startingPlace, destinationPlace, Optional.empty());
    }

    private DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Optional<Date> expectedShippingDate
    ) {
        this.id = deliveryId;
        this.deliveryDetail = expectedShippingDate
                .map(date -> new DeliveryDetailImpl(this.id, weight, startingPlace, destinationPlace, date))
                .orElseGet(() -> new DeliveryDetailImpl(this.id, weight, startingPlace, destinationPlace));
        this.deliveryStatus = null;
        this.observers = new ArrayList<>();
        // TODO: add the drone
    }

    @Override
    public void startTracking() {
        this.isInTracking = true;
    }

    @Override
    public void stopTracking() {
        this.isInTracking = false;
    }

    @Override
    public DeliveryState getDeliveryState() {
        return this.deliveryStatus.getState();
    }

    @Override
    public int getDaysLeft() {
        return this.deliveryStatus.getTimeLeft().days();
    }

    @Override
    public void addDeliveryObserver(final DeliveryObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public DeliveryId getId() {
        return this.id;
    }

    private void notifyDeliveryEvent(final DeliveryEvent event) {
        if (this.isInTracking) {
            this.observers.forEach(o -> o.notifyDeliveryEvent(event));
        }
    }
}
