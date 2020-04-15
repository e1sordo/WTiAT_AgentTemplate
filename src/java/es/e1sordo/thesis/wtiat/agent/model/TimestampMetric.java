package es.e1sordo.thesis.wtiat.agent.model;

public class TimestampMetric {

    private long timestamp;
    private Object value;

    public TimestampMetric(long timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
