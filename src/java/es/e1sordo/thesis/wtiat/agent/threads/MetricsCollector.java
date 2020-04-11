package es.e1sordo.thesis.wtiat.agent.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsCollector implements Runnable {

    final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    public MetricsCollector(ConcurrentLinkedQueue<Map<String, String>> metricsQueue) {
        this.metricsQueue = metricsQueue;
    }

    private ConcurrentLinkedQueue<Map<String, String>> metricsQueue;

    @Override
    public void run() {
        metricsQueue.add(Map.of(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                "metric")
        );
    }
}
