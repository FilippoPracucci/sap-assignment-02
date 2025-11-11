package delivery_service.domain;

import common.ddd.Aggregate;

public interface Delivery extends Aggregate<DeliveryId> {

    DeliveryDetail getDeliveryDetail();

    void startTracking();

    void stopTracking();

    DeliveryState getDeliveryState();

    int getDaysLeft();

    void addDeliveryObserver(DeliveryObserver observer);
}
