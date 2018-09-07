package exe;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.*;

public class Dispatcher implements Runnable {

    private static long ORPHANE_THRESHOLD = 3000;
    private static long NULL_THRESHOLD = 300;

    //    private final BlockingQueue<String> outputQueue;
    private final NavigableMap<String, Long> orphanMap;
    private final NavigableMap<Long, List<String>> nullMap;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, List<LogEntry>>> map;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final BlockingQueue<TraceRoot> outputQueue;
    private final LogEntry logEntry;

    public Dispatcher(NavigableMap<String, Long> orphanMap,
                      NavigableMap<Long, List<String>> nullMap,
                      ConcurrentHashMap<String, ConcurrentHashMap<String, List<LogEntry>>> map,
                      ScheduledThreadPoolExecutor scheduledThreadPoolExecutor,
                      BlockingQueue<TraceRoot> outputQueue,
                      String logEntry) {
        this.orphanMap = orphanMap;
        this.nullMap = nullMap;
        this.map = map;
        this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
        this.outputQueue = outputQueue;
        this.logEntry = Utils.createDaoFromLog(logEntry);
//        this.outputQueue = outputQueue;
//        this.map = new ConcurrentHashMap<>();
//        this.orphanMap = new ConcurrentSkipListSet<>();
    }

    @Override
    public void run() {
        //update time for this trace id
//        if (logEntry.getCallerSpan().equals("null")) {
//            nullMap.computeIfAbsent(logEntry.getEndEpoch(), (k) -> new ArrayList<>()).add(logEntry.getTraceId());
//        }


//        orphanMap.compute(logEntry.getTraceId(), (key, value) -> {
//            // NOT ATOMIC! However in most cases is shouldn't be an issue, as timeout for orphans doesn't have to be strictly preserved
//            if (value == null || value < logEntry.getEnd()) {
//                return logEntry.getEnd();
//            }
//            return value;
//        });

        //put new entry associated wiht this trace id
        map
                .computeIfAbsent(logEntry.getTraceId(), (key) -> new ConcurrentHashMap<>())
                .computeIfAbsent(logEntry.getCallerSpan(), (key) -> new ArrayList<>())
                .add(logEntry);

        if (logEntry.getCallerSpan().equals("null")) {
            scheduledThreadPoolExecutor.schedule(new Assembler(outputQueue, logEntry.getTraceId(), map), NULL_THRESHOLD, TimeUnit.MILLISECONDS);
        }


        //shedule from null Map and remove entries
//        List<String> toCombine = new ArrayList<>();
//        synchronized (nullMap) {
//            SortedMap<Long, List<String>> toSerialize = nullMap.tailMap(logEntry.getEndEpoch() - NULL_THRESHOLD);
//            for (Long aLong : toSerialize.keySet()) {
//                toCombine.addAll(nullMap.remove(aLong));
//            }
//        }
//        for (String traceId : toCombine) {
//            scheduledThreadPoolExecutor.execute(new Assembler(outputQueue, map.remove(traceId)));
//        }

        //remove all the "orphans"

    }


}
