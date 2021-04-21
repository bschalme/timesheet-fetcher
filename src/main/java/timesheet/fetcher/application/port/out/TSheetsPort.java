package timesheet.fetcher.application.port.out;

public interface TSheetsPort {
    public void checkAvailability();

    public String retrieveTimesheets(Integer id, String startDate, String endDate);
}