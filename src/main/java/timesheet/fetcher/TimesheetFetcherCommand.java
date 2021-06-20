package timesheet.fetcher;

import static java.time.format.DateTimeFormatter.ISO_DATE;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import timesheet.fetcher.application.port.out.ConfigPort;
import timesheet.fetcher.application.port.out.QbdApiPort;
import timesheet.fetcher.application.port.out.TSheetsPort;
import timesheet.fetcher.application.service.QbdTimesheetTransformer;
import timesheet.fetcher.domain.QbdTimesheetEntries;

@Command(name = "timesheet-fetcher", description = "...",
        mixinStandardHelpOptions = true)
@Slf4j
public class TimesheetFetcherCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Option(names = {"-d", "--dry-run"}, description = "Do not update QBD API")
    boolean dryRun;

    @Inject
    private TSheetsPort tsheetsPort;

    @Inject
    private QbdApiPort qbdApiPort;

    @Inject
    private QbdTimesheetTransformer transformer;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ConfigPort configPort;

    private DateTimeFormatter simpleDisplayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d");

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(TimesheetFetcherCommand.class, args);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        // business logic here
        if (verbose) {
            log.info("Begin fetching timesheets.");
        }
        if (dryRun) {
            log.info("This is a dry run.");
        }
        tsheetsPort.checkAvailability();
        qbdApiPort.checkAvailability();
        LocalDate timesheetLastFetchedDate = LocalDate.parse(configPort.getTimesheetLastFetchedDate(), ISO_DATE);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        if (timesheetLastFetchedDate.isBefore(yesterday)) {
            LocalDate fromDate = timesheetLastFetchedDate.plusDays(1);
            LocalDate toDate = yesterday;
            String jsonString = tsheetsPort.retrieveTimesheets(null, fromDate.format(ISO_DATE), toDate.format(ISO_DATE));
            Map<String, Object> jsonMap = xformToJsonMap(jsonString);
            if (hasTimesheets(jsonMap)) {
                logRetrievedTimesheets(jsonMap, fromDate, toDate);
                QbdTimesheetEntries qbdApiBody = transformer.transform(jsonString);
                if (dryRun) {
                    log.info("This would be the call to QBD API:{}{}", System.getProperty("line.separator"),
                            formatQbdTimesheetEntries(qbdApiBody));
                } else {
                    qbdApiPort.enterTimesheets(qbdApiBody);
                    configPort.updateTimesheetLastFetchedDate(toDate);
                }
            } else {
                log.info("No timesheet entries retrieved between {} and {}", fromDate.format(simpleDisplayFormatter), toDate.format(simpleDisplayFormatter));
                if (!dryRun) {
                    configPort.updateTimesheetLastFetchedDate(toDate);
                }
            }
        } else {
            log.debug("Today is {} and timesheet last fetched date is {}", today.format(ISO_DATE), timesheetLastFetchedDate.format(ISO_DATE));
            log.info("Timesheets have already been fetched. Try again tomorrow.");
        }
    }

    private Map<String, Object> xformToJsonMap(String jsonString) {
        ObjectReader objectReader = objectMapper.readerForMapOf(Object.class);
        try {
            return objectReader.readValue(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean hasTimesheets(Map<String, Object> jsonMap) {
        Map<String, Object> results = (Map<String, Object>) jsonMap.get("results");
        return results.get("timesheets") instanceof Map;
    }

    private void logRetrievedTimesheets(Map<String, Object> jsonMap, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> results = (Map<String, Object>) jsonMap.get("results");
        Map<String, Object> timesheets = (Map<String, Object>) results.get("timesheets");
        Set<Entry<String, Object>> entrySet = timesheets.entrySet();
        int durationInSeconds = 0;
        for (Entry<String, Object> timesheetEntry : entrySet) {
            Map<String, Object> timesheetData = (Map<String, Object>) timesheetEntry.getValue();
            durationInSeconds += (Integer) timesheetData.get("duration");
        }
        log.info("Retrieved {} timesheet entries between {} and {} for a Duration of {} from TSheets.", entrySet.size(),
                fromDate.format(simpleDisplayFormatter), toDate.format(simpleDisplayFormatter), Duration.ofSeconds(durationInSeconds));
    }

    private String formatQbdTimesheetEntries(QbdTimesheetEntries entries) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entries);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void processTimesheets(Map<String, Object> results) {

    }
}
