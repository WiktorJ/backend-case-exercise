package exercise.stats;

public class OutputStatistic implements StatisticsCalculator {
    @Override
    public String calculate(StatisticsHolder statisticsHolder) {
        long runningTime = (System.nanoTime() - statisticsHolder.getStartTime()) / 1000000000;
        runningTime = runningTime == 0 ? 1 : runningTime;
        return String.format("Lines written: %d, Writing rate %d lines/s\n",
                statisticsHolder.getLinesWritten().get(),
                statisticsHolder.getLinesWritten().get()/runningTime
                );
    }
}
