package delivery_service.domain;

public record DeliveryCreated(DeliveryId id, DeliveryDetail deliveryDetail) implements DeliveryEvent {
}
