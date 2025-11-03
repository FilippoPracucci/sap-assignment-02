package delivery_service.domain;

public record Delivered(DeliveryId id) implements DeliveryEvent {
}
