package com.krillsson.sysapi.core.monitoring.monitors

import com.krillsson.sysapi.core.domain.system.SystemLoad
import com.krillsson.sysapi.core.monitoring.Monitor
import com.krillsson.sysapi.core.monitoring.MonitorType
import java.time.Duration
import java.util.*

class MemoryMonitor(override val id: UUID, override val config: Monitor.Config) : Monitor {
    override val type: MonitorType = MonitorType.MEMORY_SPACE

    override fun value(systemLoad: SystemLoad): Double {
        return systemLoad.memory.available.toDouble()
    }

    override fun isPastThreshold(value: Double): Boolean {
        return value < config.threshold
    }
}