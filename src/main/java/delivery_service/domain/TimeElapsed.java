package delivery_service.domain;

public record TimeElapsed(DeliveryId id, DeliveryTime time) implements DeliveryEvent {
}
