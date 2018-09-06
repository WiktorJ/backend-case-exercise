package output;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class StardartOutputWriter extends OutputWriter {

    private BlockingQueue<String> logQueue;

    public StardartOutputWriter(BlockingQueue<String> logQueue) {
        this.logQueue = logQueue;
    }



    @Override
    public void run() {
        while (continueWriting()) {
            try {
                System.out.println(logQueue.take());
            } catch (InterruptedException e) {
                //TODO: Handle
                e.printStackTrace();
            }

            if (stopGracefully()) {
                ArrayList<String> logs = new ArrayList<>(logQueue.size());
                logQueue.drainTo(logs);
                for (String log : logs) {
                    System.out.println(log);
                }
            }
        }
    }
}
