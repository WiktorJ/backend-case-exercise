package exe;

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

    private static long ORPHANE_THRESHOLD = 3000;
    private static long NULL_THRESHOLD = 300;

    private final NavigableMap<Long, List<String>> orphanMap;
    private final ConcurrentHashMap<String, TraceStateHolder> map;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final BlockingQueue<TraceRoot> outputQueue;
    private BlockingQueue<String> inputQueue;
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
        try {
            while (!stop) {
                dispatchEntry(inputQueue.take());
            }
            if (stopGracefully) {
                ArrayList<String> logs = new ArrayList<>(inputQueue.size());
                inputQueue.drainTo(logs);
                System.out.println(logs.size());
                for (String log : logs) {
                    dispatchEntry(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void dispatchEntry(String rawEntry) {

        LogEntry logEntry = Utils.createDaoFromLog(rawEntry);

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

        SortedMap<Long, List<String>> suspectedOrphans = orphanMap.tailMap(logEntry.getEndEpoch() - ORPHANE_THRESHOLD);
        for (Map.Entry<Long, List<String>> entry : suspectedOrphans.entrySet()) {
            for (String s : entry.getValue()) {
                TraceStateHolder holder = map.get(s);
                if (holder != null && !holder.isNullArrived() && logEntry.getEndEpoch() - ORPHANE_THRESHOLD > holder.getLatestEndTimestamp()) {
                    System.err.println("ORPHANE: " + s + " " + holder + " \n" + logEntry);
                    map.remove(s);
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



