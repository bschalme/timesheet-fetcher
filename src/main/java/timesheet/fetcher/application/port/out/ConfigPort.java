package timesheet.fetcher.application.port.out;

import java.time.LocalDate;

public interface ConfigPort {
    public String getTimesheetLastFetchedDate();

    public void updateTimesheetLastFetchedDate(LocalDate date);
}
