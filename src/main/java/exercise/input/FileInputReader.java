package exercise.input;

import exercise.ConfigHolder;
import exercise.exceptions.IORuntimeException;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * Simple reader that reads logs line by line.
 */
public class FileInputReader implements InputReader {

    private static Logger logger = LoggerFactory.getLogger(FileInputReader.class);

    private static final int BUFFER_SIZE = ConfigHolder.getConfig().getInt("fileInputBufferSize", 65536);

    private final String path;
    private BlockingQueue<String> inputQueue;

    public FileInputReader(
            BlockingQueue<String> inputQueue,
            String pathToFile) {
        this.path = pathToFile;
        this.inputQueue = inputQueue;
    }


    @Override
    public void run() {
        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(this.path), StandardCharsets.UTF_8), BUFFER_SIZE)) {
            // This only works properly for "static" files and stdin. In case of long running service (i.e. reading logs as they are appended to file)
            // More sophisticated input logic would have to be implemented (i.e. reaching end of file shouldn't break the loop).
            String line;
            while ((line = reader.readLine()) != null) {
                inputQueue.put(line);
                StatisticsHolder.getInstance().reportLineRead();
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error while reading file", e);
            throw new IORuntimeException("Error while reading file", e);
        }
    }
}
