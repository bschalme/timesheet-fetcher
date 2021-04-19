package timesheet.fetcher.application.port.out;

import timesheet.fetcher.domain.TSheetsJobCodeXref;

public interface JobCodeXrefPort {
    public TSheetsJobCodeXref getXref(Integer tsheetsJobCodeId);
}
