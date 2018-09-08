package exercise.processing;

import exercise.domain.LogEntry;
import exercise.domain.TraceRoot;
import exercise.domain.TraceStateHolder;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Once null arrive and some time threshold passes this class will assemble LogEntries in to correct hierarchy.
 *
 */
public class Assembler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Assembler.class);


    private final BlockingQueue<TraceRoot> outputQueue;
    private final String traceId;
    private final ConcurrentHashMap<String, TraceStateHolder> tracesMap;

    /**
     *
     * @param outputQueue Queue where traces ready to serialize are placed.
     * @param traceId Id of trace to assemble.
     * @param tracesMap Map "traceId" -> "all services calls".
     */
    public Assembler(BlockingQueue<TraceRoot> outputQueue, String traceId, ConcurrentHashMap<String, TraceStateHolder> tracesMap) {
        this.outputQueue = outputQueue;
        this.traceId = traceId;
        this.tracesMap = tracesMap;
    }

    @Override
    public void run() {
        //Entry is removed from tracesMap. Time threshold passed so any lagging entries should be there.
        ConcurrentHashMap<String, List<LogEntry>> logMap = tracesMap.remove(traceId).getEntries();
        Queue<String> callerSpans = new LinkedList<>();
        callerSpans.offer("null");
        Optional<LogEntry> root = logMap.get("null").stream().findFirst();
        if (root.isPresent()) {
            //Statistics purposes
            root.get().setDepth(1);
            int maxDepth = 1;
            StatisticsHolder.getInstance().reportTraceSize(logMap.size());
            //In this loop the "assembly" is done
            //Every time spanId of one entry is equals to spanId of caller of another
            //This entry is added to the queue to be processed in next loops.
            while (!callerSpans.isEmpty()) {
                String callerSpan = callerSpans.poll();
                // remove in case we have circular dependency
                List<LogEntry> called = logMap.remove(callerSpan);
                if (called != null) {
                    for (LogEntry entry : called) {
                        String span = entry.getSpan();
                        List<LogEntry> calls = logMap.get(span);
                        //It might be that the log has no children
                        if (calls != null) {
                            //Sorting is not really necessary. I was trying to be more coherent with example files.
                            Collections.sort(calls);
                            entry.addCalls(calls);
                            calls.forEach((call) -> call.setDepth(entry.getDepth() + 1));
                            if (entry.getDepth() + 1 > maxDepth) {
                                maxDepth += 1;
                            }
                        }
                        callerSpans.offer(span);
                    }
                }

            }
            if (!logMap.isEmpty()) {
                //That means there were some logs missing in the middle, so we report orphans.
                StatisticsHolder.getInstance().reportOrphan(logMap.keySet(), traceId);
            }
            try {
                StatisticsHolder.getInstance().reportTraceDepth(maxDepth);
                StatisticsHolder.getInstance().reportTrace();
                outputQueue.put(new TraceRoot(root.get().getTraceId(), root.get()));
            } catch (InterruptedException e) {
                logger.warn("Interruption while assembling trace with id: {}", traceId, e);
            }
        } else {
            StatisticsHolder.getInstance().reportOrphan(logMap.keySet(), traceId);
        }

    }
}