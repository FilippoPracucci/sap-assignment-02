package lobby_service.application;

public class CreateDeliveryFailedException extends Exception {

    public CreateDeliveryFailedException(final String message) {
        super(message);
    }

    public CreateDeliveryFailedException() {
    }
}
