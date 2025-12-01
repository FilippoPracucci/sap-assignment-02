package account_service.application;

import account_service.domain.Account;
import common.hexagonal.InBoundPort;

@InBoundPort
public interface AccountService {

	/**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     */
	Account registerUser(String userName, String password);

	/**
     * 
     * Get account info.
     * 
     * @param userId
     * @return
     * @throws AccountNotFoundException
     */
	Account getAccountInfo(String userId) throws AccountNotFoundException;
		
	
	/**
	 * 
	 * Check password validity
	 * 
	 * @param userId
	 * @param password
	 * @return
	 * @throws AccountNotFoundException
	 */
	boolean isValidPassword(String userId, String password) throws AccountNotFoundException;

}
