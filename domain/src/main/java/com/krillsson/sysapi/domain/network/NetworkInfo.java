package com.krillsson.sysapi.domain.network;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class NetworkInfo {
    private final String defaultGateway, hostName, domainName, primaryDns, secondaryDns;

    private List<NetworkInterfaceConfig> networkInterfaceConfigs;

    public NetworkInfo(String defaultGateway, String hostName, String domainName, String primaryDns, String secondaryDns) {
        this.defaultGateway = defaultGateway;
        this.hostName = hostName;
        this.domainName = domainName;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
    }

    public NetworkInfo(String defaultGateway, String hostName, String domainName, String primaryDns, String secondaryDns,
                       List<NetworkInterfaceConfig> networkInterfaceConfigs) {
        this(defaultGateway,hostName,domainName,primaryDns,secondaryDns);
        this.networkInterfaceConfigs = networkInterfaceConfigs;
    }

    @JsonProperty
    public String getDefaultGateway() {
        return defaultGateway;
    }

    @JsonProperty
    public String getHostName() {
        return hostName;
    }

    @JsonProperty
    public String getDomainName() {
        return domainName;
    }

    @JsonProperty
    public String getPrimaryDns() {
        return primaryDns;
    }

    @JsonProperty
    public String getSecondaryDns() {
        return secondaryDns;
    }
    @JsonProperty
    public List<NetworkInterfaceConfig> getNetworkInterfaceConfigs() {
        return networkInterfaceConfigs;
    }
}
