package timesheet.fetcher.client;

import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import timesheet.fetcher.TsheetsConfiguration;

@Client(TsheetsConfiguration.TSHEETS_API_URL)
@Header(name="Authorization", value="Bearer ${tsheet.api-token}")
public interface TsheetsClient {

}
