package exe.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class FileInputReader implements InputReader {

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
        //  TODO: size of buffer configurable
        //  TODO: configure charset
        //  TODO: If reading is blocking try with fileChannel
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), StandardCharsets.UTF_8), 65536)) {
            // This only works properly for "static" files and stdin. In case of long running service (i.e. reading logs as they are appended to file)
            // More sophisticated input logic would have to be implemented (i.e. reaching end of file shouldn't break the loop).
            String line;
            while ((line = reader.readLine()) != null) {
                inputQueue.put(line);
            }
        } catch (IOException | InterruptedException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
