package exercise.input;

import exercise.exceptions.IORuntimeException;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

public class StandardInputReader implements exercise.input.InputReader {
    private static Logger logger = LoggerFactory.getLogger(StandardInputReader.class);


    private static final String EXIT_COMMAND = "exit";

    private final BlockingQueue<String> inputQueue;

    public StandardInputReader(BlockingQueue<String> inputQueue) {
        this.inputQueue = inputQueue;
    }


    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in) {
                    @Override
                    public void close() {
                        //  We do not want to close System.in
                    }
                })) {
            String line;
            while (!(line = reader.readLine()).equals(EXIT_COMMAND)) {
                inputQueue.put(line);
                StatisticsHolder.getInstance().reportLineRead();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error while reading from stdin", e);
            throw new IORuntimeException("Error while reading from stdin", e);
        }
    }
}
