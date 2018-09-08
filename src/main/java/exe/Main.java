package exe;

import exe.input.FileInputReader;
import exe.input.InputReader;
import exe.input.StardartInputReader;
import exe.output.FileOutputWriter;
import exe.output.OutputWriter;
import exe.output.StardartOutputWriter;
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
        CommandLine cmd = getParser(args);
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");

//        todo: queue size parametrized
        BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(50);
        BlockingQueue<TraceRoot> outputQueue = new ArrayBlockingQueue<>(1000);

        InputReader inputReader = inputFilePath == null ? new StardartInputReader(inputQueue) : new FileInputReader(inputQueue, inputFilePath);
        OutputWriter outputWriter = outputFilePath == null ? new StardartOutputWriter(outputQueue) : new FileOutputWriter(outputQueue, outputFilePath);


        NavigableMap<Long, List<String>> orphanMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));
        ConcurrentHashMap<String, TraceStateHolder> map = new ConcurrentHashMap<>();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3, new ThreadPoolExecutor.CallerRunsPolicy());
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor1 = new ScheduledThreadPoolExecutor(3, new ThreadPoolExecutor.CallerRunsPolicy());
        Dispatcher dispatcher = new Dispatcher(orphanMap, map, scheduledThreadPoolExecutor, outputQueue, inputQueue);
        scheduledThreadPoolExecutor1.execute(dispatcher);
        scheduledThreadPoolExecutor1.execute(dispatcher);
        scheduledThreadPoolExecutor1.execute(dispatcher);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
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
