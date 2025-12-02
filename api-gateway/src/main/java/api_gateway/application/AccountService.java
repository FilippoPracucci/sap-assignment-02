package api_gateway.application;

import api_gateway.domain.Account;
import api_gateway.domain.UserId;
import common.hexagonal.OutBoundPort;

@OutBoundPort
public interface AccountService {

	/**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     */
	Account registerUser(String userName, String password) throws ServiceNotAvailableException;

	/**
     * 
     * Get account info.
     * 
     * @param userId
     * @return
     * @throws AccountNotFoundException
     */
	Account getAccountInfo(UserId userId) throws AccountNotFoundException, ServiceNotAvailableException;
}
