package exercise.domain;

import lombok.Data;

/**
 * Class representing root of trace.
 */
@Data
public class TraceRoot {
    private final String id;
    private final LogEntry root;
}
