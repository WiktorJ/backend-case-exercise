package exercise.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder class used during log dispatching.
 */
@Data
@AllArgsConstructor
public class TraceStateHolder {
    /**
     * Mapping "spanId" -> "calls from service associated with this spanId"
     */
    private final ConcurrentHashMap<String, List<LogEntry>> entries;
    private volatile boolean nullArrived;
    private volatile long latestEndTimestamp;
}
