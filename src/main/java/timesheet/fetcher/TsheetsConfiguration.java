package timesheet.fetcher;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties(TsheetsConfiguration.PREFIX)
@Requires(property = TsheetsConfiguration.PREFIX)
public class TsheetsConfiguration {
    public static final String PREFIX = "tsheets";
    public static final String TSHEETS_API_URL = "https://rest.tsheets.com";

    private String apiVersion;
    private String apiToken;

    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiToken() {
        return this.apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
