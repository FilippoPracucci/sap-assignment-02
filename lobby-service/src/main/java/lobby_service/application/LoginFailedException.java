package main.java.lobby_service.application;

public class LoginFailedException extends Exception {

    public LoginFailedException(final String message) {
        super(message);
    }
}
