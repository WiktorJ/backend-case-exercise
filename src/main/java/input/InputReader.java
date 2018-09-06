package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public abstract class InputReader implements Runnable {

    private BlockingQueue<String> logQueue;

    InputReader(BlockingQueue<String> logQueue) {
        this.logQueue = logQueue;
    }

    protected void readFile(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                logQueue.put(line);
            } catch (InterruptedException e) {
                //TODO: Handle
                e.printStackTrace();
            }
        }
    }
}
