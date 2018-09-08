package exercise.processing;

import exercise.domain.LogEntry;
import exercise.domain.TraceRoot;
import exercise.domain.TraceStateHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AssemblerTest {

    private BlockingQueue<TraceRoot> traceRoots;
    private ConcurrentHashMap<String, TraceStateHolder> map;
    private LogEntry log1;
    private LogEntry log2;
    private LogEntry log3;
    private LogEntry log4;


    @BeforeEach
    public void setup() {
        traceRoots = new ArrayBlockingQueue<>(1);
        map = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, List<LogEntry>> logEntries = new ConcurrentHashMap<>();

        log1 = LogEntry.builder()
                .start("")
                .end("")
                .endEpoch(0L)
                .startEpoch(0L)
                .traceId("zxinxqxk")
                .service("")
                .callerSpan("null")
                .span("sw4z3x7m")
                .calls(new ArrayList<>())
                .build();
        log2 = LogEntry.builder()
                .start("")
                .end("")
                .endEpoch(0L)
                .startEpoch(0L)
                .traceId("zxinxqxk")
                .service("")
                .callerSpan("sw4z3x7m")
                .span("dkeotmsr")
                .calls(new ArrayList<>())
                .build();
        log3 = LogEntry.builder()
                .start("")
                .end("")
                .endEpoch(0L)
                .startEpoch(0L)
                .traceId("zxinxqxk")
                .service("")
                .callerSpan("sw4z3x7m")
                .span("3pf7lvi4")
                .calls(new ArrayList<>())
                .build();
        log4 = LogEntry.builder()
                .start("")
                .end("")
                .endEpoch(0L)
                .startEpoch(0L)
                .traceId("zxinxqxk")
                .service("")
                .callerSpan("3pf7lvi4")
                .span("ilk2wmay")
                .calls(new ArrayList<>())
                .build();


        ArrayList<LogEntry> list1 = new ArrayList<>();
        list1.add(log1);
        ArrayList<LogEntry> list2 = new ArrayList<>();
        list2.add(log2);
        list2.add(log3);
        ArrayList<LogEntry> list3 = new ArrayList<>();
        list3.add(log4);

        logEntries.put("null", list1);
        logEntries.put("sw4z3x7m", list2);
        logEntries.put("3pf7lvi4", list3);
        logEntries.put("ilk2wmay", new ArrayList<>());

        TraceStateHolder traceStateHolder = new TraceStateHolder(logEntries, true, 0);
        map.put("zxinxqxk", traceStateHolder);
    }
    @Test
    public void shouldCorrectlyAssemble() throws InterruptedException {
        Assembler assembler = new Assembler(traceRoots, "zxinxqxk", map);
        assembler.run();
        TraceRoot poll = traceRoots.poll(100, TimeUnit.MILLISECONDS);
        assertEquals("zxinxqxk",poll.getId());
        assertEquals("sw4z3x7m", poll.getRoot().getSpan());
        assertEquals(2, poll.getRoot().getCalls().size());
        assertEquals("3pf7lvi4", poll.getRoot().getCalls().get(0).getSpan());
        assertEquals("dkeotmsr", poll.getRoot().getCalls().get(1).getSpan());
        assertEquals(1, poll.getRoot().getCalls().get(0).getCalls().size());
        assertEquals(0, poll.getRoot().getCalls().get(1).getCalls().size());
    }

}
