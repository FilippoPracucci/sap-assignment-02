package delivery_service.domain;

public interface DroneObserver {

	void notifyDeliveryEvent(DeliveryEvent event);
}