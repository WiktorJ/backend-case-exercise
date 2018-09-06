import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;

public class OutputWriter implements Runnable {

    private final BlockingQueue<String> logQueue;
    private final BufferedReader reader;

    //TODO: size of buffer configurable
    public OutputWriter(BlockingQueue<String> logQueue, String pathToFile) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(pathToFile));
        this.logQueue = logQueue;
    }

    public OutputWriter(BlockingQueue<String> logQueue) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.logQueue = logQueue;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                logQueue.put(line);
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
