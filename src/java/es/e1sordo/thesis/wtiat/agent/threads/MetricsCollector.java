package es.e1sordo.thesis.wtiat.agent.threads;

import es.e1sordo.thesis.wtiat.agent.model.TimestampMetric;
import es.e1sordo.thesis.wtiat.lib.ElectronicDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsCollector implements Runnable {

    final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);

    public MetricsCollector(ElectronicDevice device, List<List<String>> metrics,
                            ConcurrentLinkedQueue<List<TimestampMetric>> metricsQueue) {
        this.device = device;
        this.metrics = metrics;
        this.metricsQueue = metricsQueue;
    }

    private ElectronicDevice device;
    private List<List<String>> metrics;
    private final ConcurrentLinkedQueue<List<TimestampMetric>> metricsQueue;

    @Override
    public void run() {
        final var metricValues = new LinkedList<TimestampMetric>();

        for (List<String> metricReadParams : metrics) {
            try {
                metricValues.add(
                        new TimestampMetric(
                                System.currentTimeMillis(),
                                device.readParameter(metricReadParams)
                        )
                );
            } catch (Throwable e) {
                logger.error("Failed to read metric value", e);
                // sorry, but we just skip it. Null is null
                metricValues.add(null);
            }
        }

        metricsQueue.add(metricValues);
    }
}
