package timesheet.fetcher.adapter.out.gateway;

import static io.micronaut.http.HttpRequest.GET;

import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.reactivex.Flowable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import timesheet.fetcher.TsheetsConfiguration;
import timesheet.fetcher.application.port.out.TSheetsPort;

@Singleton
@Slf4j
public class TSheetsGateway implements TSheetsPort {

    private final RxHttpClient httpClient;
    private final URI uri;
    private final TsheetsConfiguration configuration;

    @Inject
    private ObjectMapper objectMapper;

    public TSheetsGateway(@Client(TsheetsConfiguration.TSHEETS_API_URL) RxHttpClient httpClient, TsheetsConfiguration configuration) {
        super();
        this.httpClient = httpClient;
        this.uri = UriBuilder.of("/api")
            .path(configuration.getApiVersion())
            .build();
        this.configuration = configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void checkAvailability() {
        URI currentUserUri = UriBuilder.of(uri)
                .path("current_user")
                .build();
        Flowable<HttpResponse<String>> call =  httpClient.exchange(GET(currentUserUri)
                .header("Authorization", "Bearer " + configuration.getApiToken())
                .header("Content-Type", "application/json"), String.class);
        try {
            call.blockingFirst();
            log.info("TSheets is available.");
        } catch (HttpClientResponseException e) {
            HttpResponse<?> errorResponse = e.getResponse();
            log.error("TSheets is NOT available. HTTP Status Code {} - {}", errorResponse.getStatus().getCode(), errorResponse.getStatus().getReason());
            Optional<String> errorBody = (Optional<String>) errorResponse.getBody();
            if (errorBody.isPresent()) {
                try {
                    Map<String, Object> errorBodyMap = objectMapper.readValue(errorBody.get(), Map.class);
                    Map<String, Object> error = (Map<String, Object>) errorBodyMap.get("error");
                    log.error("Error from TSheets. code: '{}', message: '{}'", error.get("code"), error.get("message"));
                } catch (JsonProcessingException e1) {
                    throw new RuntimeException(e1.getMessage(), e1);
                }
            }
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String retrieveTimesheets(Integer id, String startDate, String endDate) {
        log.debug("id = {}, startDate = {}, endDate = {}", id, startDate, endDate);
        URI timesheetUri = UriBuilder.of(uri)
            .path("timesheets")
            .queryParam("ids", id)
            .queryParam("start_date", startDate)
            .queryParam("end_date", endDate)
            .build();
        log.info("Fetching timesheets from '{}' to '{}'.", startDate, endDate);
        Flowable<HttpResponse<String>> call =  httpClient.exchange(GET(timesheetUri)
                .header("Authorization", "Bearer " + configuration.getApiToken())
                .header("Content-Type", "application/json"), String.class);
        HttpResponse<String> response;
        try {
            response = call.blockingFirst();
        } catch (HttpClientResponseException e) {
            HttpResponse<?> errorResponse = e.getResponse();
            log.error("TSheets returned HTTP Status Code {} - {}", errorResponse.getStatus().getCode(), errorResponse.getStatus().getReason());
            Optional<String> errorBody = (Optional<String>) errorResponse.getBody();
            if (errorBody.isPresent()) {
                try {
                    Map<String, Object> errorBodyMap = objectMapper.readValue(errorBody.get(), Map.class);
                    Map<String, Object> error = (Map<String, Object>) errorBodyMap.get("error");
                    log.error("Error from TSheets. code: '{}', message: '{}'", error.get("code"), error.get("message"));
                } catch (JsonProcessingException e1) {
                    throw new RuntimeException(e1.getMessage(), e1);
                }
            }
            throw e;
        }
        Optional<String> body = response.getBody(String.class);
        if (body.isPresent()) {
            String payload = body.get();
            logRetrievedTimeshets(payload, startDate, endDate);
            return payload;
        }
        return "";
    }

    @SneakyThrows(JsonProcessingException.class)
    private void logRetrievedTimeshets(String payload, String startDate, String endDate) {
        int durationInSeconds = 0;
        JsonNode rootNode = objectMapper.readTree(payload);
        JsonNode timesheets = rootNode.at("/results/timesheets");
        Iterator<String> timesheetIds = timesheets.fieldNames();
        while (timesheetIds.hasNext()) {
            String timesheetId = timesheetIds.next();
            JsonNode timesheet = timesheets.get(timesheetId);
            durationInSeconds += timesheet.get("duration").asInt();
        }
        log.info("Retrieved {} timesheet entries between {} and {} for a Duration of {} from TSheets.", timesheets.size(),
                startDate, endDate, Duration.ofSeconds(durationInSeconds));
    }
}
