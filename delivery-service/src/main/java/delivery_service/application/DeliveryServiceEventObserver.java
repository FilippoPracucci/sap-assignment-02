package delivery_service.application;

import common.hexagonal.OutBoundPort;
import delivery_service.domain.DeliveryObserver;

@OutBoundPort
public interface DeliveryServiceEventObserver extends DeliveryObserver {

	void notifyNewDeliveryCreated();
}