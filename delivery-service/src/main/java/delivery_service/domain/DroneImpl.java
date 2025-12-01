package delivery_service.domain;

import delivery_service.domain.*;

import java.util.ArrayList;
import java.util.List;

class DroneImpl implements Drone, Runnable {

    private static final int DURATION_MULTIPLIER = 5;
    private static final int HOUR_IN_SECONDS = 3600;
    private static final int HOURS_IN_A_DAY = 24;
    private static final int PERIOD_IN_HOURS = 1;

    private final List<DroneObserver> droneObservers;
    private final DeliveryDetail deliveryDetail;
    private final int deliveryDurationInHours;

    public DroneImpl(final DeliveryDetail deliveryDetail) {
        this.deliveryDetail = deliveryDetail;
        this.droneObservers = new ArrayList<>();
        this.deliveryDurationInHours = DURATION_MULTIPLIER * ((int) this.deliveryDetail.weight());
    }

    @Override
    public void startDrone() {
        Thread.ofVirtual().start(this);
    }

    @Override
    public void run() {
        this.notifyDeliveryEvent(new Shipped(
                this.deliveryDetail.getId(),
                new DeliveryTime(
                        this.deliveryDurationInHours / HOURS_IN_A_DAY,
                        this.deliveryDurationInHours % HOURS_IN_A_DAY
                )
        ));
        for (int i = 0; i < this.deliveryDurationInHours; i++) {
            try {
                Thread.sleep(PERIOD_IN_HOURS * HOUR_IN_SECONDS);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.notifyDeliveryEvent(new TimeElapsed(
                    this.deliveryDetail.getId(),
                    new DeliveryTime(0, PERIOD_IN_HOURS)
            ));
        }
        this.notifyDeliveryEvent(new Delivered(this.deliveryDetail.getId()));
    }

    @Override
    public void addDroneObserver(final DroneObserver observer) {
        this.droneObservers.add(observer);
    }

    private void notifyDeliveryEvent(final DeliveryEvent event) {
        this.droneObservers.forEach(obs -> obs.notifyDeliveryEvent(event));
    }
}
