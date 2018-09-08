package exercise.domain;

import exercise.domain.LogEntry;
import lombok.Data;

@Data
public class TraceRoot {
    private final String id;
    private final LogEntry root;
}
