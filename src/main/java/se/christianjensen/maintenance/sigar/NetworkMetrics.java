package se.christianjensen.maintenance.sigar;


import org.hyperic.sigar.*;
import se.christianjensen.maintenance.representation.network.NetworkInfo;
import se.christianjensen.maintenance.representation.network.NetworkInterfaceConfig;
import se.christianjensen.maintenance.representation.network.NetworkInterfaceSpeed;
import se.christianjensen.maintenance.representation.network.NetworkInterfaceStatistics;

import java.util.ArrayList;
import java.util.List;

public class NetworkMetrics extends AbstractSigarMetric {

    final int SPEED_MEASUREMENT_PERIOD = 100;
    final int BYTE_TO_BIT = 8;


    protected NetworkMetrics(Sigar sigar) {
        super(sigar);
    }

    public List<NetworkInterfaceConfig> getConfigs() {
        String[] netIfs;
        ArrayList<NetworkInterfaceConfig> configs = new ArrayList<>();
        try {
            netIfs = sigar.getNetInterfaceList();
            for (String name : netIfs) {
                NetworkInterfaceConfig networkInterfaceConfig = NetworkInterfaceConfig.fromSigarBean(sigar.getNetInterfaceConfig(name));
                networkInterfaceConfig.setNetworkInterfaceStatistics(NetworkInterfaceStatistics.fromSigarBean(sigar.getNetInterfaceStat(name)));
                networkInterfaceConfig.setNetworkInterfaceSpeed(getSpeed(name));
                configs.add(networkInterfaceConfig);
            }
        } catch (SigarException | InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
        if (!configs.isEmpty()) {
            return configs;
        } else {
            throw new IllegalArgumentException("No network interfaces where found");
        }
    }

    public NetworkInterfaceConfig getConfigById(String id) {
        NetworkInterfaceConfig config;

        try {
            NetInterfaceConfig sigarConfig = sigar.getNetInterfaceConfig(id);
            config = NetworkInterfaceConfig.fromSigarBean(sigarConfig);
            config.setNetworkInterfaceStatistics(NetworkInterfaceStatistics.fromSigarBean(sigar.getNetInterfaceStat(id)));
            config.setNetworkInterfaceSpeed(getSpeed(id));
        } catch (SigarException | InterruptedException | IllegalArgumentException e) {
            throw new IllegalArgumentException("No NetworkInterfaceConfig with id " + id + " were found", e);
        }

        return config;
    }

    public NetworkInfo getNetworkInfo() {
        NetInfo sigarNetInfo;
        NetworkInfo networkInfo = null;
        List<NetworkInterfaceConfig> configs;

        try {
            sigarNetInfo = sigar.getNetInfo();
            configs = getConfigs();
            networkInfo = NetworkInfo.fromSigarBean(sigarNetInfo, configs);
        } catch (SigarException e) {
            throw new IllegalArgumentException(e.getCause());
        }
        return networkInfo;
    }

    public NetworkInterfaceSpeed getSpeed(String networkInterfaceConfigName) throws InterruptedException, SigarException {
        long rxbps, txbps;
        long start = 0;
        long end = 0;
        long rxBytesStart = 0;
        long rxBytesEnd = 0;
        long txBytesStart = 0;
        long txBytesEnd = 0;

        start = System.currentTimeMillis();
        NetInterfaceStat statStart = sigar.getNetInterfaceStat(networkInterfaceConfigName);
        rxBytesStart = statStart.getRxBytes();
        txBytesStart = statStart.getTxBytes();
        Thread.sleep(SPEED_MEASUREMENT_PERIOD);
        NetInterfaceStat statEnd = sigar.getNetInterfaceStat(networkInterfaceConfigName);
        end = System.currentTimeMillis();
        rxBytesEnd = statEnd.getRxBytes();
        txBytesEnd = statEnd.getTxBytes();

        rxbps = measureSpeed(start, end, rxBytesStart, rxBytesEnd);
        txbps = measureSpeed(start, end, txBytesStart, txBytesEnd);
        return new NetworkInterfaceSpeed(rxbps, txbps);
    }

    private long measureSpeed(long start, long end, long rxBytesStart, long rxBytesEnd) {
        return (rxBytesEnd - rxBytesStart) * BYTE_TO_BIT / (end - start) * SPEED_MEASUREMENT_PERIOD;
    }


}
