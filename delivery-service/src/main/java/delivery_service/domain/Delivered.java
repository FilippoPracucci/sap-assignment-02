package delivery_service.domain;

import delivery_service.domain.DeliveryEvent;
import delivery_service.domain.DeliveryId;

public record Delivered(DeliveryId id) implements DeliveryEvent {
}
