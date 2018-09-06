package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

public class StardartInputReader extends InputReader {

    public StardartInputReader(BlockingQueue<String> logQueue) {
        super(logQueue);
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

            this.readFile(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
