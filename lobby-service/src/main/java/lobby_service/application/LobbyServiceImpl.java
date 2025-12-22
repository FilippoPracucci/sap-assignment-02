package lobby_service.application;

import lobby_service.domain.*;

import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 
 * Implementation of the Service entry point at the application layer
 * 
 */
public class LobbyServiceImpl implements LobbyService {
	static Logger logger = Logger.getLogger("[Lobby Service]");
    
    private final UserSessions userSessionRepository = new UserSessions();
    private AccountService accountService;
    private DeliveryService deliveryService;
    
    @Override
	public String login(final UserId userId, final String password) throws LoginFailedException {
		logger.info("Login: " + userId.id() + " " + password);
		try {
			if (!this.accountService.isValidPassword(userId, password)) {
				throw new LoginFailedException("Wrong password");
			}
			return this.userSessionRepository.createSession(userId);
		} catch (final UserNotFoundException ex) {
			throw new LoginFailedException("Username does not exist");
		} catch (final ServiceNotAvailableException ex) {
			throw new LoginFailedException("Service not available");
		}
	}

	@Override
	public DeliveryId createNewDelivery(final String userSessionId, final double weight, final Address startingPlace,
										final Address destinationPlace, final Optional<Calendar> expectedShippingMoment)
			throws CreateDeliveryFailedException {
		try {
			if (this.userSessionRepository.isPresent(userSessionId)) {
				final DeliveryId deliveryId = this.deliveryService.createNewDelivery(weight, startingPlace,
						destinationPlace, expectedShippingMoment);
				logger.info("create new delivery " + deliveryId.id() + " by " + userSessionId);
				return deliveryId;
			} else {
				throw new CreateDeliveryFailedException("User is not logged in");
			}
		} catch (final ServiceNotAvailableException ex) {
			throw new CreateDeliveryFailedException("The service is not available");
		}
	}

	@Override
	public String trackDelivery(final String userSessionId, final DeliveryId deliveryId) throws TrackDeliveryFailedException {
		logger.info("Track delivery " + userSessionId + " " + deliveryId);
		try {
			if (this.userSessionRepository.isPresent(userSessionId)) {
				return this.deliveryService.trackDelivery(deliveryId);
			} else {
				throw new TrackDeliveryFailedException();
			}
		} catch (final ServiceNotAvailableException ex) {
			throw new TrackDeliveryFailedException();
		}
	}
	    
	public void bindAccountService(final AccountService service) {
		this.accountService = service;
	}

	public void bindDeliveryService(final DeliveryService service) {
		this.deliveryService = service;
	}

}
