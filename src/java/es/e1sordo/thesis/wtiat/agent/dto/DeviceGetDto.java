package es.e1sordo.thesis.wtiat.agent.dto;

import java.util.List;
import java.util.Objects;

public class DeviceGetDto {

    private String id;
    private String name;
    private String connectorName;
    private Integer gatheringFrequencyInMillis;
    private Integer batchSendingFrequencyInMillis;
    private List<String> connectionValues;
    private List<List<String>> metrics;
    private List<String> metricTypes;

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

    public List<String> getMetricTypes() {
        return metricTypes;
    }

    public void setMetricTypes(List<String> metricTypes) {
        this.metricTypes = metricTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceGetDto that = (DeviceGetDto) o;
        return getId().equals(that.getId()) &&
                getName().equals(that.getName()) &&
                getConnectorName().equals(that.getConnectorName()) &&
                getGatheringFrequencyInMillis().equals(that.getGatheringFrequencyInMillis()) &&
                getBatchSendingFrequencyInMillis().equals(that.getBatchSendingFrequencyInMillis()) &&
                Objects.equals(getConnectionValues(), that.getConnectionValues()) &&
                Objects.equals(getMetrics(), that.getMetrics()) &&
                Objects.equals(getMetricTypes(), that.getMetricTypes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(), getName(), getConnectorName(), getGatheringFrequencyInMillis(),
                getBatchSendingFrequencyInMillis(), getConnectionValues(), getMetrics(), getMetricTypes()
        );
    }
}
