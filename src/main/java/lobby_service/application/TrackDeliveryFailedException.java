package lobby_service.application;

public class TrackDeliveryFailedException extends Exception {

    public TrackDeliveryFailedException(final String message) {
        super(message);
    }

    public TrackDeliveryFailedException() {
    }
}
