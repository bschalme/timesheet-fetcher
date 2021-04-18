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
import timesheet.fetcher.service.QbdApiService;
import timesheet.fetcher.service.TSheetsService;

@Command(name = "timesheet-fetcher", description = "...",
        mixinStandardHelpOptions = true)
@Slf4j
public class TimesheetFetcherCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Inject
    private TSheetsService tSheetsService;

    @Inject
    private QbdApiService qbdApiService;

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
        tSheetsService.checkAvailability();
        qbdApiService.checkAvailability();
//        String jsonString = tSheetsService.retrieveTimesheets(186512, "2020-06-01", "2020-06-01");
        String jsonString = tSheetsService.retrieveTimesheets(null, "2020-06-01", "2020-06-01");
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
