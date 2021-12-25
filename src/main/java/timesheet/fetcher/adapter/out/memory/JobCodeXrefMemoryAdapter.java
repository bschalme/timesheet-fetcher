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
        xrefMap.put(60921270, TSheetsJobCodeXref.builder()
                .tsheetsJobCodeId(60921270)
                .qbdServiceItemId("80000051-1638646199")
                .qbdServiceItemFullName("Premium rate 2")
                .qbdJobId("80000162-1618687386")
                .qbdJobFullName("The Board:Job One")
                .build());
        xrefMap.put(49359685, TSheetsJobCodeXref.builder()
                .tsheetsJobCodeId(49359685)
                .qbdServiceItemId("ABC-123")
                .qbdServiceItemFullName("Standard rate")
                .qbdJobId("DEF-456")
                .qbdJobFullName("English Harbour:Rum Tasting")
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
