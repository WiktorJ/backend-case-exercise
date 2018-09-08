package exe;

import exe.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
public class TraceStateHolder {
    private final ConcurrentHashMap<String, List<LogEntry>> entries;
    private volatile boolean nullArrived;
    private volatile long latestEndTimestamp;
}
