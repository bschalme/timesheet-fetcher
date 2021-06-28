package timesheet.fetcher.adapter.out.gateway;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpRequest.POST;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import timesheet.fetcher.QbdApiConfiguration;
import timesheet.fetcher.application.port.out.QbdApiPort;
import timesheet.fetcher.domain.QbdTimesheetEntries;

@Singleton
@Slf4j
@Requires(env = "localhost")
public class QbdApiGateway implements QbdApiPort {
    private final RxHttpClient httpClient;
    private final URI uri;
    private final QbdApiConfiguration configuration;

    @Inject
    private ObjectMapper objectMapper;

    public QbdApiGateway(@Client("${qbd-api.api-url}") RxHttpClient httpClient, QbdApiConfiguration configuration) {
        super();
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.uri = UriBuilder.of("/qbd-api")
                .build();
    }

    @Override
    public void checkAvailability() {
        URI currentUserUri = UriBuilder.of(uri)
                .path("manage")
                .path("health")
                .build();
        Flowable<HttpResponse<String>> call =  httpClient.exchange(GET(currentUserUri)
                .header("Authorization", "Bearer " + configuration.getAccessToken())
                .header("Content-Type", "application/json"), String.class);
        try {
            call.blockingFirst();
            log.info("QBD API is available.");
        } catch (HttpClientResponseException e) {
            HttpResponse<?> errorResponse = e.getResponse();
            log.error("QBD API is NOT available. HTTP Status Code {} {}", errorResponse.getStatus().getCode(), errorResponse.getStatus().getReason());
            @SuppressWarnings("unchecked")
            Optional<String> errorBody = (Optional<String>) errorResponse.getBody();
            if (errorBody.isPresent()) {
                log.error(errorBody.get());
            }
            throw e;
        }
    }

    @Override
    public void enterTimesheets(QbdTimesheetEntries timesheetEntries) {
        URI timesheetsUri = UriBuilder.of(uri)
                .path("timesheets")
                .build();
        log.info("Doing a POST to QBD API to create timesheet entries.");
        Flowable<HttpResponse<String>> call = httpClient.exchange(
                POST(timesheetsUri, timesheetEntries)
                .bearerAuth(configuration.getAccessToken()), 
                String.class 
        );
        try {
            HttpResponse<String> response = call.blockingFirst();
            Optional<String> bodyOptional = response.getBody();
            if (bodyOptional.isPresent()) {
                log.info(bodyOptional.get());
                log.info("Created {} timesheet entries in QBD API", timesheetEntries.getEntries().size());
            }
        } catch (HttpClientResponseException e) {
            HttpResponse<?> errorResponse = e.getResponse();
            log.error("Timesheet entry failed. HTTP Status Code {} {}", errorResponse.getStatus().getCode(), errorResponse.getStatus().getReason());
            @SuppressWarnings("unchecked")
            Optional<String> errorBody = (Optional<String>) errorResponse.getBody();
            if (errorBody.isPresent()) {
                log.error(errorBody.get());
            }
            throw e;
        }
    }
}
