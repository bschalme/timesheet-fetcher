package timesheet.fetcher.domain;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

@Data
@Introspected
@Builder
public class TSheetsUserXref {
    private Integer tsheetsUserId;
    private String qbdAssociateId;
    private String userFullName;
}
