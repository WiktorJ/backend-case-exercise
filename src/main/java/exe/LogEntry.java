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
    private transient final Long startEpoch;
    private transient final String traceId;
    private transient final String callerSpan;
    private transient boolean malformed = false;

    public void addCalls(List<LogEntry> entry) {
        calls.addAll(entry);
    }

    @Override
    public int compareTo(LogEntry logEntry) {
        int compare = Long.compare(this.endEpoch, logEntry.endEpoch);
        if (compare != 0) {
            return compare;
        }
        compare = Long.compare(this.startEpoch, logEntry.startEpoch);
        if (compare != 0) {
            return compare;
        }
        compare = this.service.compareTo(logEntry.service);
        if (compare != 0) {
            return compare;
        }
        return this.span.compareTo(logEntry.span);
    }
}
