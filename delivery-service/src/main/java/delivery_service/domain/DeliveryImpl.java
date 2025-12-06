package delivery_service.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DeliveryImpl implements Delivery, DroneObserver {

    private final DeliveryId id;
    private final DeliveryDetail deliveryDetail;
    private final MutableDeliveryStatus deliveryStatus;
    private final List<DeliveryObserver> observers;

    public DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Optional<Calendar> expectedShippingMoment,
            final DeliveryState deliveryState
    ) {
        this.id = deliveryId;
        this.deliveryDetail = new DeliveryDetailImpl(this.id, weight, startingPlace, destinationPlace,
                expectedShippingMoment.orElseGet(TimeConverter::getNowAsCalendar));
        this.deliveryStatus = new DeliveryStatusImpl(this.id);
        this.deliveryStatus.setDeliveryState(deliveryState);
        this.observers = new ArrayList<>();
        if (!deliveryState.equals(DeliveryState.DELIVERED)) {
            this.initDrone();
        }
    }

    public DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Optional<Calendar> expectedShippingMoment
    ) {
        this.id = deliveryId;
        this.deliveryDetail = new DeliveryDetailImpl(this.id, weight, startingPlace, destinationPlace,
                expectedShippingMoment.orElseGet(TimeConverter::getNowAsCalendar));
        this.deliveryStatus = new DeliveryStatusImpl(this.id);
        this.observers = new ArrayList<>();
        this.initDrone();
    }

    @Override
    public DeliveryDetail getDeliveryDetail() {
        return this.deliveryDetail;
    }

    @Override
    public DeliveryStatus getDeliveryStatus() {
        return this.deliveryStatus;
    }

    @Override
    public void updateDeliveryState(final DeliveryState deliveryState) {
        this.deliveryStatus.setDeliveryState(deliveryState);
    }

    @Override
    public void addDeliveryObserver(final DeliveryObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeDeliveryObserver(final DeliveryObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public DeliveryId getId() {
        return this.id;
    }

    @Override
    public void notifyDeliveryEvent(final DeliveryEvent event) {
        if (event instanceof Shipped) {
            this.deliveryStatus.setDeliveryState(DeliveryState.SHIPPING);
            this.deliveryStatus.setTimeLeft(((Shipped) event).timeLeft());
        } else if (event instanceof TimeElapsed) {
            this.deliveryStatus.subDeliveryTime(((TimeElapsed) event).time());
        } else if (event instanceof Delivered) {
            this.deliveryStatus.setDeliveryState(DeliveryState.DELIVERED);
        }
        this.observers.forEach(obs -> obs.notifyDeliveryEvent(event));
    }

    private void initDrone() {
        final Drone drone = new DroneImpl(this.deliveryDetail);
        drone.addDroneObserver(this);
        Thread.ofVirtual().start(() -> {
            try {
                if (this.deliveryDetail.expectedShippingMoment().toInstant().isAfter(TimeConverter.getNowAsInstant())) {
                    Thread.sleep(TimeConverter.getNowAsInstant().until(
                            this.deliveryDetail.expectedShippingMoment().toInstant(), ChronoUnit.MILLIS)
                    );
                }
                drone.startDrone();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
