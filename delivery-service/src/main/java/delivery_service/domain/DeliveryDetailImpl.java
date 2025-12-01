package main.java.delivery_service.domain;

import java.util.Calendar;

public record DeliveryDetailImpl(
        DeliveryId id,
        double weight,
        Address startingPlace,
        Address destinationPlace,
        Calendar expectedShippingMoment
) implements DeliveryDetail {

    @Override
    public DeliveryId getId() {
        return this.id();
    }
}
