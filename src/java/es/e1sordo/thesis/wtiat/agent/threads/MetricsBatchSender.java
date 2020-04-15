package es.e1sordo.thesis.wtiat.agent.threads;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;
import es.e1sordo.thesis.wtiat.agent.model.TimestampMetric;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsBatchSender implements Runnable {

    public MetricsBatchSender(AgentHttpClient client, String deviceId,
                              ConcurrentLinkedQueue<List<TimestampMetric>> metricsQueue) {
        this.client = client;
        this.deviceId = deviceId;
        this.metricsQueue = metricsQueue;
    }

    private AgentHttpClient client;
    private String deviceId;
    private final ConcurrentLinkedQueue<List<TimestampMetric>> metricsQueue;

    @Override
    public void run() {
        synchronized (metricsQueue) {
            final boolean status = client.loadMetrics(metricsQueue, deviceId);
            if (status) {
                metricsQueue.clear();
            }
        }
    }
}
