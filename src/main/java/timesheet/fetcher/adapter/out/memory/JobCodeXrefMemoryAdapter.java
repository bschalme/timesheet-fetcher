package timesheet.fetcher.adapter.out.memory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import timesheet.fetcher.application.port.out.JobCodeXrefPort;
import timesheet.fetcher.domain.TSheetsJobCodeXref;

@Singleton
public class JobCodeXrefMemoryAdapter implements JobCodeXrefPort {

    private Map<Integer, TSheetsJobCodeXref> xrefMap;

    public JobCodeXrefMemoryAdapter() {
        xrefMap = new HashMap<>();
        xrefMap.put(58920512, TSheetsJobCodeXref.builder()
                .tsheetsJobCodeId(58920512)
                .qbdServiceItemId("80000050-1618687625")
                .qbdServiceItemFullName("Premium rate")
                .qbdJobId("80000162-1618687386")
                .qbdJobFullName("The Board:Job One")
                .build());
        xrefMap.put(58920510, TSheetsJobCodeXref.builder()
                .tsheetsJobCodeId(58920510)
                .qbdServiceItemId(null)
                .qbdJobId("80000162-1618687386")
                .qbdJobFullName("The Board:Job One")
                .build());
    }

    @Override
    public TSheetsJobCodeXref getXref(Integer tsheetsJobCodeId) {
        return xrefMap.get(tsheetsJobCodeId);
    }

}
