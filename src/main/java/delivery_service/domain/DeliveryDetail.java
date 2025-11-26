package delivery_service.domain;

import common.ddd.Entity;

import java.util.Calendar;

public interface DeliveryDetail extends Entity<DeliveryId> {

    double weight();

    Address startingPlace();

    Address destinationPlace();

    Calendar expectedShippingMoment();
}
