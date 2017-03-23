/*
 * Sys-Api (https://github.com/Krillsson/sys-api)
 *
 * Copyright 2017 Christian Jensen / Krillsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Maintainers:
 * contact[at]christian-jensen[dot]se
 */
package com.krillsson.sysapi.core.domain.cpu;

import oshi.hardware.CentralProcessor;

public class CpuInfo {
    private final CentralProcessor centralProcessor;
    private final int processCount;
    private final int threadCount;
    private final CpuLoad cpuLoad;
    private final CpuHealth cpuHealth;

    public CpuInfo(CentralProcessor centralProcessor, int processCount, int threadCount, CpuLoad cpuLoad, CpuHealth cpuHealth) {
        this.centralProcessor = centralProcessor;
        this.processCount = processCount;
        this.threadCount = threadCount;
        this.cpuLoad = cpuLoad;
        this.cpuHealth = cpuHealth;
    }

    public CentralProcessor getCentralProcessor() {
        return centralProcessor;
    }

    public int getProcessCount() {
        return processCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public CpuLoad getCpuLoad()
    {
        return cpuLoad;
    }

    public CpuHealth getCpuHealth() {
        return cpuHealth;
    }
}