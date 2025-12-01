package delivery_service.domain;

import delivery_service.domain.DeliveryEvent;
import delivery_service.domain.DeliveryTime;

public record TimeElapsed(DeliveryId id, DeliveryTime time) implements DeliveryEvent {
}
