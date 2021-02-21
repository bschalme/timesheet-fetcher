package timesheet.fetcher;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "timesheet-fetcher", description = "...",
        mixinStandardHelpOptions = true)
@Slf4j
public class TimesheetFetcherCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(TimesheetFetcherCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            log.info("Someone told me to fetch some timesheets. Here you go.");
        }
    }
}
