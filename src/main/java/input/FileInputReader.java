package input;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class FileInputReader implements InputReader {

    private BlockingQueue<String> logQueue;
    private final String path;

    public FileInputReader(BlockingQueue<String> logQueue, String pathToFile) {
        this.logQueue = logQueue;
        this.path = pathToFile;
    }


    @Override
    public void run() {
        //  TODO: size of buffer configurable
        //  TODO: configure charset
        //  TODO: If reading is blocking try with fileChannel
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), StandardCharsets.UTF_8))) {
            // This only works properly for "static" files and stdin. In case of long running service (i.e. reading logs as they are appended to file)
            // More sophisticated input logic would have to be implemented (i.e. reaching end of file shouldn't break the loop).
            String line;
            while ((line = reader.readLine()) != null) {
                logQueue.put(line);
            }
        } catch (IOException | InterruptedException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
