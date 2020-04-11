package es.e1sordo.thesis.wtiat.agent.threads;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MetricsBatchSender implements Runnable {

    final Logger logger = LoggerFactory.getLogger(MetricsBatchSender.class);

    public MetricsBatchSender(AgentHttpClient client, ConcurrentLinkedQueue<Map<String, String>> metricsQueue) {
        this.client = client;
        this.metricsQueue = metricsQueue;
    }

    private AgentHttpClient client;
    private final ConcurrentLinkedQueue<Map<String, String>> metricsQueue;

    @Override
    public void run() {
        synchronized (metricsQueue) {
            client.loadMetrics(metricsQueue);
            metricsQueue.clear();
        }
    }
}
