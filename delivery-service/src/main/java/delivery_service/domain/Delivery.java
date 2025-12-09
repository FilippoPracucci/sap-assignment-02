package delivery_service.domain;

import common.ddd.Aggregate;

public interface Delivery extends Aggregate<DeliveryId> {

    DeliveryDetail getDeliveryDetail();

    DeliveryStatus getDeliveryStatus();

    void addDeliveryObserver(DeliveryObserver observer);

    void removeDeliveryObserver(DeliveryObserver observer);

    void applyEvent(DeliveryEvent event);

    void startDeliveringProcess();
}
