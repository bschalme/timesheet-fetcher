package timesheet.fetcher;

import java.time.Duration;
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
    private QbdApiPort qbdApiService;

    @Inject
    private QbdTimesheetTransformer transformer;

    @Inject
    private ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(TimesheetFetcherCommand.class, args);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        // business logic here
        if (verbose) {
            log.info("Begin fetching timesheets.");
        }
        tsheetsPort.checkAvailability();
        qbdApiService.checkAvailability();
//        String jsonString = tSheetsService.retrieveTimesheets(186512, "2020-06-01", "2020-06-01");
        String jsonString = tsheetsPort.retrieveTimesheets(null, "2021-04-19", "2021-04-19");
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
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
