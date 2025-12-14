package account_service;

import account_service.application.AccountRepository;
import account_service.application.AccountServiceImpl;
import account_service.infrastructure.AccountServiceController;
import account_service.infrastructure.FileBasedAccountRepository;
import account_service.infrastructure.Synchronizer;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class SetupSteps {

    protected static final String DB_ACCOUNTS_FILE_NAME = "testAccounts.json";
    protected static final int ACCOUNT_SERVICE_PORT = 9000;
    protected static final String ACCOUNT_ENDPOINT = "http://localhost:" + ACCOUNT_SERVICE_PORT + "/api/v1";
    protected static final String ACCOUNTS_RESOURCE_PATH = ACCOUNT_ENDPOINT + "/accounts";

    @BeforeAll
    public static void setUp() {
        final AccountServiceImpl accountService = new AccountServiceImpl();
        final AccountRepository accountRepository = new FileBasedAccountRepository(DB_ACCOUNTS_FILE_NAME);
        accountService.bindAccountRepository(accountRepository);
        final AccountServiceController controller = new AccountServiceController(accountService, ACCOUNT_SERVICE_PORT);
        final Synchronizer sync = new Synchronizer();
        Vertx.vertx()
                .deployVerticle(controller)
                .onSuccess((res) -> sync.notifySync());
        try {
            sync.awaitSync();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        registerAccount("userName", "Secret#123");
    }

    private static void registerAccount(final String userName, final String password) {
        try {
            doPost(ACCOUNTS_RESOURCE_PATH, new JsonObject(Map.of(
                    "userName", userName,
                    "password", password)
            ));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected static HttpResponse<String> doPost(final String uri, final JsonObject body) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @AfterAll
    public static void resetTestEnvironment() {
        if (!new File(System.getProperty("user.dir") + File.separator + DB_ACCOUNTS_FILE_NAME).delete()) {
            Assertions.fail("Db file not deleted");
        }
    }
}
