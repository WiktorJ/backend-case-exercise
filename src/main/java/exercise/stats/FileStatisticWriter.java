package exercise.stats;

import exercise.exceptions.IORuntimeException;
import exercise.output.FileOutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileStatisticWriter implements StatisticWriter {
    private String path;
    private static Logger logger = LoggerFactory.getLogger(FileStatisticWriter.class);

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
            logger.warn("Error while writing statistics to file, program will continue", e);
        }
    }
}
