package main.java.delivery_service.domain;

public interface DeliveryObserver {

    void notifyDeliveryEvent(DeliveryEvent event);
}
