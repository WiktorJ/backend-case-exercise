package exercise.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class StatisticsHolder {

    private StatisticsHolder() {
    }

    private final ConcurrentHashMap<String, String> orphans = new ConcurrentHashMap<>();
    private AtomicLong malformedCount = new AtomicLong(0);
    private AtomicLong totalTraceDepth = new AtomicLong(0);
    private AtomicLong totalTraceSize = new AtomicLong(0);
    private AtomicLong tracesCount = new AtomicLong(0);
    private AtomicLong linesRead = new AtomicLong(0);
    private AtomicLong linesWritten = new AtomicLong(0);
    private long startTime = 0;
    private final List<StatisticsCalculator> calculators = new ArrayList<>();
    private StatisticWriter statisticWriter = new StatisticWriter() {};


    private static class LazyHolder {
        static final StatisticsHolder statistics = new StatisticsHolder();
    }

    public static StatisticsHolder getInstance() {
        return LazyHolder.statistics;
    }

    public void setStatisticWriter(StatisticWriter statisticWriter) {
        this.statisticWriter = statisticWriter;
    }

    public void accept(StatisticsCalculator statisticsCalculator) {
        calculators.add(statisticsCalculator);
    }

    public void publishStatistics() {
        List<String> stats = new ArrayList<>();
        stats.add("----------------------------\n");
        calculators.forEach(statisticsCalculator ->
                stats.add(statisticsCalculator.calculate(this)));
        statisticWriter.writeStatistics(stats);

    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void reportMalformed() {
        malformedCount.incrementAndGet();
    }

    public void reportLineRead() {
        linesRead.incrementAndGet();
    }

    public void reportLineWritten() {
        linesWritten.incrementAndGet();
    }

    public void reportTrace() {
        tracesCount.incrementAndGet();
    }

    public void reportTraceDepth(int depth) {
        totalTraceDepth.addAndGet(depth);
    }

    public void reportTraceSize(int depth) {
        totalTraceSize.addAndGet(depth);
    }

    public void reportOrphan(Set<String> spanIds, String traceId) {
        spanIds.forEach((spanId) -> orphans.put(spanId, traceId));

    }

    public ConcurrentHashMap<String, String> getOrphans() {
        return orphans;
    }

    public AtomicLong getMalformedCount() {
        return malformedCount;
    }

    public AtomicLong getTotalTraceDepth() {
        return totalTraceDepth;
    }

    public AtomicLong getTotalTraceSize() {
        return totalTraceSize;
    }

    public AtomicLong getTracesCount() {
        return tracesCount;
    }

    public AtomicLong getLinesRead() {
        return linesRead;
    }

    public AtomicLong getLinesWritten() {
        return linesWritten;
    }

    public long getStartTime() {
        return startTime;
    }
}
