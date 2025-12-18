package api_gateway.application;

public class ServiceNotAvailableException extends Exception {

    private boolean isCircuitOpen;

    public ServiceNotAvailableException() {
        super("Service not available");
    }

    public ServiceNotAvailableException(final boolean isCircuitOpen) {
        this();
        this.isCircuitOpen = isCircuitOpen;
    }

    public boolean isCircuitOpen() {
        return this.isCircuitOpen;
    }
}
