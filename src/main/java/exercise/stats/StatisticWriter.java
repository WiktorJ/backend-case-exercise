package exercise.stats;

import java.util.List;

public interface StatisticWriter {
    default void writeStatistics(List<String> statistics) {
        statistics.forEach(System.err::println);
    }
}
