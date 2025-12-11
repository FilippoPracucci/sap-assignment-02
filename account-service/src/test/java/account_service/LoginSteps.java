package account_service;

import account_service.application.AccountRepository;
import account_service.application.AccountServiceImpl;
import account_service.domain.UserId;
import account_service.infrastructure.AccountServiceController;
import account_service.infrastructure.FileBasedAccountRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.Vertx;
import lobby_service.application.LobbyServiceImpl;
import lobby_service.application.LoginFailedException;
import lobby_service.infrastructure.AccountServiceProxy;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private String currentPage = "";
    private String lastInfo = "";
    private String lastError = "";

	private final LobbyServiceImpl lobbyService;
    private final AccountRepository accountRepository;

	public LoginSteps(){
        this.lobbyService = new LobbyServiceImpl();
        this.lobbyService.bindAccountService(new AccountServiceProxy("http://localhost:9000"));
        this.accountRepository = new FileBasedAccountRepository();
        var accountService = new AccountServiceImpl();
        accountService.bindAccountRepository(this.accountRepository);
        Vertx.vertx().deployVerticle(new AccountServiceController(accountService, 9000));
	}
	
    /* Scenario: Successful login */
    
    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        this.currentPage = "login";
    }

    @Given("I have an account")
    public void iHaveAnAccount() {
    }

    private void login(final String userId, final String pwd) {
        try {
            this.lobbyService.login(new lobby_service.domain.UserId(userId), pwd);
        } catch (final LoginFailedException e) {
            this.lastError = e.getMessage();
            return;
        }
        this.lastInfo = "User logged in";
        this.currentPage = "home";
    }

    @When("I login with my userId {string} and my password {string}")
    public void iLoginWithMyUserIdAndMyPassword(final String userId, final String pwd) {
        assertThat(this.currentPage).isEqualTo("login");
        this.login(userId, pwd);
    }

    @Then("I should access to the system for delivering packages")
    public void iShouldAccessToTheSystemForDeliveringPackages() {
        assertThat(this.lastInfo).isEqualTo("User logged in");
        assertThat(this.currentPage).isEqualTo("home");
    }

    /* Scenario: Login fails without having an account */

    @When("I login with userId {string} and the password {string}")
    public void iLoginWithUserIdAndThePassword(final String userId, final String pwd) {
        assertThat(this.currentPage).isEqualTo("login");
        this.login(userId, pwd);
    }

    @Then("I should see an error {string}")
    public void iShouldSeeAnError(final String message) {
        assertThat(this.lastError).isEqualTo(message);
    }

    @And("I should not access to the system")
    public void iShouldNotAccessToTheSystem() {
        assertThat(this.currentPage).isEqualTo("login");
    }

    /* Scenario: Login fails with wrong password */

    @And("I have an account with userId {string} and password {string}")
    public void iHaveAnAccountWithUserIdAndPassword(final String userId, final String pwd) {
       assertThat(this.accountRepository.isPresent(new UserId(userId)));
       assertThat(this.accountRepository.isValid(new UserId(userId), pwd));
    }
}
