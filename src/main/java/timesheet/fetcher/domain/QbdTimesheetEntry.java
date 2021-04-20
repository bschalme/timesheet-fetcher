package timesheet.fetcher.domain;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QbdTimesheetEntry {
    private String associateId;
    private String jobId;
    private String serviceItemId;
    private LocalDate dateWorked;
    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;
    private Integer durationInMinutes;
    private String notes;
    private String billableStatus;
    
}
