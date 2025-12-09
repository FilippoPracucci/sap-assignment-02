package delivery_service.domain;

import common.ddd.DomainEvent;

public interface DeliveryEvent extends DomainEvent {

    DeliveryId id();
}
