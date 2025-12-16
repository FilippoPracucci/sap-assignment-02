package account_service.application;

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException() {
        super("Account not present");
    }
}
