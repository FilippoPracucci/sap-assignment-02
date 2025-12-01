package main.java.delivery_service.application;

public class DeliveryNotFoundException extends Exception {

    public DeliveryNotFoundException() {
        super("Delivery does not exist");
    }
}
