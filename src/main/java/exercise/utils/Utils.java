package exercise.utils;

import exercise.ConfigHolder;
import exercise.domain.LogEntry;
import exercise.stats.FileStatisticWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final String DATE_FORMAT = ConfigHolder.getConfig().getString("dateFormat", "yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'");

    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);


    public static LogEntry createDaoFromLog(String logEntry) {
        String[] parts = logEntry.split(" ");
        if (parts.length != 5) {
            return returnMalformedLogEntry(String.format("Number of log elements %s, expected %s", parts.length, 5), logEntry);
        }

        try {
            LocalDateTime startTime = LocalDateTime.parse(parts[0], dtf);
            LocalDateTime endTime = LocalDateTime.parse(parts[1], dtf);
            String[] spans = parts[4].split("->");
            if (spans.length != 2) {
                return returnMalformedLogEntry(String.format("Wrong format of span ids. Expected 'callerSpan->span'. Received: '%s'", parts[4]), logEntry);
            }
            return LogEntry.builder()
                    .start(parts[0])
                    .end(parts[1])
                    .endEpoch(endTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .startEpoch(startTime.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .traceId(parts[2])
                    .service(parts[3])
                    .callerSpan(spans[0])
                    .span(spans[1])
                    .calls(new ArrayList<>())
                    .build();
        } catch (java.time.format.DateTimeParseException exception) {
            return returnMalformedLogEntry(
                    String.format(
                            "Wrong date format received\nstart: %s\nend: %s\nExpected: %s\n",
                            parts[0],
                            parts[1],
                            DATE_FORMAT
                    ), logEntry);
        }

    }

    private static LogEntry returnMalformedLogEntry(String reason, String entry) {
        logger.warn("Malformed log: {}, reason: {}", entry, reason);
        return LogEntry.builder().malformed(true).build();
    }
}
