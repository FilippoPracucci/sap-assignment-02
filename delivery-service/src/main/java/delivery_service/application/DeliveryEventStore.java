package delivery_service.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import delivery_service.domain.DeliveryEvent;
import delivery_service.domain.DeliveryId;

import java.util.List;
import java.util.Map;

@OutBoundPort
public interface DeliveryEventStore extends Repository {

    void storeDeliveryEvent(DeliveryEvent event);

    Map<DeliveryId, List<DeliveryEvent>> retrieveDeliveryEvents();

    DeliveryId getNextId();

}
