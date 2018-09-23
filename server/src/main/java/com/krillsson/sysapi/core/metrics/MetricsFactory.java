package com.krillsson.sysapi.core.metrics;

import com.krillsson.sysapi.core.domain.system.SystemInfo;
import com.krillsson.sysapi.core.domain.system.SystemLoad;
import com.krillsson.sysapi.util.EnvironmentUtils;
import oshi.software.os.OperatingSystem;

public interface MetricsFactory {
    boolean prerequisitesFilled();

    boolean initialize();

    CpuMetrics cpuMetrics();

    NetworkMetrics networkMetrics();

    DriveMetrics driveMetrics();

    MemoryMetrics memoryMetrics();

    ProcessesMetrics processesMetrics();

    GpuMetrics gpuMetrics();

    MotherboardMetrics motherboardMetrics();

    default SystemLoad consolidatedMetrics(){
        return consolidatedMetrics(OperatingSystem.ProcessSort.MEMORY, -1);
    }

    default SystemLoad consolidatedMetrics(OperatingSystem.ProcessSort sort, int limit) {
        return new SystemLoad(
                cpuMetrics().uptime(), cpuMetrics().cpuLoad(),
                networkMetrics().networkInterfaceLoads(),
                driveMetrics().driveLoads(),
                memoryMetrics().memoryLoad(),
                processesMetrics().processesInfo(sort, limit).getProcesses(), gpuMetrics().gpuLoads(),
                motherboardMetrics().motherboardHealth());
    }
}