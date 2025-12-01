package main.java.delivery_service.domain;

interface DroneObserver {

	void notifyDeliveryEvent(DeliveryEvent event);
}