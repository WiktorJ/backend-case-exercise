package exe;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Assembler implements Runnable {


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
                        }
                        callerSpans.offer(span);
                    }
                }

            }
            try {
                outputQueue.put(new TraceRoot(root.get().getTraceId(), root.get()));
            } catch (InterruptedException e) {
                //TODO: Handle
                e.printStackTrace();
            }
        } else {
            //TODO: orphan?
        }

    }
}