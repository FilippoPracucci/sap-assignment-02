package main.java.delivery_service.domain;

import delivery_service.domain.DroneObserver;

interface Drone {

    void startDrone();

    void addDroneObserver(DroneObserver observer);
}
