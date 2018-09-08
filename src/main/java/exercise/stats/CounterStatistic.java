package exercise.stats;

public class CounterStatistic implements StatisticsCalculator {
    @Override
    public String calculate(StatisticsHolder statisticsHolder) {
           return String.format("Number of orphans: %d, number of malformed logs: %d\n",
                    statisticsHolder.getOrphans().keySet().size(),
                    statisticsHolder.getMalformedCount().get());
    }
}
