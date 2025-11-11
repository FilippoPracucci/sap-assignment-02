package delivery_service.domain;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public record DeliveryDetailImpl(
        DeliveryId id,
        double weight,
        Address startingPlace,
        Address destinationPlace,
        Calendar expectedShippingDate
) implements DeliveryDetail {

    public DeliveryDetailImpl(
            final DeliveryId id,
            final double weight,
            final Address startingPlace,
            final Address destinationPlace
    ) {
        this(id, weight, startingPlace, destinationPlace,
                new Calendar.Builder().setInstant(Date.from(Instant.now())).build());
    }

    @Override
    public DeliveryId getId() {
        return this.id();
    }
}
