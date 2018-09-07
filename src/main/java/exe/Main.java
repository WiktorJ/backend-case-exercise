package exe;

import exe.input.FileInputReader;
import exe.input.InputReader;
import exe.output.FileOutputWriter;
import exe.output.OutputWriter;
import exe.output.StardartOutputWriter;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.*;

public class Main {



    public static void main(String[] args) throws InterruptedException, IOException {
//        todo: queue size parametrized
        NavigableMap<String, Long> orphanMap = new ConcurrentSkipListMap<>();
        NavigableMap<Long, List<String>> nullMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));
        ConcurrentHashMap<String, ConcurrentHashMap<String, List<LogEntry>>> map = new ConcurrentHashMap<>();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.CallerRunsPolicy());
        BlockingQueue<TraceRoot> outputQueue = new ArrayBlockingQueue<>(50000);


//        InputReader inputReader = new StardartInputReader(lines);
        InputReader inputReader = new FileInputReader(
                orphanMap, nullMap, map, scheduledThreadPoolExecutor, outputQueue, "/home/wiktor/Downloads/large-log.txt");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        OutputWriter outputWriter = new StardartOutputWriter(outputQueue);
        OutputWriter outputWriter = new FileOutputWriter(outputQueue, "/home/wiktor/Documents/test.txt");
        executorService.execute(outputWriter);
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        scheduledThreadPoolExecutor.shutdown();
        scheduledThreadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        outputWriter.setStopFlag();
        executorService.shutdown();
        executorService.shutdownNow();

    }
}
