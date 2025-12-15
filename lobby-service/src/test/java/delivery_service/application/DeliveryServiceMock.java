package delivery_service.application;

import lobby_service.application.CreateDeliveryFailedException;
import lobby_service.application.DeliveryService;
import lobby_service.application.ServiceNotAvailableException;
import lobby_service.application.TrackDeliveryFailedException;
import lobby_service.domain.Address;
import lobby_service.domain.DeliveryId;

import java.util.Calendar;
import java.util.Optional;

public class DeliveryServiceMock implements DeliveryService {

    @Override
    public DeliveryId createNewDelivery(
            final double weight,
            final Address startingPlace,
            final Address destinationPlace,
            final Optional<Calendar> expectedShippingMoment
    ) throws CreateDeliveryFailedException,ServiceNotAvailableException {
        return new DeliveryId("delivery-0");
    }

    @Override
    public String trackDelivery(final DeliveryId deliveryId) throws TrackDeliveryFailedException,
            ServiceNotAvailableException {
        return "tracking-session-0";
    }
}
