package timesheet.fetcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import timesheet.fetcher.application.port.out.ConfigPort;
import timesheet.fetcher.application.port.out.QbdApiPort;
import timesheet.fetcher.application.port.out.TSheetsPort;
import timesheet.fetcher.application.service.QbdTimesheetTransformer;
import timesheet.fetcher.domain.QbdTimesheetEntries;
import timesheet.fetcher.domain.QbdTimesheetEntry;

import javax.inject.Inject;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimesheetFetcherCommandTest {

    @InjectMocks
    private TimesheetFetcherCommand fetcherCommand;

    @Mock
    private TSheetsPort mockTsheetsPort;

    @Mock
    private QbdApiPort mockQbdApiPort;

    @Mock
    private QbdTimesheetTransformer mockTransformer;

    @Spy
    private ObjectMapper mockObjectMapper = new ObjectMapper();

    @Mock
    private ConfigPort mockConfigPort;

    @Captor
    private ArgumentCaptor<String> fromDateCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> newDateArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> toDateCaptor;

    @Test
    @Disabled
    void testWithCommandLineOption() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "-v" };
            PicocliRunner.run(TimesheetFetcherCommand.class, ctx, args);

            // timesheet-fetcher
            // assertThat(baos.toString(), matchesPattern("^.*Begin fetching timesheets."));
        }
    }

    @Test
    void dryRun() throws Exception {
        // Given:
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);
        when(mockConfigPort.getTimesheetLastFetchedDate()).thenReturn(fourDaysAgo.format(ISO_DATE));
        when(mockTsheetsPort.retrieveTimesheets(isNull(), isA(String.class), isA(String.class)))
                .thenReturn(readFileToString(new File("src/test/resources/TSheetsTimesheets.json"), UTF_8));
        when(mockTransformer.transform(isA(String.class))).thenReturn(new QbdTimesheetEntries(List.of(QbdTimesheetEntry.builder()
                .build())));

        // When:
        fetcherCommand.dryRun = true;
        fetcherCommand.run();

        // Then:
        verify(mockQbdApiPort).checkAvailability();
        verify(mockTsheetsPort).retrieveTimesheets(isNull(), fromDateCaptor.capture(), toDateCaptor.capture());
        assertThat(fromDateCaptor.getValue(), is(threeDaysAgo.format(ISO_DATE)));
        assertThat(toDateCaptor.getValue(), is(yesterday.format(ISO_DATE)));
        verify(mockQbdApiPort, never()).enterTimesheets(isA(QbdTimesheetEntries.class));
        verify(mockConfigPort, never()).updateTimesheetLastFetchedDate(newDateArgumentCaptor.capture());
    }

    @Test
    void lastFetchedFourDaysAgo() throws Exception {
        // Given:
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);
        when(mockConfigPort.getTimesheetLastFetchedDate()).thenReturn(fourDaysAgo.format(ISO_DATE));
        when(mockTsheetsPort.retrieveTimesheets(isNull(), isA(String.class), isA(String.class)))
                .thenReturn(readFileToString(new File("src/test/resources/TSheetsTimesheets.json"), UTF_8));
        when(mockTransformer.transform(isA(String.class))).thenReturn(new QbdTimesheetEntries(List.of(QbdTimesheetEntry.builder()
                .build())));

        // When:
        fetcherCommand.run();

        // Then:
        verify(mockQbdApiPort).checkAvailability();
        verify(mockTsheetsPort).retrieveTimesheets(isNull(), fromDateCaptor.capture(), toDateCaptor.capture());
        assertThat(fromDateCaptor.getValue(), is(threeDaysAgo.format(ISO_DATE)));
        assertThat(toDateCaptor.getValue(), is(yesterday.format(ISO_DATE)));
        verify(mockQbdApiPort).enterTimesheets(isA(QbdTimesheetEntries.class));
        verify(mockConfigPort).updateTimesheetLastFetchedDate(newDateArgumentCaptor.capture());
        assertThat(newDateArgumentCaptor.getValue(), is(yesterday));
    }

    @Test
    void lastFetchedYesterday() throws Exception {
        // Given:
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        when(mockConfigPort.getTimesheetLastFetchedDate()).thenReturn(yesterday.format(ISO_DATE));

        // When:
        fetcherCommand.run();

        // Then:
        verify(mockQbdApiPort).checkAvailability();
        verify(mockTsheetsPort, never()).retrieveTimesheets(isNull(), fromDateCaptor.capture(), toDateCaptor.capture());
        verify(mockQbdApiPort, never()).enterTimesheets(isA(QbdTimesheetEntries.class));
        verify(mockConfigPort, never()).updateTimesheetLastFetchedDate(newDateArgumentCaptor.capture());
    }

    @Test
    void noTimesheetsRetrieved() throws Exception {
        // Given:
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate fourDaysAgo = today.minusDays(4);
        when(mockConfigPort.getTimesheetLastFetchedDate()).thenReturn(fourDaysAgo.format(ISO_DATE));
        when(mockTsheetsPort.retrieveTimesheets(isNull(), isA(String.class), isA(String.class)))
                .thenReturn(readFileToString(new File("src/test/resources/EmptyTSheets.json"), UTF_8));

        // When:
        fetcherCommand.run();

        // Then:
        verify(mockQbdApiPort).checkAvailability();
        verify(mockTsheetsPort).retrieveTimesheets(isNull(), fromDateCaptor.capture(), toDateCaptor.capture());
        assertThat(fromDateCaptor.getValue(), is(threeDaysAgo.format(ISO_DATE)));
        assertThat(toDateCaptor.getValue(), is(yesterday.format(ISO_DATE)));
        verify(mockQbdApiPort, never()).enterTimesheets(isA(QbdTimesheetEntries.class));
        verify(mockConfigPort).updateTimesheetLastFetchedDate(newDateArgumentCaptor.capture());
        assertThat(newDateArgumentCaptor.getValue(), is(yesterday));
    }
}
