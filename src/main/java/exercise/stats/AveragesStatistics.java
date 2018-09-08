package exercise.stats;

public class AveragesStatistics implements StatisticsCalculator {
    @Override
    public String calculate(StatisticsHolder statisticsHolder) {
        return  String.format("Average trace depth: %3.2f, Average trace size: %3.2f\n",
                statisticsHolder.getTotalTraceDepth().get()/(double) statisticsHolder.getTracesCount().get(),
                statisticsHolder.getTotalTraceSize().get()/(double) statisticsHolder.getTracesCount().get()
        );
    }
}
