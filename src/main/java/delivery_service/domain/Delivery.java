package delivery_service.domain;

import common.ddd.Aggregate;

public interface Delivery extends Aggregate<DeliveryId> {

    void trackDelivery();   // userId

    String getDeliveryState();

    int getDaysLeft();

    void addDeliveryObserver(DeliveryObserver observer);
}
