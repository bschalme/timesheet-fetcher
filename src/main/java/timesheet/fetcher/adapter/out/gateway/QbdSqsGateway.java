package timesheet.fetcher.adapter.out.gateway;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import timesheet.fetcher.application.port.out.QbdApiPort;
import timesheet.fetcher.domain.QbdTimesheetEntries;

import javax.inject.Singleton;

import static io.micronaut.context.env.Environment.AMAZON_EC2;
import static io.micronaut.core.util.StringUtils.isEmpty;

@Singleton
@Requires(env = AMAZON_EC2)
@RequiredArgsConstructor
@Slf4j
public class QbdSqsGateway implements QbdApiPort {

    private final TimesheetProducer producer;
    private final AmazonSQS sqsClient;

    @Value("${airspeed.queues.timesheet.name}")
    private String timesheetQueueName;

    @Override
    public void checkAvailability() {
        GetQueueAttributesRequest request = new GetQueueAttributesRequest();
        GetQueueUrlResult queueUrlResult = sqsClient.getQueueUrl(timesheetQueueName);
        String queueUrl = queueUrlResult.getQueueUrl();
        if (isEmpty(queueUrl)) {
            log.error("Problem looking for '{}'", timesheetQueueName);
            return;
        }
        log.info("QBD Timesheet queue is available.");
    }

    @Override
    public void enterTimesheets(QbdTimesheetEntries timesheetEntries) {
        producer.send(timesheetEntries);
    }
}
