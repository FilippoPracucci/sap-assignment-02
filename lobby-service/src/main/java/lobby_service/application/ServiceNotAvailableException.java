package lobby_service.application;

public class ServiceNotAvailableException extends Exception {

    public ServiceNotAvailableException() {
        super("Service not available");
    }

}
