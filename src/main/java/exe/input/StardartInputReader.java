package exe.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

public class StardartInputReader implements exe.input.InputReader {

    private static final String EXIT_COMMAND = "exit";

    private final BlockingQueue<String> inputQueue;

    public StardartInputReader(BlockingQueue<String> inputQueue) {
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
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}