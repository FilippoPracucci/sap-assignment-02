package api_gateway.application;

public class ServiceNotAvailableException extends Exception {

    public ServiceNotAvailableException() {
        super("Service not available");
    }
}
