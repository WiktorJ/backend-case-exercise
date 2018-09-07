package exe.input;

import exe.Dispatcher;
import exe.LogEntry;
import exe.TraceRoot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class FileInputReader implements InputReader {

    private final NavigableMap<String, Long> orphanMap;
    private final NavigableMap<Long, List<String>> nullMap;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, List<LogEntry>>> map;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final BlockingQueue<TraceRoot> outputQueue;
    private final String path;

    public FileInputReader(
            NavigableMap<String, Long> orphanMap,
            NavigableMap<Long, List<String>> nullMap,
            ConcurrentHashMap<String, ConcurrentHashMap<String, List<LogEntry>>> map,
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor,
            BlockingQueue<TraceRoot> outputQueue,
            String pathToFile) {
        this.orphanMap = orphanMap;
        this.nullMap = nullMap;
        this.map = map;
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        this.outputQueue = outputQueue;
        this.path = pathToFile;
    }


    @Override
    public void run() {
        //  TODO: size of buffer configurable
        //  TODO: configure charset
        //  TODO: If reading is blocking try with fileChannel
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), StandardCharsets.UTF_8))) {
            // This only works properly for "static" files and stdin. In case of long running service (i.e. reading logs as they are appended to file)
            // More sophisticated exe.input logic would have to be implemented (i.e. reaching end of file shouldn't break the loop).
            String line;
            while ((line = reader.readLine()) != null) {
                scheduledThreadPoolExecutor.execute(
                        new Dispatcher(orphanMap, nullMap, map, scheduledThreadPoolExecutor, outputQueue, line)
                );
            }

        } catch (IOException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
