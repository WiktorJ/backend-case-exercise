package exercise.output;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import exercise.ConfigHolder;
import exercise.domain.TraceRoot;
import exercise.exceptions.IORuntimeException;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class FileOutputWriter extends OutputWriter {

    private static Logger logger = LoggerFactory.getLogger(FileOutputWriter.class);

    private static final int BUFFER_SIZE = ConfigHolder.getConfig().getInt("fileOutputBufferSize", 65536);
    private BlockingQueue<TraceRoot> logQueue;
    private final String path;
    private final ObjectMapper objectMapper;

    public FileOutputWriter(BlockingQueue<TraceRoot> logQueue, String pathToFile) {
        this.logQueue = logQueue;
        this.path = pathToFile;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }


    @Override
    public void run() {
        try (JsonGenerator writer = this.objectMapper.getFactory().createGenerator(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(this.path), StandardCharsets.UTF_8), BUFFER_SIZE))) {
            writer.setPrettyPrinter(new MinimalPrettyPrinter("\n"));
            while (continueWriting()) {
                try {

                    this.objectMapper.writeValue(writer, logQueue.take());
                    StatisticsHolder.getInstance().reportLineWritten();
                } catch (InterruptedException e) {
                    logger.info("Interruption while writing to file, continueWriting = {}", continueWriting(), e);
                }

            }
            if (stopGracefully()) {
                ArrayList<TraceRoot> logs = new ArrayList<>(logQueue.size());
                logQueue.drainTo(logs);
                for (TraceRoot log : logs) {
                    StatisticsHolder.getInstance().reportLineWritten();
                    this.objectMapper.writeValue(writer, log);
                }
            }
        } catch (IOException e) {
            logger.error("Error while writing to file", e);
            throw new IORuntimeException("Error while writing to file", e);
        }
    }
}
