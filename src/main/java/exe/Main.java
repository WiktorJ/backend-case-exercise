package exe;

import exe.input.FileInputReader;
import exe.input.InputReader;
import exe.input.StardartInputReader;
import exe.output.FileOutputWriter;
import exe.output.OutputWriter;
import exe.output.StardartOutputWriter;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {



    public static void main(String[] args) throws InterruptedException, IOException {
//        todo: queue size parametrized
        long startTime = System.nanoTime();
        NavigableMap<Long, List<String>> orphanMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));
        ConcurrentHashMap<String, TraceStateHolder> map = new ConcurrentHashMap<>();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, new ThreadPoolExecutor.CallerRunsPolicy());
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor1 = new ScheduledThreadPoolExecutor(3, new ThreadPoolExecutor.CallerRunsPolicy());
        BlockingQueue<TraceRoot> outputQueue = new ArrayBlockingQueue<>(1000);
        BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(50);
        Dispatcher dispatcher = new Dispatcher(orphanMap, map, scheduledThreadPoolExecutor, outputQueue, inputQueue);
        scheduledThreadPoolExecutor1.execute(dispatcher);
        scheduledThreadPoolExecutor1.execute(dispatcher);
        scheduledThreadPoolExecutor1.execute(dispatcher);
//
        InputReader inputReader = new StardartInputReader(inputQueue);
//        InputReader inputReader = new FileInputReader("/home/wiktor/Downloads/large-log.txt", inputQueue);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        OutputWriter outputWriter = new StardartOutputWriter(outputQueue);
//        OutputWriter outputWriter = new FileOutputWriter(outputQueue, "/home/wiktor/Documents/test.txt");
        executorService.execute(outputWriter);
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        dispatcher.setStopFlag();
        scheduledThreadPoolExecutor1.shutdownNow();
        scheduledThreadPoolExecutor1.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        scheduledThreadPoolExecutor.shutdown();
        scheduledThreadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        outputWriter.setStopFlag();
        executorService.shutdown();
        executorService.shutdownNow();
        long endTime = System.nanoTime();

        System.out.println("Duration: " + (endTime - startTime)/1000000 + "ms");

    }
}
