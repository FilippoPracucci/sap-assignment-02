package delivery_service.domain;

interface Drone {

    void startDrone();

    void addDroneObserver(DroneObserver observer);
}
