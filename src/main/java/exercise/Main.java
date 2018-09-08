package exercise;

import exercise.input.FileInputReader;
import exercise.input.InputReader;
import exercise.input.StardartInputReader;
import exercise.output.FileOutputWriter;
import exercise.output.OutputWriter;
import exercise.output.StardartOutputWriter;
import exercise.stats.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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


    private static CommandLine getParser(String[] args) {
        Options options = new Options();
        Option.builder("i").longOpt("input").hasArg(true).desc("Path to input file").required(false);
        options.addOption(
                Option.builder("i")
                        .longOpt("input")
                        .hasArg(true)
                        .desc("Path to input file")
                        .required(false)
                        .build()
        );
        options.addOption(
                Option.builder("o")
                        .longOpt("output")
                        .hasArg(true)
                        .desc("Path to output file")
                        .required(false)
                        .build()
        );
        options.addOption(
                Option.builder("e")
                        .longOpt("errorOutput")
                        .hasArg(true)
                        .desc("Path to error output file")
                        .required(false)
                        .build()
        );

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(1000);
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(
                    "java -jar backend-case-exercise.jar -i path/to/input/file -o path/to/output/file",
                    "If no file is specified, than standard input/output is used.",
                    options,
                    "");
            System.exit(1);
        }
        return null;
    }


    public static void main(String[] args) throws InterruptedException, IOException {


        long startTime = System.nanoTime();
        StatisticsHolder.getInstance().setStartTime(startTime);
        CommandLine cmd = getParser(args);
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String errorOutputFilePath = cmd.getOptionValue("errorOutput");

        BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(ConfigHolder.getConfig().getInt("inputQueueSize", 50));
        BlockingQueue<TraceRoot> outputQueue = new ArrayBlockingQueue<>(ConfigHolder.getConfig().getInt("outputQueueSize", 1000));

        InputReader inputReader = inputFilePath == null ? new StardartInputReader(inputQueue) : new FileInputReader(inputQueue, inputFilePath);
        OutputWriter outputWriter = outputFilePath == null ? new StardartOutputWriter(outputQueue) : new FileOutputWriter(outputQueue, outputFilePath);
        if (errorOutputFilePath != null) {
            StatisticsHolder.getInstance().setStatisticWriter(new FileStatisticWriter(errorOutputFilePath));
        }

        int availableProcessors = Runtime.getRuntime().availableProcessors() - 2; //Subtract two threads doing io
        availableProcessors = availableProcessors < 2 ? 2 : availableProcessors; //We need minimum 4 threads (be design)
        int halfCores = (availableProcessors + 1) / 2; // in case somehow we have odd number

        ScheduledThreadPoolExecutor assemblerScheduler = new ScheduledThreadPoolExecutor(halfCores, new ThreadPoolExecutor.CallerRunsPolicy());
        ThreadPoolExecutor dispatcherScheduler = new ThreadPoolExecutor(halfCores, halfCores, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(halfCores));

        ConcurrentHashMap<String, TraceStateHolder> map = new ConcurrentHashMap<>();
        NavigableMap<Long, List<String>> orphanMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));
        Dispatcher dispatcher = new Dispatcher(orphanMap, map, assemblerScheduler, outputQueue, inputQueue);

        dispatcherScheduler.execute(dispatcher);
        dispatcherScheduler.execute(dispatcher);
        dispatcherScheduler.execute(dispatcher);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(outputWriter);
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        dispatcher.setStopFlag();
        dispatcherScheduler.shutdownNow();
        dispatcherScheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        assemblerScheduler.shutdown();
        assemblerScheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        outputWriter.setStopFlag();
        executorService.shutdown();
        executorService.shutdownNow();
        //TODO: handle orphans at the end
        long endTime = System.nanoTime();
        StatisticsHolder.getInstance().accept(new OutputStatistic());
        StatisticsHolder.getInstance().accept(new InputStatistic());
        StatisticsHolder.getInstance().accept(new CounterStatistic());
        StatisticsHolder.getInstance().accept(new AveragesStatistics());
        StatisticsHolder.getInstance().publishStatistics();
        //write orphans id
        System.out.println("Duration: " + (endTime - startTime) / 1000000 + "ms");

    }
}
