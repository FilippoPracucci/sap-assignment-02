package account_service.infrastructure;

import account_service.application.AccountAlreadyPresentException;
import account_service.application.AccountNotFoundException;
import account_service.application.AccountRepository;
import account_service.application.InvalidAccountIdException;
import account_service.domain.Account;
import account_service.domain.AccountImpl;
import account_service.domain.UserId;
import common.hexagonal.Adapter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * 
 * A simple file-based implementation of the AccountRepository.
 * 
 */
@Adapter
public class FileBasedAccountRepository implements AccountRepository {
	static Logger logger = Logger.getLogger("[AccountDB]");

	private static final String USER_PREFIX = "user-";

	/* db file */
	static final String DB_ACCOUNTS_PATH = System.getProperty("user.dir") + File.separator;

	private final String dbAccountsFile;
	private final HashMap<UserId, Account> userAccounts;
	
	public FileBasedAccountRepository(final String fileName) {
		this.dbAccountsFile = DB_ACCOUNTS_PATH + fileName;
		this.userAccounts = new HashMap<>();
		initFromDB();
	}

	@Override
	public UserId getNextId() {
		return new UserId(USER_PREFIX + (this.userAccounts.keySet().stream()
				.map(userId -> Integer.valueOf(userId.id().split("^" + USER_PREFIX)[1]))
				.max(Integer::compareTo).orElse(-1) + 1));
	}

	@Override
	public void addAccount(final Account account) throws InvalidAccountIdException, AccountAlreadyPresentException {
		if (!account.getId().id().matches("^" + USER_PREFIX + "\\d+$")) {
			throw new InvalidAccountIdException();
		}
		if (this.isPresent(account.getId())) {
			throw new AccountAlreadyPresentException();
		}
		this.userAccounts.put(account.getId(), account);
		saveOnDB();
	}

	@Override
	public boolean isPresent(final UserId userId) {
		return this.userAccounts.containsKey(userId);
	}
	
	@Override
	public Account getAccount(final UserId userId) throws AccountNotFoundException {
		if (!this.userAccounts.containsKey(userId)) {
			throw new AccountNotFoundException();
		}
		return this.userAccounts.get(userId);
	}

	@Override
	public boolean isValid(final UserId userId, final String password) {
		return (this.userAccounts.containsKey(userId) && this.userAccounts.get(userId).getPassword().equals(password));
	}

	private void initFromDB() {
		try {
			var accountsDB = new BufferedReader(new FileReader(dbAccountsFile));
			var sb = new StringBuilder();
			while (accountsDB.ready()) {
				sb.append(accountsDB.readLine()).append("\n");
			}
			accountsDB.close();
			var array = new JsonArray(sb.toString());
			for (int i = 0; i < array.size(); i++) {
				var user = array.getJsonObject(i);
				Account acc = new AccountImpl(new UserId(user.getString("userId")),
						user.getString("userName"), user.getString("password"));
				this.userAccounts.put(acc.getId(), acc);
			}
		} catch (Exception ex) {
			logger.info("DB not found, creating an empty one.");
			saveOnDB();
		}
	}
	
	private void saveOnDB() {
		try {
			JsonArray list = new JsonArray();
			for (Account ac: this.userAccounts.values()) {
				var obj = new JsonObject();
				obj.put("userId", ac.getId().id());
				obj.put("userName", ac.getUserName());
				obj.put("password", ac.getPassword());
				obj.put("whenCreated", ac.getWhenCreated());
				list.add(obj);
			}
			var usersDB = new FileWriter(dbAccountsFile);
			usersDB.append(list.encodePrettily());
			usersDB.flush();
			usersDB.close();
		} catch (Exception ex) {
			logger.severe(ex.getMessage());
		}	
	}
	
}
