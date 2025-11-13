package delivery_service.application;

import delivery_service.domain.*;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Implementation of the Delivery Service entry point at the application layer
 * 
 */
public class DeliveryServiceImpl implements DeliveryService {
	static Logger logger = Logger.getLogger("[Delivery Service]");

    private DeliveryRepository deliveryRepository;
    private final TrackingSessions trackingSessionRepository;

    public DeliveryServiceImpl() {
    	this.trackingSessionRepository = new TrackingSessions();
    }

	@Override
	public DeliveryDetail getDeliveryDetail(final DeliveryId deliveryId) throws DeliveryNotFoundException {
		logger.log(Level.INFO, "get delivery " + deliveryId + " detail");
		if (!this.deliveryRepository.isPresent(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		return this.deliveryRepository.getDelivery(deliveryId).getDeliveryDetail();
	}

	@Override
	public TrackingSession getTrackingSession(final String trackingSessionId) {
		return this.trackingSessionRepository.getTrackingSession(trackingSessionId);
	}

	@Override
	public DeliveryId createNewDelivery(final double weight, final Address startingPlace,
										final Address destinationPlace, final Calendar expectedShippingDate) {
		final Delivery delivery = new DeliveryImpl(this.deliveryRepository.getNextId(), weight, startingPlace,
				destinationPlace, expectedShippingDate);
		logger.log(Level.INFO, "create New Delivery " + delivery.getId().id());
        try {
            this.deliveryRepository.addDelivery(delivery);
        } catch (InvalidDeliveryIdException | DeliveryAlreadyPresentException e) {
            throw new RuntimeException(e);
        }
        return delivery.getId();
	}

	@Override
	public TrackingSession trackDelivery(final DeliveryId deliveryId, final TrackingSessionEventObserver observer)
			throws InvalidTrackingException {
		logger.log(Level.INFO, "Track delivery " + deliveryId);
        final Delivery delivery;
        try {
            delivery = this.deliveryRepository.getDelivery(deliveryId);
        } catch (final DeliveryNotFoundException e) {
            throw new InvalidTrackingException();
        }
        delivery.startTracking();
		var trackingSession = this.trackingSessionRepository.createSession();
		trackingSession.bindTrackingSessionEventNotifier(observer);
		delivery.addDeliveryObserver(trackingSession);
		return trackingSession;
	}
	
    public void bindDeliveryRepository(final DeliveryRepository repo) {
    	this.deliveryRepository = repo;
    }
}
