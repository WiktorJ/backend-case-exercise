package exe;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LogEntry implements Serializable, Comparable<LogEntry> {
    private final String start;
    private final String end;
    private final String service;
    private final String span;
    private final List<LogEntry> calls;
    private transient final Long endEpoch;
    private transient final String traceId;
    private transient final String callerSpan;

    public void addCalls(List<LogEntry> entry) {
        calls.addAll(entry);
    }

    @Override
    public int compareTo(LogEntry logEntry) {
        return Long.compare(this.endEpoch, logEntry.endEpoch);
    }
}
