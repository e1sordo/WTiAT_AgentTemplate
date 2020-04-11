package es.e1sordo.thesis.wtiat.agent.dto;

import java.util.List;

public class DeviceGetDto {

    private String id;
    private String name;
    private String connectorName;
    private Integer gatheringFrequencyInMillis;
    private Integer batchSendingFrequencyInMillis;
    private List<String> connectionValues;
    private List<List<String>> metrics;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public Integer getGatheringFrequencyInMillis() {
        return gatheringFrequencyInMillis;
    }

    public void setGatheringFrequencyInMillis(Integer gatheringFrequencyInMillis) {
        this.gatheringFrequencyInMillis = gatheringFrequencyInMillis;
    }

    public Integer getBatchSendingFrequencyInMillis() {
        return batchSendingFrequencyInMillis;
    }

    public void setBatchSendingFrequencyInMillis(Integer batchSendingFrequencyInMillis) {
        this.batchSendingFrequencyInMillis = batchSendingFrequencyInMillis;
    }

    public List<String> getConnectionValues() {
        return connectionValues;
    }

    public void setConnectionValues(List<String> connectionValues) {
        this.connectionValues = connectionValues;
    }

    public List<List<String>> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<List<String>> metrics) {
        this.metrics = metrics;
    }
}
