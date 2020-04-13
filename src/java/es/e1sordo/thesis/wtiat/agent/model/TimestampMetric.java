package es.e1sordo.thesis.wtiat.agent.model;

import java.util.List;

public class TimestampMetric {

    private long timestamp;
    private List<Object> metrics;

    public TimestampMetric(long timestamp, List<Object> metrics) {
        this.timestamp = timestamp;
        this.metrics = metrics;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Object> metrics) {
        this.metrics = metrics;
    }
}
