package exercise.stats;

public class InputStatistic implements StatisticsCalculator {
    @Override
    public String calculate(StatisticsHolder statisticsHolder) {
        long runningTime = (System.nanoTime() - statisticsHolder.getStartTime()) / 1000000000;
        runningTime = runningTime == 0 ? 1 : runningTime;
        return String.format("Lines read: %d, Reading rate %d lines/s\n",
                statisticsHolder.getLinesRead().get(),
                statisticsHolder.getLinesRead().get()/runningTime
                );
    }
}
