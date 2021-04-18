package timesheet.fetcher.client;

import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;

@Header(name="User-Agent", value="Timesheet-Fetcher")
@Client("https://rest.tsheets.com")
public interface QbdApiClient {

}
