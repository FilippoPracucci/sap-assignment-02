package account_service;

import account_service.application.AccountRepository;
import account_service.application.AccountServiceImpl;
import account_service.infrastructure.AccountServiceController;
import account_service.infrastructure.FileBasedAccountRepository;
import account_service.infrastructure.Synchronizer;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationSteps {

    private String currentPage = "";
    private HttpResponse<String> response;

    private static final String ACCOUNT_ENDPOINT = "http://localhost:9000/api/v1";
    static final String ACCOUNTS_RESOURCE_PATH = ACCOUNT_ENDPOINT + "/accounts";

    private final AccountServiceController controller;
    
	public RegistrationSteps() {
        final AccountServiceImpl accountService = new AccountServiceImpl();
        final AccountRepository accountRepository = new FileBasedAccountRepository();
        accountService.bindAccountRepository(accountRepository);
        this.controller = new AccountServiceController(accountService, 9000);
	}

    @Before
    public void setUp() {
        final Synchronizer sync = new Synchronizer();
        Vertx.vertx()
                .deployVerticle(this.controller)
                .onSuccess((res) -> sync.notifySync());
        try {
            sync.awaitSync();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
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
        try {
             this.response = this.doPost(ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", username,
                    "password", pwd)
            ));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Then("I should see a confirmation that my account has been created and receive my identifier")
    public void iShouldSeeConfirmationAndReceiveMyIdentifier() {
        assertThat(this.response.statusCode()).isEqualTo(200);
        assertTrue(new JsonObject(this.response.body()).containsKey("accountId"));
    }

    private HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
