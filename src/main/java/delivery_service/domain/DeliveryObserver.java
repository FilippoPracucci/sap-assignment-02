package delivery_service.domain;

public interface DeliveryObserver {

    void notifyDeliveryEvent(DeliveryEvent event);
}
