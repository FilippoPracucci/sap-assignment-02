package delivery_service.domain;

import common.ddd.Entity;

import java.util.Date;

public interface DeliveryDetail extends Entity<DeliveryId> {

    double getWeight();

    Address getStartingPlace();

    Address getDestinationPlace();

    Date getExpectedShippingDate();
}
