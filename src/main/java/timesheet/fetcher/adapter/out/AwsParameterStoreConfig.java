package timesheet.fetcher.adapter.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import timesheet.fetcher.application.port.out.ConfigPort;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class AwsParameterStoreConfig implements ConfigPort {

    private static final String PARAMETER_NAME = "/config/timesheet-fetcher_prod/timesheet-last-fetched-date";
    private final SsmClient ssmClient;

    @Override
    public String getTimesheetLastFetchedDate() {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(PARAMETER_NAME)
                .build();
        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        String timesheetLastFetchedDate = parameterResponse.parameter().value();
        log.debug("timesheet-last-fetched-date is '{}'.", timesheetLastFetchedDate);
        return timesheetLastFetchedDate;
    }

    @Override
    public void updateTimesheetLastFetchedDate(LocalDate date) {
        String dateStr = date.format(ISO_DATE);
        PutParameterRequest parameterRequest = PutParameterRequest.builder()
                .name(PARAMETER_NAME)
                .value(dateStr)
                .overwrite(true)
                .build();
        ssmClient.putParameter(parameterRequest);
        log.debug("timesheet-last-fetched-date set to '{}'.", dateStr);
    }
}
