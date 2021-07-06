package timesheet.fetcher;

import static java.time.format.DateTimeFormatter.ISO_DATE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.SneakyThrows;
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
    public void runOld() {
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
            String jsonString = tsheetsPort.retrieveTimesheets(null, fromDate.format(ISO_DATE),
                    yesterday.format(ISO_DATE));
            Map<String, Object> jsonMap = xformToJsonMap(jsonString);
            if (hasTimesheets(jsonMap)) {
                QbdTimesheetEntries qbdApiBody = transformer.transform(jsonString);
                if (dryRun) {
                    log.info("This would be the call to QBD API:{}{}", System.getProperty("line.separator"),
                            formatQbdTimesheetEntries(qbdApiBody));
                } else {
                    qbdApiPort.enterTimesheets(qbdApiBody);
                    configPort.updateTimesheetLastFetchedDate(yesterday);
                }
            } else {
                log.info("No timesheet entries retrieved between {} and {}", fromDate.format(simpleDisplayFormatter),
                        yesterday.format(simpleDisplayFormatter));
                if (!dryRun) {
                    configPort.updateTimesheetLastFetchedDate(yesterday);
                }
            }
        } else {
            log.debug("Today is {} and timesheet last fetched date is {}", today.format(ISO_DATE),
                    timesheetLastFetchedDate.format(ISO_DATE));
            log.info("Timesheets have already been fetched. Try again tomorrow.");
        }
    }

    public void run() {
        if (verbose) {
            log.info("Begin fetching timesheets.");
        }
        if (dryRun) {
            log.info("This is a dry run.");
        }
        boolean wetRun = !dryRun;
        tsheetsPort.checkAvailability();
        qbdApiPort.checkAvailability();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        Stream.of(LocalDate.parse(configPort.getTimesheetLastFetchedDate(), ISO_DATE))
                .filter(timesheetLastFetchedDate -> {
                    if (timesheetLastFetchedDate.isBefore(yesterday)) {
                        return true;
                    } else {
                        log.debug("Today is {} and timesheet last fetched date is {}", today.format(ISO_DATE),
                                timesheetLastFetchedDate.format(ISO_DATE));
                        log.info("Timesheets have already been fetched. Try again tomorrow.");
                        return false;
                    }
                })
                .map(timesheetLastFetchedDate -> {
                    LocalDate fromDate = timesheetLastFetchedDate.plusDays(1);
                    return xformToJsonMap(tsheetsPort.retrieveTimesheets(null, fromDate.format(ISO_DATE),
                            yesterday.format(ISO_DATE)));
                })
                .map(jsonMap -> {
                    if (dryRun) {
                        return jsonMap;
                    }
                    configPort.updateTimesheetLastFetchedDate(yesterday);
                    return jsonMap;
                })
                .filter(this::hasTimesheets)
                .map(this::xformToQbdTimesheetEntries)
                .forEach(qbdTimesheetEntries -> {
                    if (dryRun) {
                        return;
                    }
                    qbdApiPort.enterTimesheets(qbdTimesheetEntries);
                });
    }

    @SneakyThrows(JsonProcessingException.class)
    private Map<String, Object> xformToJsonMap(String jsonString) {
        ObjectReader objectReader = objectMapper.readerForMapOf(Object.class);
        return objectReader.readValue(jsonString);
    }

    private boolean hasTimesheets(Map<String, Object> jsonMap) {
        Map<String, Object> results = (Map<String, Object>) jsonMap.get("results");
        return results.get("timesheets") instanceof Map;
    }

    @SneakyThrows(JsonProcessingException.class)
    private String formatQbdTimesheetEntries(QbdTimesheetEntries entries) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entries);
    }

    @SneakyThrows(JsonProcessingException.class)
    private QbdTimesheetEntries xformToQbdTimesheetEntries(Map<String, Object> jsonMap) {
        return transformer.transform(objectMapper.writeValueAsString(jsonMap));
    }
}
