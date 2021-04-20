package timesheet.fetcher.domain;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class QbdTimesheetEntries {
    private final List<QbdTimesheetEntry> entries;
}
