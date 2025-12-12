package account_service;

import account_service.application.AccountRepository;
import account_service.application.AccountServiceImpl;
import account_service.domain.UserId;
import account_service.infrastructure.FileBasedAccountRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationSteps {

    private String currentPage = "";
    private String userId = "";
    private String lastInfo = "";

	private final AccountServiceImpl accountService;
    private final AccountRepository accountRepository;
    
	public RegistrationSteps(){
        this.accountService = new AccountServiceImpl();
        this.accountRepository = new FileBasedAccountRepository();
        this.accountService.bindAccountRepository(this.accountRepository);
	}
	
    /* Scenario: Successful registration */
    
    @Given("I am on the registration page")
    public void iAmOnTheRegistrationPage() {
        this.currentPage = "registration";
    }

    @Given("I have not an account")
    public void iHaveNotAnAccount() {
    }

    @When("I create an account with a username {string} and a valid password {string}")
    public void iCreateAnAccountWithAUsernameAndAValidPassword(final String username, final String pwd) {
        assertThat(this.currentPage).isEqualTo("registration");
        this.userId = this.accountService.registerUser(username, pwd).getId().id();
        this.lastInfo = "Account created";
    }

    @Then("I should see a confirmation that my account has been created and receive my identifier")
    public void iShouldSeeConfirmationAndReceiveMyIdentifier() {
        assertThat(this.lastInfo).isEqualTo("Account created");
        assertThat(this.accountRepository.isPresent(new UserId(this.userId)));
    }
}
