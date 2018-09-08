package exercise.processing;

import exercise.domain.TraceRoot;
import exercise.domain.TraceStateHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DispatcherTest {


    private Dispatcher dispatcher;
    private ConcurrentHashMap<String, TraceStateHolder> map;
    private BlockingQueue<String> inputQueue;
    private NavigableMap<Long, List<String>> orphanMap;
    @Mock
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    @Mock
    private BlockingQueue<TraceRoot> outputQueue;

    @BeforeEach
    public void setUp() {
        map = new ConcurrentHashMap<>();
        inputQueue = new ArrayBlockingQueue<>(1);
        orphanMap = new ConcurrentSkipListMap<>((key1, key2) -> -Long.compare(key1, key2));
        dispatcher = new Dispatcher(orphanMap, map, scheduledThreadPoolExecutor, outputQueue, inputQueue);
    }

    @Test
    public void shouldDispatch() throws InterruptedException {
        inputQueue.put("2013-10-23T10:12:35.739Z 2013-10-23T10:12:35.740Z 6eydtfin service8 y7qvl6cc->ulpcjbc5");
        Thread thread = new Thread(dispatcher);
        thread.start();
        dispatcher.setStopFlag();
        thread.interrupt();
        thread.join();
        assertEquals(1, orphanMap.size());
        assertEquals(1, map.size());
        assertTrue(map.keySet().contains("6eydtfin"));
        assertEquals(1, map.get("6eydtfin").getEntries().size());
        assertNotNull( map.get("6eydtfin").getEntries().get("y7qvl6cc"));
        assertEquals(1, map.get("6eydtfin").getEntries().get("y7qvl6cc").size());
        assertEquals("service8", map.get("6eydtfin").getEntries().get("y7qvl6cc").get(0).getService());
        assertEquals("2013-10-23T10:12:35.739Z", map.get("6eydtfin").getEntries().get("y7qvl6cc").get(0).getStart());

    }
}
