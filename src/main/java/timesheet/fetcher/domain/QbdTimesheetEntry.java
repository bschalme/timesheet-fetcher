package timesheet.fetcher.domain;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QbdTimesheetEntry {
    private String associateId;
    private String jobId;
    private String serviceItemId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateWorked;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime startDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime endDateTime;
    private Integer durationInMinutes;
    private String notes;
    private String billableStatus;
    
}
