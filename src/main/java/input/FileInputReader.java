package input;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class FileInputReader extends InputReader {

    private final String path;

    public FileInputReader(BlockingQueue<String> logQueue, String pathToFile) {
        super(logQueue);
        this.path = pathToFile;
    }


    @Override
    public void run() {
        //  TODO: size of buffer configurable
        //  TODO: configure charset
        //  TODO: If reading is blocking try with fileChannel
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), StandardCharsets.UTF_8))) {
            this.readFile(reader);
        } catch (IOException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
