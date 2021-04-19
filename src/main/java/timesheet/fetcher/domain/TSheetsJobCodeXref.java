package timesheet.fetcher.domain;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

@Data
@Introspected
@Builder
public class TSheetsJobCodeXref {
    private Integer tsheetsJobCodeId;
    private String qbdJobId;
    private String qbdServiceItemId;
    private String qbdJobFullName;
    private String qbdServiceItemFullName;
}
