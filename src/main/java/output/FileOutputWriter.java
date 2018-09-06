package output;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class FileOutputWriter extends OutputWriter {

    private BlockingQueue<String> logQueue;
    private final String path;

    public FileOutputWriter(BlockingQueue<String> logQueue, String pathToFile) {
        this.logQueue = logQueue;
        this.path = pathToFile;
    }


    @Override
    public void run() {
        //  TODO: size of buffer configurable
        //  TODO: configure charset
        //  TODO: If reading is blocking try with fileChannel, or without buffer
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.path), StandardCharsets.UTF_8))) {
            while (continueWriting()) {
                try {
                    writer.write(logQueue.take());
                    writer.newLine();
                } catch (InterruptedException e) {
                    //TODO: Handle
                    e.printStackTrace();
                }
            }
            if (stopGracefully()) {
                ArrayList<String> logs = new ArrayList<>(logQueue.size());
                logQueue.drainTo(logs);
                for (String log : logs) {
                    writer.write(log);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            //TODO: Handle
            e.printStackTrace();
        }
    }
}
