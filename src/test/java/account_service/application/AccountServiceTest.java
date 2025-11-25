package account_service.application;

import account_service.domain.Account;
import account_service.infrastructure.FileBasedAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {

    private AccountServiceImpl accountService;
    private Account account;

    @BeforeEach
    public void setUp() {
        this.accountService = new AccountServiceImpl();
        this.accountService.bindAccountRepository(new FileBasedAccountRepository());
        this.account = this.accountService.registerUser("John", "Secret#123");
    }

    @Test
    public void testRegisterUser() {
        try {
            assertEquals(account, this.accountService.getAccountInfo(account.getId().id()));
        } catch (final AccountNotFoundException e) {
            fail(e);
        }
    }

    @Test
    public void testIsValidPassword() {
        try {
            assertTrue(this.accountService.isValidPassword(account.getId().id(), account.getPassword()));
            assertFalse(this.accountService.isValidPassword(account.getId().id(), "password"));
        } catch (final AccountNotFoundException e) {
            fail(e);
        }
    }
}
