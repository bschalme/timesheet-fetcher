package timesheet.fetcher.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class QbdTimesheetEntryUnitTest {
    @Test
    void datesFormattedCorrectly() throws Exception {
        // Given:
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        QbdTimesheetEntry entry = QbdTimesheetEntry.builder()
                .associateId("20000-929923144")
                .jobId("50000-929925953")
                .serviceItemId("8000004C-1519869935")
                .dateWorked(LocalDate.of(2020, 11, 10))
                .startDateTime(ZonedDateTime.of(2020, 11, 10, 12, 0, 0, 0, ZoneId.of("America/Winnipeg")))
                .endDateTime(ZonedDateTime.of(2020, 11, 10, 13, 30, 0, 0, ZoneId.of("America/Winnipeg")))
                .durationInMinutes(90)
                .notes("Hello World!")
                .billableStatus("Billable")
                .build();

        // When:
        String jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry);

        // Then:
        JsonNode rootNode = objectMapper.readTree(jsonStr);
        assertThat("dateWorked;", rootNode.get("dateWorked").asText(), is("2020-11-10"));
        assertThat("startDateTime;", rootNode.get("startDateTime").asText(), is("2020-11-10T12:00:00-06:00"));
        assertThat("endDateTime;", rootNode.get("endDateTime").asText(), is("2020-11-10T13:30:00-06:00"));
    }
}
