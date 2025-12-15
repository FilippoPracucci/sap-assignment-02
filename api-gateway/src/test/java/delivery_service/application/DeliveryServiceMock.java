package delivery_service.application;

import api_gateway.domain.*;
import api_gateway.infrastructure.DeliveryServiceVertx;
import io.vertx.core.Vertx;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

public class DeliveryServiceMock implements DeliveryServiceVertx {

    private final DeliveryId deliveryId;

    public DeliveryServiceMock() {
        this.deliveryId = new DeliveryId("delivery-0");
    }

    @Override
    public DeliveryDetail getDeliveryDetail(final DeliveryId deliveryId) {
        return new DeliveryDetailImpl(
                this.deliveryId,
                10.0,
                new Address("Via Emilia", 1),
                new Address("Via Roma", 20),
                GregorianCalendar.from(ZonedDateTime.now(ZoneId.systemDefault()))
        );
    }

    @Override
    public DeliveryStatus getDeliveryStatus(final DeliveryId deliveryId, final String trackingSessionId) {
        return new DeliveryStatusImpl(this.deliveryId, DeliveryState.SHIPPING, Optional.empty());
    }

    @Override
    public void stopTrackingDelivery(final DeliveryId deliveryId, final String trackingSessionId) {

    }

    @Override
    public void createAnEventChannel(final String trackingSessionId, final Vertx vertx) {

    }
}
