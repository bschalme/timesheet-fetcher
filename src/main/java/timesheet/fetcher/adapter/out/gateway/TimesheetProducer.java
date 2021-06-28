package timesheet.fetcher.adapter.out.gateway;

import io.micronaut.context.annotation.Requires;
import io.micronaut.jms.annotations.JMSProducer;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;
import timesheet.fetcher.domain.QbdTimesheetEntries;

import static io.micronaut.context.env.Environment.AMAZON_EC2;
import static io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME;

@JMSProducer(CONNECTION_FACTORY_BEAN_NAME)
@Requires(env = AMAZON_EC2)
public interface TimesheetProducer {

    @Queue("${airspeed.queues.timesheet.name}")
    void send(@MessageBody QbdTimesheetEntries timesheetEntries);
}
