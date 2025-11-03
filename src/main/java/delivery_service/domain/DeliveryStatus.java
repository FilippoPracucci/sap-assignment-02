package delivery_service.domain;

import common.ddd.Entity;

public interface DeliveryStatus extends Entity<DeliveryId> {

    DeliveryState getState();

    DeliveryTime getTimeLeft();
}