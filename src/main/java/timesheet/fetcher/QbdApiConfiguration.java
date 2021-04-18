package timesheet.fetcher;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import lombok.Data;

@ConfigurationProperties(QbdApiConfiguration.PREFIX)
@Requires(property = QbdApiConfiguration.PREFIX)
@Data
public class QbdApiConfiguration {
    public static final String PREFIX = "qbd-api";

    private String accessToken;
}
