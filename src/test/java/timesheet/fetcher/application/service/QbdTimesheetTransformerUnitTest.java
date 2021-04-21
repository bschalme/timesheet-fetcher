package timesheet.fetcher.application.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import timesheet.fetcher.application.port.out.JobCodeXrefPort;
import timesheet.fetcher.application.port.out.UserXrefPort;
import timesheet.fetcher.domain.QbdTimesheetEntries;
import timesheet.fetcher.domain.QbdTimesheetEntry;
import timesheet.fetcher.domain.TSheetsJobCodeXref;
import timesheet.fetcher.domain.TSheetsUserXref;
import timesheet.fetcher.exception.MissingJobCodeXrefException;

@ExtendWith(MockitoExtension.class)
class QbdTimesheetTransformerUnitTest {

    @InjectMocks
    private QbdTimesheetTransformer transformer;

    @Mock
    private JobCodeXrefPort mockJobCodeXrefPort;

    @Mock
    private UserXrefPort mockUserXrefPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void transformTsheetsTimesheets() throws Exception {
        // Given:
        String tsheetsTimesheets = readFileToString(new File("src/test/resources/TSheetsTimesheets.json"), UTF_8);
        when(mockJobCodeXrefPort.getXref(eq(58920512))).thenReturn(TSheetsJobCodeXref.builder()
                .tsheetsJobCodeId(58920512)
                .qbdJobId("80000162-1618687386")
                .qbdServiceItemId("80000050-1618687625")
                .build());
        when(mockUserXrefPort.getTSheetsXref(eq(186512))).thenReturn(TSheetsUserXref.builder()
                .tsheetsUserId(186512)
                .qbdAssociateId("20000-929923144")
                .build());

        // When:
        QbdTimesheetEntries results = transformer.transform(tsheetsTimesheets);

        // Then:
        assertThat(results, notNullValue());
        List<QbdTimesheetEntry> entries = results.getEntries();
        assertThat("Number of entries;", entries, hasSize(7));
        QbdTimesheetEntry entry = entries.get(0);
        assertThat("Associate ID;", entry.getAssociateId(), is("20000-929923144"));
        assertThat("Job ID", entry.getJobId(), is("80000162-1618687386"));
        assertThat("Service Item ID;", entry.getServiceItemId(), is("80000050-1618687625"));
        assertThat("dateWorked;", entry.getDateWorked(), is(LocalDate.parse("2021-04-19")));
        assertThat("startDateTime;", entry.getStartDateTime(), is(ZonedDateTime.parse("2021-04-19T10:00:00-05:00")));
        assertThat("endDateTime;", entry.getEndDateTime(), is(ZonedDateTime.parse("2021-04-19T11:00:00-05:00")));
        assertThat("durationInMinutes;", entry.getDurationInMinutes(), is(60));
        assertThat("notes;", entry.getNotes(), is("::Onboarding and overview with Capt. Barbossa."));
        assertThat("billableStatus;", entry.getBillableStatus(), is("Billable"));
    }

    @Test
    void missingJobCodeXref() throws Exception {
        // Given:
        String tsheetsTimesheets = readFileToString(new File("src/test/resources/TSheetsTimesheets.json"), UTF_8);
        when(mockJobCodeXrefPort.getXref(eq(58920512))).thenReturn(null);

        // Then:
        Exception exception = assertThrows(
                MissingJobCodeXrefException.class, 
                () -> transformer.transform(tsheetsTimesheets));
        assertThat(exception.getMessage(), is("No JobCodeXref found for TSheets jobcode_id '58920512'"));
    }
}
