package exercise;

import exercise.stats.StatisticsHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Dispatcher implements Runnable {

    private static long ORPHANS_THRESHOLD = ConfigHolder.getConfig().getInt("orphansThreshold", 3000);
    private static long NULL_THRESHOLD = ConfigHolder.getConfig().getInt("nullThreshold", 300);

    private final NavigableMap<Long, List<String>> orphanMap;
    private final ConcurrentHashMap<String, TraceStateHolder> map;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final BlockingQueue<TraceRoot> outputQueue;
    private final BlockingQueue<String> inputQueue;
    private volatile boolean stop = false;
    private volatile boolean stopGracefully = true;

    public Dispatcher(NavigableMap<Long, List<String>> orphanMap,
                      ConcurrentHashMap<String, TraceStateHolder> map,
                      ScheduledThreadPoolExecutor scheduledThreadPoolExecutor,
                      BlockingQueue<TraceRoot> outputQueue,
                      BlockingQueue<String> inputQueue
    ) {
        this.orphanMap = orphanMap;
        this.map = map;
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        this.outputQueue = outputQueue;
        this.inputQueue = inputQueue;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                runDispatch(inputQueue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (stopGracefully) {
            ArrayList<String> logs = new ArrayList<>(inputQueue.size());
            inputQueue.drainTo(logs);
            logs.forEach(this::runDispatch);
        }
    }

    private void runDispatch(String rawEntry) {
        LogEntry logEntry = Utils.createDaoFromLog(rawEntry);
        if (!logEntry.isMalformed()) {
            dispatchEntry(logEntry);
        } else {
            StatisticsHolder.getInstance().reportMalformed();
        }
    }

    private void dispatchEntry(LogEntry logEntry) {

        orphanMap.computeIfAbsent(logEntry.getEndEpoch(), (key) -> new CopyOnWriteArrayList<>()).add(logEntry.getTraceId());

        TraceStateHolder stateHolder =
                map.computeIfAbsent(logEntry.getTraceId(), (key) -> new TraceStateHolder(new ConcurrentHashMap<>(), false, logEntry.getEndEpoch()));
        if (stateHolder.getLatestEndTimestamp() < logEntry.getEndEpoch()) {
            stateHolder.setLatestEndTimestamp(logEntry.getEndEpoch());
        }
        stateHolder.getEntries().computeIfAbsent(logEntry.getCallerSpan(), (key) -> new CopyOnWriteArrayList<>()).add(logEntry);

        if (logEntry.getCallerSpan().equals("null")) {
            stateHolder.setNullArrived(true);
            scheduledThreadPoolExecutor.schedule(new Assembler(outputQueue, logEntry.getTraceId(), map), NULL_THRESHOLD, TimeUnit.MILLISECONDS);
        }

        SortedMap<Long, List<String>> suspectedOrphans = orphanMap.tailMap(logEntry.getEndEpoch() - ORPHANS_THRESHOLD);
        for (Map.Entry<Long, List<String>> entry : suspectedOrphans.entrySet()) {
            for (String traceId : entry.getValue()) {
                TraceStateHolder holder = map.get(traceId);
                if (holder != null && !holder.isNullArrived() && logEntry.getEndEpoch() - ORPHANS_THRESHOLD > holder.getLatestEndTimestamp()) {
                    StatisticsHolder.getInstance().reportOrphan(holder.getEntries().keySet(), traceId);
                    System.err.println("ORPHANE: " + traceId + " " + holder + " \n");
                    map.remove(traceId);
                }
            }
            orphanMap.remove(entry.getKey());
        }

    }

    public void setStopFlag() {
        this.stop = true;
    }

    public void setStopFlag(boolean stopGracefully) {
        this.stop = true;
        this.stopGracefully = stopGracefully;
    }

}

/*
            TODO: move to docs
            This peace of code deals with entries that occur after "null" entry. It only tries to assemble
            entries after newer than some trashold arrives. According to trace-comparator such functionality is not necessary,
            but this is a way in which is could be accomplished.
            NavigableMap<Long, List<String>> nullMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));


            if (logEntry.getCallerSpan().equals("null")) {
                nullMap.computeIfAbsent(logEntry.getEndEpoch(), (k) -> new ArrayList<>()).add(logEntry.getTraceId());
            }

            //shedule from null Map and remove entries
            List<String> toCombine = new ArrayList<>();
            synchronized (nullMap) {
                SortedMap<Long, List<String>> toSerialize = nullMap.tailMap(logEntry.getEndEpoch() - NULL_THRESHOLD);
                for (Long aLong : toSerialize.keySet()) {
                    toCombine.addAll(nullMap.remove(aLong));
                }
            }
            for (String traceId : toCombine) {
                scheduledThreadPoolExecutor.execute(new Assembler(outputQueue, map.remove(traceId)));

            \}
        }*/



