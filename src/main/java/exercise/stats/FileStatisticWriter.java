package exercise.stats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileStatisticWriter implements StatisticWriter {
    private String path;

    public FileStatisticWriter(String path) {
        this.path = path;
    }

    @Override
    public void writeStatistics(List<String> statistics) {
        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.path, true), StandardCharsets.UTF_8)) {
            for (String statistic : statistics) {
                writer.write(statistic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
