package exercise.processing;

import exercise.*;
import exercise.domain.LogEntry;
import exercise.domain.TraceRoot;
import exercise.domain.TraceStateHolder;
import exercise.stats.StatisticsHolder;
import exercise.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


/**
 * This class takes raw logs from reader and transforms them into LogEntries.
 * It also checks for orphan entries and tries to compensate for lack of order in some cases (waits with assembly
 * for some time to catch all entries that appeared after "null" entry from their trace)
 */
public class Dispatcher implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private static long ORPHANS_THRESHOLD = ConfigHolder.getConfig().getInt("orphansThreshold", 3000);
    private static long NULL_THRESHOLD = ConfigHolder.getConfig().getInt("nullThreshold", 300);

    private final NavigableMap<Long, List<String>> orphanMap;
    private final ConcurrentHashMap<String, TraceStateHolder> tracesMap;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final BlockingQueue<TraceRoot> outputQueue;
    private final BlockingQueue<String> inputQueue;
    private volatile boolean stop = false;
    private volatile boolean stopGracefully = true;

    /**
     *
     * @param orphanMap Mapping "timestamp" -> "List of traceId from this time".
     * @param tracesMap Map "traceId" -> "all services calls".
     * @param scheduledThreadPoolExecutor Executor for "Assemblers".
     * @param outputQueue Queue for "Assemblers".
     * @param inputQueue Queue where raw entries arrive.
     */
    public Dispatcher(NavigableMap<Long, List<String>> orphanMap,
                      ConcurrentHashMap<String, TraceStateHolder> tracesMap,
                      ScheduledThreadPoolExecutor scheduledThreadPoolExecutor,
                      BlockingQueue<TraceRoot> outputQueue,
                      BlockingQueue<String> inputQueue
    ) {
        this.orphanMap = orphanMap;
        this.tracesMap = tracesMap;
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
                logger.info("Interruption while writing to file, stop = {}", stop, e);
            }
        }
        //If stop signal was received but there are still some entries in the queue
        //they should be processed.
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

        //Each time entry arrive we log its timestamp in this map. Later it's used to check whether
        //particular entry was in the memory long enough to be considered an orphan.
        orphanMap.computeIfAbsent(logEntry.getEndEpoch(), (key) -> new CopyOnWriteArrayList<>()).add(logEntry.getTraceId());


        //New entry is registered under traceId
        TraceStateHolder stateHolder =
                tracesMap.computeIfAbsent(logEntry.getTraceId(), (key) -> new TraceStateHolder(new ConcurrentHashMap<>(), false, logEntry.getEndEpoch()));

        //Since it's not synchronized it's possible that we won't get latest timestamp.
        //This information is only used to determine orphan entries and doesn't have to be 100% accurate.
        if (stateHolder.getLatestEndTimestamp() < logEntry.getEndEpoch()) {
            stateHolder.setLatestEndTimestamp(logEntry.getEndEpoch());
        }

        //New entry is registered under caller span id. Thread safe collection is used because it's traversed later in Assembler.
        stateHolder.getEntries().computeIfAbsent(logEntry.getCallerSpan(), (key) -> new CopyOnWriteArrayList<>()).add(logEntry);

        //When "null" entry arrive, assembly process is scheduled. Also Flag that null arrived is set, so the entries w
        //will not be considered orphans.
        if (logEntry.getCallerSpan().equals("null")) {
            stateHolder.setNullArrived(true);
            scheduledThreadPoolExecutor.schedule(new Assembler(outputQueue, logEntry.getTraceId(), tracesMap), NULL_THRESHOLD, TimeUnit.MILLISECONDS);
        }

        //Here we take all the "suspectedOrphans". Those are all the entries Older then given threshold then currently processed entry.
        SortedMap<Long, List<String>> suspectedOrphans = orphanMap.tailMap(logEntry.getEndEpoch() - ORPHANS_THRESHOLD);
        for (Map.Entry<Long, List<String>> entry : suspectedOrphans.entrySet()) {
            for (String traceId : entry.getValue()) {
                TraceStateHolder holder = tracesMap.get(traceId);
                //Entry might have been already assembled, scheduled for assembly, or there were newer entries with the same trace id.
                //Other wise entry is an orphan.
                if (holder != null && !holder.isNullArrived() && logEntry.getEndEpoch() - ORPHANS_THRESHOLD > holder.getLatestEndTimestamp()) {
                    StatisticsHolder.getInstance().reportOrphan(holder.getEntries().keySet(), traceId);
                    tracesMap.remove(traceId);
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
            An alternative way to deal with entries that arrive after "their null" entry. This one uses relative time.
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
                scheduledThreadPoolExecutor.execute(new Assembler(outputQueue, tracesMap.remove(traceId)));

            \}
}
*/



