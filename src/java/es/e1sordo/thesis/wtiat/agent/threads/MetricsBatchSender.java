package es.e1sordo.thesis.wtiat.agent.threads;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsBatchSender implements Runnable {

    public MetricsBatchSender(AgentHttpClient client, ConcurrentLinkedQueue<Map<String, List<Object>>> metricsQueue) {
        this.client = client;
        this.metricsQueue = metricsQueue;
    }

    private AgentHttpClient client;
    private final ConcurrentLinkedQueue<Map<String, List<Object>>> metricsQueue;

    @Override
    public void run() {
        synchronized (metricsQueue) {
            final boolean status = client.loadMetrics(metricsQueue);
            if (status) {
                metricsQueue.clear();
            }
        }
    }
}
