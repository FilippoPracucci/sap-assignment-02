package delivery_service.application;

import delivery_service.domain.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 
 * Implementation of the Delivery Service entry point at the application layer
 * 
 */
public class DeliveryServiceImpl implements DeliveryService, DeliveryObserver {
	static Logger logger = Logger.getLogger("[Delivery Service]");

    private DeliveryRepository deliveryRepository;
    private final TrackingSessions trackingSessionRepository;
	private final List<DeliveryServiceEventObserver> observers;

    public DeliveryServiceImpl() {
    	this.trackingSessionRepository = new TrackingSessions();
		this.observers = new ArrayList<>();
    }

	@Override
	public DeliveryDetail getDeliveryDetail(final DeliveryId deliveryId) throws DeliveryNotFoundException {
		logger.info("get delivery " + deliveryId + " detail");
		if (!this.deliveryRepository.isPresent(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		return this.deliveryRepository.getDelivery(deliveryId).getDeliveryDetail();
	}

	@Override
	public DeliveryStatus getDeliveryStatus(final DeliveryId deliveryId, final String trackingSessionId)
			throws DeliveryNotFoundException, TrackingSessionNotFoundException {
		logger.info("get delivery " + deliveryId.id() + " status");
        if (!this.deliveryRepository.isPresent(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		if (!this.trackingSessionRepository.isPresent(trackingSessionId)) {
			throw new TrackingSessionNotFoundException();
		}
		return this.deliveryRepository.getDelivery(deliveryId).getDeliveryStatus();
	}

	@Override
	public TrackingSession getTrackingSession(final String trackingSessionId) throws TrackingSessionNotFoundException {
		return this.trackingSessionRepository.getSession(trackingSessionId);
	}

	@Override
	public DeliveryId createNewDelivery(final double weight, final Address startingPlace,
										final Address destinationPlace, final Optional<Calendar> expectedShippingMoment) {
		final Delivery delivery = new DeliveryImpl(this.deliveryRepository.getNextId(), weight, startingPlace,
				destinationPlace, expectedShippingMoment);
		delivery.addDeliveryObserver(this);
		logger.info("create New Delivery " + delivery.getId().id());
        try {
            this.deliveryRepository.addDelivery(delivery);
        } catch (InvalidDeliveryIdException | DeliveryAlreadyPresentException e) {
            throw new RuntimeException(e);
        }
		this.observers.forEach(obs -> {
			obs.notifyNewDeliveryCreated();
			delivery.addDeliveryObserver(obs);
		});
        return delivery.getId();
	}

	@Override
	public TrackingSession trackDelivery(final DeliveryId deliveryId, final TrackingSessionEventObserver observer)
			throws DeliveryNotFoundException {
		logger.info("Track delivery " + deliveryId);
		final TrackingSession trackingSession = this.trackingSessionRepository.createSession();
		trackingSession.bindTrackingSessionEventNotifier(observer);
		this.deliveryRepository.getDelivery(deliveryId).addDeliveryObserver(trackingSession);
		return trackingSession;
	}

	@Override
	public void stopTrackingDelivery(final DeliveryId deliveryId, final String trackingSessionId)
			throws DeliveryNotFoundException, TrackingSessionNotFoundException {
		logger.info("Stop tracking delivery " + deliveryId);
		this.deliveryRepository.getDelivery(deliveryId)
				.removeDeliveryObserver(this.trackingSessionRepository.getSession(trackingSessionId));
		this.trackingSessionRepository.removeSession(trackingSessionId);
	}

	@Override
	public void notifyDeliveryEvent(final DeliveryEvent event) {
		logger.info("DeliveryService: event " + event);
		try {
			if (event instanceof Shipped) {
				this.deliveryRepository.updateDeliveryState(((Shipped) event).id(), DeliveryState.SHIPPING);
			} else if (event instanceof Delivered) {
				this.deliveryRepository.updateDeliveryState(((Delivered) event).id(), DeliveryState.DELIVERED);
			}
		} catch (final DeliveryNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void bindDeliveryRepository(final DeliveryRepository repo) {
		this.deliveryRepository = repo;
		this.deliveryRepository.getAllDeliveries().stream()
				.filter(delivery -> !delivery.getDeliveryStatus().getState().equals(DeliveryState.DELIVERED))
				.forEach(delivery -> delivery.addDeliveryObserver(this));
	}

	public void addObserver(final DeliveryServiceEventObserver obs) {
		this.observers.add(obs);
	}
}
