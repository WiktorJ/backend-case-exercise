package exercise;

import lombok.Data;

@Data
public class TraceRoot {
    private final String id;
    private final LogEntry root;
}
