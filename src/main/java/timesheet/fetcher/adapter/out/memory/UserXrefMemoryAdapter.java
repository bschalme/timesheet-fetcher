package timesheet.fetcher.adapter.out.memory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import timesheet.fetcher.application.port.out.UserXrefPort;
import timesheet.fetcher.domain.TSheetsUserXref;

@Singleton
public class UserXrefMemoryAdapter implements UserXrefPort {

    private Map<Integer, TSheetsUserXref> xrefMap;

    public UserXrefMemoryAdapter() {
        xrefMap = new HashMap<>();
        xrefMap.put(186512, TSheetsUserXref.builder()
                .tsheetsUserId(186512)
                .qbdAssociateId("20000-929923144")
                .userFullName("Yours Truly")
                .build());
    }

    @Override
    public TSheetsUserXref getTSheetsXref(Integer tsheetsUserId) {
        return xrefMap.get(tsheetsUserId);
    }

}
