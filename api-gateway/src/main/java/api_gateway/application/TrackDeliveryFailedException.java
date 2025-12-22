package api_gateway.application;

public class TrackDeliveryFailedException extends Exception {

    public TrackDeliveryFailedException(final String message) {
        super(message);
    }
}
