package es.e1sordo.thesis.wtiat.agent.threads;

import es.e1sordo.thesis.wtiat.lib.ElectronicDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsCollector implements Runnable {

    final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    public MetricsCollector(ElectronicDevice device, List<List<String>> metrics,
                            ConcurrentLinkedQueue<Map<String, List<Object>>> metricsQueue) {
        this.device = device;
        this.metrics = metrics;
        this.metricsQueue = metricsQueue;
    }

    private ElectronicDevice device;
    private List<List<String>> metrics;
    private final ConcurrentLinkedQueue<Map<String, List<Object>>> metricsQueue;

    @Override
    public void run() {
        final var metricValues = new LinkedList<>();

        for (List<String> metric : metrics) {
            try {
                metricValues.add(device.readParameter(metric));
            } catch (Throwable e) {
                logger.error("Failed to read metric value", e);
                // sorry, but we just skip it. Null is null
                metricValues.add("");
            }
        }

        metricsQueue.add(Map.of(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                metricValues)
        );
    }
}
