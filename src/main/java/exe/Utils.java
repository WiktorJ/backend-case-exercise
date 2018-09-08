package exe;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Utils {

    private final static DateTimeFormatter dtf  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'");


    public static LogEntry createDaoFromLog(String logEntry) {
        String[] s = logEntry.split(" ");
        // TODO: Handle zones?
        LocalDateTime startTime  = LocalDateTime.parse(s[0],dtf);
        LocalDateTime endTime  = LocalDateTime.parse(s[1],dtf);
        return LogEntry.builder()
                .start(s[0])
                .end(s[1])
                .endEpoch(endTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                .startEpoch(startTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                .traceId(s[2])
                .service(s[3])
                .callerSpan(s[4].split("->")[0])
                .span(s[4].split("->")[1])
                .calls(new ArrayList<>())
                .build();
    }
}
