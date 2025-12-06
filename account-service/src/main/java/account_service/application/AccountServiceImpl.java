package account_service.application;

import account_service.domain.Account;
import account_service.domain.AccountImpl;
import account_service.domain.UserId;

import java.util.logging.Logger;

public class AccountServiceImpl implements AccountService {
	static Logger logger = Logger.getLogger("[Shipping on the air]");

	private AccountRepository accountRepository;
    
    @Override
	public Account registerUser(final String userName, final String password) {
		logger.info("Register User: " + userName + " " + password);
		var account = new AccountImpl(this.accountRepository.getNextId(), userName, password);
        try {
            this.accountRepository.addAccount(account);
        } catch (InvalidAccountIdException | AccountAlreadyPresentException e) {
            throw new RuntimeException(e);
        }
        return account;
	}

	@Override
	public Account getAccountInfo(final String userId) throws AccountNotFoundException {
		logger.info("Get account info: " + userId);
		if (!this.accountRepository.isPresent(new UserId(userId))) {
			throw new AccountNotFoundException();
		}
		return this.accountRepository.getAccount(new UserId(userId));
	}
    
		
	@Override
	public boolean isValidPassword(final String userId, final String password) throws AccountNotFoundException {
		logger.info("IsValid password " + userId + " - " + password);
		if (!this.accountRepository.isPresent(new UserId(userId))) {
			throw new AccountNotFoundException();
		}
		return this.accountRepository.isValid(new UserId(userId), password);
	}

    public void bindAccountRepository(final AccountRepository repo) {
    	this.accountRepository = repo;
    }


}
