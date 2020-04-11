package es.e1sordo.thesis.wtiat.agent.dto;

public class AgentGetDto {

    private String id;
    private String name;
    private String ip;
    private Integer pid;
    private String registerDate;
    private String assignedDeviceId;
    private String assignedDate;
    private Boolean shouldTerminate;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getAssignedDeviceId() {
        return assignedDeviceId;
    }

    public void setAssignedDeviceId(String assignedDeviceId) {
        this.assignedDeviceId = assignedDeviceId;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }

    public Boolean getShouldTerminate() {
        return shouldTerminate;
    }

    public void setShouldTerminate(Boolean shouldTerminate) {
        this.shouldTerminate = shouldTerminate;
    }
}
