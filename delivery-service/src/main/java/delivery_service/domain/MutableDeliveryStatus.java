package main.java.delivery_service.domain;

interface MutableDeliveryStatus extends DeliveryStatus {

    void setDeliveryState(DeliveryState state);

    void setTimeLeft(DeliveryTime timeLeft);

    void addDeliveryTime(DeliveryTime timeLeftToAdd);

    void subDeliveryTime(DeliveryTime timeLeftToSub);
}
