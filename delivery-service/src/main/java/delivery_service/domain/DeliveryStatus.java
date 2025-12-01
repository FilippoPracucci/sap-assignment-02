package main.java.delivery_service.domain;

import common.ddd.Entity;
import delivery_service.domain.DeliveryNotShippedYetException;

public interface DeliveryStatus extends Entity<DeliveryId> {

    DeliveryState getState();

    DeliveryTime getTimeLeft() throws DeliveryNotShippedYetException;

    boolean isTimeLeftAvailable();
}