package api_gateway.application;

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException() {
        super("Account not present");
    }
}
