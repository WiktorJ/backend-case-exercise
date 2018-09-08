package exercise.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import exercise.domain.TraceRoot;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Simple stdout writer.
 */
public class StandardOutputWriter extends OutputWriter {

    private static Logger logger = LoggerFactory.getLogger(StandardOutputWriter.class);

    private final BlockingQueue<TraceRoot> logQueue;
    private final ObjectMapper objectMapper;

    public StandardOutputWriter(BlockingQueue<TraceRoot> logQueue) {
        this.logQueue = logQueue;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }


    @Override
    public void run() {
        while (continueWriting()) {
            try {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logQueue.take()));
                StatisticsHolder.getInstance().reportLineWritten();
                if (stopGracefully()) {
                    ArrayList<TraceRoot> logs = new ArrayList<>(logQueue.size());
                    logQueue.drainTo(logs);
                    for (TraceRoot log : logs) {
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(log));
                        StatisticsHolder.getInstance().reportLineWritten();
                    }
                }
            } catch (InterruptedException e) {
                logger.info("Interruption while writing to file, continueWriting = {}", continueWriting(), e);
            } catch (JsonProcessingException e) {
                logger.warn("Error while serializing entry", e);
            }

        }
    }
}
