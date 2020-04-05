package es.e1sordo.thesis.wtiat.agent.model;

public class GatewayInfo {

    private String protocol;
    private String host;
    private Integer port;

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public GatewayInfo setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public GatewayInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public GatewayInfo setPort(Integer port) {
        this.port = port;
        return this;
    }
}
