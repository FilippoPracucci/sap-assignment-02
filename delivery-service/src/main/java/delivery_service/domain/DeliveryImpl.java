package delivery_service.domain;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class DeliveryImpl implements Delivery, DroneObserver {

    private final DeliveryId id;
    private DeliveryDetail deliveryDetail;
    private MutableDeliveryStatus deliveryStatus;
    private final List<DeliveryObserver> observers;

    public DeliveryImpl(final DeliveryId deliveryId) {
        this.id = deliveryId;
        this.observers = new ArrayList<>();
    }

    public DeliveryImpl(
            final DeliveryId deliveryId,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Optional<Calendar> expectedShippingMoment,
            final List<DeliveryObserver> observers
    ) {
        this.id = deliveryId;
        this.deliveryDetail = new DeliveryDetailImpl(this.id, weight, startingPlace, destinationPlace,
                expectedShippingMoment.orElseGet(TimeConverter::getNowAsCalendar));
        this.deliveryStatus = new DeliveryStatusImpl(this.id);
        this.observers = new ArrayList<>(observers);
        this.observers.forEach(obs ->
                obs.notifyDeliveryEvent(new DeliveryCreated(this.id, this.deliveryDetail)));
        this.startDeliveringProcess();
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
    public synchronized void addDeliveryObserver(final DeliveryObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public synchronized void removeDeliveryObserver(final DeliveryObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void applyEvent(final DeliveryEvent event) {
        switch (event) {
            case DeliveryCreated deliveryCreated -> {
                this.deliveryDetail = deliveryCreated.deliveryDetail();
                this.deliveryStatus = new DeliveryStatusImpl(event.id());
            }
            case Shipped shipped -> {
                this.deliveryStatus.setDeliveryState(DeliveryState.SHIPPING);
                this.deliveryStatus.setTimeLeft(shipped.timeLeft());
            }
            case TimeElapsed timeElapsed -> this.deliveryStatus.subDeliveryTime(timeElapsed.time());
            case Delivered delivered -> this.deliveryStatus.setDeliveryState(DeliveryState.DELIVERED);
            default -> throw new IllegalArgumentException("Event type not supported");
        }
    }

    @Override
    public DeliveryId getId() {
        return this.id;
    }

    @Override
    public synchronized void notifyDeliveryEvent(final DeliveryEvent event) {
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

    @Override
    public void startDeliveringProcess() {
        Optional<DeliveryTime> deliveryTime = Optional.empty();
        try {
            deliveryTime = Optional.of(this.deliveryStatus.getTimeLeft());
        } catch (final DeliveryNotShippedYetException ignored) {
        }
        final Drone drone = new DroneImpl(this.deliveryDetail, deliveryTime);
        drone.addDroneObserver(this);
        Thread.ofVirtual().start(() -> {
            try {
                if (TimeConverter.getZonedDateTime(this.deliveryDetail.expectedShippingMoment())
                        .isAfter(TimeConverter.getNowAsZonedDateTime())) {
                    Thread.sleep(TimeConverter.getNowAsZonedDateTime().until(
                            TimeConverter.getZonedDateTime(this.deliveryDetail.expectedShippingMoment()),
                            ChronoUnit.MILLIS
                    ));
                }
                drone.startDrone();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
