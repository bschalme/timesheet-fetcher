package timesheet.fetcher.application.port.out;

import timesheet.fetcher.domain.QbdTimesheetEntries;

public interface QbdApiPort {

    void checkAvailability();

    void enterTimesheets(QbdTimesheetEntries timesheetEntries);
}