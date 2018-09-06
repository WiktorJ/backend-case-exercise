import input.FileInputReader;
import input.InputReader;
import input.StardartInputReader;
import output.FileOutputWriter;
import output.OutputWriter;
import output.StardartOutputWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;

public class Main {



    public static void main(String[] args) throws InterruptedException, IOException {
//        todo: queue size parametrized
        BlockingQueue<String> lines = new ArrayBlockingQueue<>(10);
//        InputReader inputReader = new StardartInputReader(lines);
        InputReader inputReader = new FileInputReader(lines, "/home/wiktor/Downloads/small-log.txt");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        OutputWriter outputWriter = new StardartOutputWriter(lines);
        OutputWriter outputWriter = new FileOutputWriter(lines, "/home/wiktor/Documents/test.txt");
        executorService.execute(outputWriter);
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        outputWriter.setStopFlag();
        executorService.shutdown();
        executorService.shutdownNow();

    }
}
