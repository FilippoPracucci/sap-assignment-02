package main.java.lobby_service.application;

import common.hexagonal.OutBoundPort;
import lobby_service.application.ServiceNotAvailableException;
import lobby_service.application.UserNotFoundException;
import lobby_service.domain.UserId;

@OutBoundPort

public interface AccountService {
	
	/**
	 * 
	 * Check password validity
	 *
	 * @param userId
	 * @param password
	 * @return
	 * @throws UserNotFoundException
	 */
	boolean isValidPassword(UserId userId, String password) throws UserNotFoundException, ServiceNotAvailableException;

    
}
