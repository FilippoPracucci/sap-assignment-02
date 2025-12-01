package delivery_service.domain;

import common.ddd.Aggregate;
import delivery_service.domain.*;

public interface Delivery extends Aggregate<DeliveryId> {

    DeliveryDetail getDeliveryDetail();

    DeliveryStatus getDeliveryStatus();

    void updateDeliveryState(DeliveryState deliveryState);

    void addDeliveryObserver(DeliveryObserver observer);

    void removeDeliveryObserver(DeliveryObserver observer);
}
