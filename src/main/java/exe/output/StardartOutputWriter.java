package exe.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import exe.TraceRoot;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class StardartOutputWriter extends OutputWriter {

    private final BlockingQueue<TraceRoot> logQueue;
    private final ObjectMapper objectMapper;

    public StardartOutputWriter(BlockingQueue<TraceRoot> logQueue) {
        this.logQueue = logQueue;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }



    @Override
    public void run() {
        while (continueWriting()) {
            try {
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logQueue.take()));
            } catch (InterruptedException | JsonProcessingException e) {
                //TODO: Handle
                e.printStackTrace();
            }

            if (stopGracefully()) {
                ArrayList<TraceRoot> logs = new ArrayList<>(logQueue.size());
                logQueue.drainTo(logs);
                for (TraceRoot log : logs) {
                    try {
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(log));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
