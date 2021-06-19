package timesheet.fetcher;

import static java.time.format.DateTimeFormatter.ISO_DATE;

import java.time.Duration;
import java.time.LocalDate;
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

    @Option(names = {"-f", "--from-date"}, description = "From this date", required = false)
    LocalDate fromDate;

    @Option(names = {"-t", "--to-date"}, description = "Up to and including this date", required = false)
    LocalDate toDate;

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
        fromDate = timesheetLastFetchedDate.plusDays(1);
        toDate = LocalDate.now().minusDays(1);
//        String jsonString = tSheetsService.retrieveTimesheets(186512, "2020-06-01", "2020-06-01");
        String jsonString = tsheetsPort.retrieveTimesheets(null, fromDate.format(ISO_DATE), toDate.format(ISO_DATE));
        try {
            ObjectReader objectReader = objectMapper.readerForMapOf(Object.class);
            Map<String, Object> jsonMap = objectReader.readValue(jsonString);
            Map<String, Object> results = (Map<String, Object>) jsonMap.get("results");
            Map<String, Object> timesheets = (Map<String, Object>) results.get("timesheets");
            Set<Entry<String, Object>> entrySet = timesheets.entrySet();
            int durationInSeconds = 0;
            for (Entry<String, Object> timesheetEntry: entrySet) {
                Map<String, Object> timesheetData = (Map<String, Object>) timesheetEntry.getValue();
                durationInSeconds += (Integer) timesheetData.get("duration");
            }
            log.info("Retrieved {} timesheet entries for a Duration of {} from TSheets.", entrySet.size(),
                    Duration.ofSeconds(durationInSeconds));
            QbdTimesheetEntries qbdApiBody = transformer.transform(jsonString);
            if (dryRun) {
                log.info("This would be the call to QBD API:{}{}", System.getProperty("line.separator"),
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(qbdApiBody));
            } else {
                qbdApiPort.enterTimesheets(qbdApiBody);
                log.info("Created {} timesheet entries in QBD API", entrySet.size());
                configPort.updateTimesheetLastFetchedDate(toDate);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
