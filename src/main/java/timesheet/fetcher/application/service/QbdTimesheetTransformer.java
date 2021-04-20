package timesheet.fetcher.application.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import timesheet.fetcher.application.port.out.JobCodeXrefPort;
import timesheet.fetcher.application.port.out.UserXrefPort;
import timesheet.fetcher.domain.QbdTimesheetEntries;
import timesheet.fetcher.domain.QbdTimesheetEntry;
import timesheet.fetcher.domain.TSheetsJobCodeXref;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class QbdTimesheetTransformer {
    private final JobCodeXrefPort jobCodeXrefPort;
    private final UserXrefPort userXrefPort;
    private final ObjectMapper objectMapper;

    private static final String BILLABLE = "Billable";

    public QbdTimesheetEntries transform(String tsheetsTimesheets) {
        List<QbdTimesheetEntry> entries = new ArrayList<>();
        try {
            JsonNode rootNode = objectMapper.readTree(tsheetsTimesheets);
            JsonNode timesheets = rootNode.at("/results/timesheets");
            Iterator<String> timesheetIds = timesheets.fieldNames();
            while (timesheetIds.hasNext()) {
                String timesheetId = timesheetIds.next();
                JsonNode timesheet = timesheets.get(timesheetId);
                TSheetsJobCodeXref tsheetsJobCodeXref = jobCodeXrefPort.getXref(timesheet.get("jobcode_id").asInt());
                ZonedDateTime startDateTime = ZonedDateTime.parse(timesheet.get("start").asText());
                ZonedDateTime endDateTime = ZonedDateTime.parse(timesheet.get("end").asText());
                entries.add(QbdTimesheetEntry.builder()
                        .associateId(userXrefPort.getTSheetsXref(timesheet.get("user_id").asInt()).getQbdAssociateId())
                        .jobId(tsheetsJobCodeXref.getQbdJobId())
                        .serviceItemId(tsheetsJobCodeXref.getQbdServiceItemId())
                        .dateWorked(LocalDate.parse(timesheet.get("date").asText()))
                        .startDateTime(startDateTime)
                        .endDateTime(endDateTime)
                        .durationInMinutes((int) Duration.between(startDateTime, endDateTime).toMinutes())
                        .notes(timesheet.get("notes").asText())
                        .billableStatus(BILLABLE)
                        .build());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new QbdTimesheetEntries(entries);
    }
}
