import input.FileInputReader;
import input.InputReader;
import input.StardartInputReader;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {



    public static void main(String[] args) throws InterruptedException {
//        todo: queue size parametrized
        BlockingQueue<String> lines = new ArrayBlockingQueue<>(10);
//        InputReader inputReader = new StardartInputReader(lines);
        InputReader inputReader = new FileInputReader(lines, "/home/wiktor/Downloads/small-log.txt");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(inputReader);
    }
}
