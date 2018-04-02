package com.krillsson.sysapi.core.metrics.defaultimpl;

import com.krillsson.sysapi.core.SpeedMeasurementManager;
import com.krillsson.sysapi.core.metrics.*;
import com.krillsson.sysapi.util.Utils;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class DefaultMetricsFactory implements MetricsFactory {

    private final HardwareAbstractionLayer hal;
    private final OperatingSystem operatingSystem;
    private final SpeedMeasurementManager speedMeasurementManager;
    private final Utils utils;

    private CpuMetrics cpuMetrics;
    private NetworkMetrics networkMetrics;
    private GpuMetrics gpuMetrics;
    private DriveMetrics driveMetrics;
    private ProcessesMetrics processesMetrics;
    private MotherboardMetrics motherboardMetrics;
    private MemoryMetrics memoryMetrics;

    public DefaultMetricsFactory(HardwareAbstractionLayer hal, OperatingSystem operatingSystem, SpeedMeasurementManager speedMeasurementManager, Utils utils) {
        this.hal = hal;
        this.operatingSystem = operatingSystem;
        this.speedMeasurementManager = speedMeasurementManager;
        this.utils = utils;
    }

    @Override
    public boolean prerequisitesFilled() {
        return true;
    }

    @Override
    public boolean initialize() {
        setCpuMetrics(new DefaultCpuMetrics(hal, operatingSystem, new DefaultCpuSensors(hal), utils));
        DefaultNetworkMetrics networkInfoProvider = new DefaultNetworkMetrics(hal, speedMeasurementManager);
        networkInfoProvider.register();
        setNetworkMetrics(networkInfoProvider);
        setGpuMetrics(new DefaultGpuMetrics(hal));
        DefaultDriveProvider diskInfoProvider = new DefaultDriveProvider(operatingSystem, hal, speedMeasurementManager);
        diskInfoProvider.register();
        setDriveMetrics(diskInfoProvider);
        setProcessesMetrics(new DefaultProcessesMetrics(operatingSystem, hal));
        setMotherboardMetrics(new DefaultMotherboardMetrics(hal));
        setMemoryMetrics(new DefaultMemoryMetrics(hal));
        return true;
    }

    @Override
    public CpuMetrics cpuMetrics() {
        return cpuMetrics;
    }

    @Override
    public NetworkMetrics networkMetrics() {
        return networkMetrics;
    }

    @Override
    public DriveMetrics driveMetrics() {
        return driveMetrics;
    }

    @Override
    public MemoryMetrics memoryMetrics() {
        return memoryMetrics;
    }

    @Override
    public ProcessesMetrics processesMetrics() {
        return processesMetrics;
    }

    @Override
    public GpuMetrics gpuMetrics() {
        return gpuMetrics;
    }

    @Override
    public MotherboardMetrics motherboardMetrics() {
        return motherboardMetrics;
    }

    protected void setCpuMetrics(CpuMetrics cpuMetrics) {
        this.cpuMetrics = cpuMetrics;
    }

    protected void setNetworkMetrics(NetworkMetrics networkMetrics) {
        this.networkMetrics = networkMetrics;
    }

    protected void setGpuMetrics(GpuMetrics gpuMetrics) {
        this.gpuMetrics = gpuMetrics;
    }

    protected void setDriveMetrics(DriveMetrics driveMetrics) {
        this.driveMetrics = driveMetrics;
    }

    protected void setProcessesMetrics(ProcessesMetrics processesMetrics) {
        this.processesMetrics = processesMetrics;
    }

    protected void setMotherboardMetrics(MotherboardMetrics motherboardMetrics) {
        this.motherboardMetrics = motherboardMetrics;
    }

    protected void setMemoryMetrics(MemoryMetrics memoryMetrics) {
        this.memoryMetrics = memoryMetrics;
    }
}