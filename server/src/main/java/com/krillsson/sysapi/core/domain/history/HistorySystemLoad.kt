package com.krillsson.sysapi.core.domain.history

import com.krillsson.sysapi.core.domain.cpu.CpuLoad
import com.krillsson.sysapi.core.domain.drives.DriveLoad
import com.krillsson.sysapi.core.domain.gpu.GpuLoad
import com.krillsson.sysapi.core.domain.memory.MemoryLoad
import com.krillsson.sysapi.core.domain.network.NetworkInterfaceLoad
import com.krillsson.sysapi.core.domain.sensors.HealthData

class HistorySystemLoad(
    val uptime: Long,
    val systemLoadAverage: Double,
    val cpuLoad: CpuLoad,
    val networkInterfaceLoads: List<NetworkInterfaceLoad>,
    val driveLoads: List<DriveLoad>,
    val memory: MemoryLoad,
    val gpuLoads: List<GpuLoad>,
    val motherboardHealth: List<HealthData>
)