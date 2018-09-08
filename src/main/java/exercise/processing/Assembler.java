package exercise.processing;

import exercise.domain.LogEntry;
import exercise.domain.TraceRoot;
import exercise.domain.TraceStateHolder;
import exercise.output.StandardOutputWriter;
import exercise.stats.StatisticsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


public class Assembler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Assembler.class);


    private final BlockingQueue<TraceRoot> outputQueue;
    private String traceId;
    private final ConcurrentHashMap<String, TraceStateHolder> map;

    public Assembler(BlockingQueue<TraceRoot> outputQueue, String traceId, ConcurrentHashMap<String, TraceStateHolder> map) {
        this.outputQueue = outputQueue;
        this.traceId = traceId;
        this.map = map;
    }

    @Override
    public void run() {
        Map<String, List<LogEntry>> logMap = map.remove(traceId).getEntries();
        Queue<String> callerSpans = new LinkedList<>();
        callerSpans.offer("null");
        Optional<LogEntry> root = logMap.get("null").stream().findFirst();
        if (root.isPresent()) {
            root.get().setDepth(1);
            int maxDepth = 1;
            StatisticsHolder.getInstance().reportTraceSize(logMap.size());
            while (!callerSpans.isEmpty()) {
                String callerSpan = callerSpans.poll();
                // remove in case we have circular dependency
                List<LogEntry> called = logMap.remove(callerSpan);
                if (called != null) {
                    for (LogEntry entry : called) {
                        String span = entry.getSpan();
                        List<LogEntry> calls = logMap.get(span);
                        if (calls != null) {
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