package api_gateway.application;

public class DeliveryNotFoundException extends Exception {

    public DeliveryNotFoundException() {
        super("Delivery does not exist");
    }
}
