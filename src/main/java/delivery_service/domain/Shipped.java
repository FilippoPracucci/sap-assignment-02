package delivery_service.domain;

public record Shipped(DeliveryId id) implements DeliveryEvent {
}
