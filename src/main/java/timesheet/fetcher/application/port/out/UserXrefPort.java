package timesheet.fetcher.application.port.out;

import timesheet.fetcher.domain.TSheetsUserXref;

public interface UserXrefPort {
    public TSheetsUserXref getTSheetsXref(Integer tsheetsUserId);
}
