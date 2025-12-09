package delivery_service.domain;

public record Shipped(DeliveryId id, DeliveryTime timeLeft) implements DeliveryEvent {
}
