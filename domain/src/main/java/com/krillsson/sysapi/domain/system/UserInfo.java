package com.krillsson.sysapi.domain.system;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInfo {
    private String user;
    private String device;
    private String host;
    private long time;

    public UserInfo(String user, String device, String host, long time) {
        this.user = user;
        this.device = device;
        this.host = host;
        this.time = time;
    }

    @JsonProperty
    public String getUser() {
        return user;
    }

    @JsonProperty
    public String getDevice() {
        return device;
    }

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public long getTime() {
        return time;
    }
}
