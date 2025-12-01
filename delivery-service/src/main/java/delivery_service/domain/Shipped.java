package delivery_service.domain;

import delivery_service.domain.DeliveryEvent;
import delivery_service.domain.DeliveryId;
import delivery_service.domain.DeliveryTime;

public record Shipped(DeliveryId id, DeliveryTime timeLeft) implements DeliveryEvent {
}
