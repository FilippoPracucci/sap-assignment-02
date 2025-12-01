package delivery_service.domain;

interface DroneObserver {

	void notifyDeliveryEvent(DeliveryEvent event);
}