package com.krillsson.sysapi.domain.processes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperic.sigar.ProcMem;

public class ProcessMemory {
    long size,
            resident,
            share,
            minorFaults,
            majorFaults,
            pageFaults;

    public ProcessMemory(long size, long resident, long share, long minorFaults, long majorFaults, long pageFaults) {
        this.size = size;
        this.resident = resident;
        this.share = share;
        this.minorFaults = minorFaults;
        this.majorFaults = majorFaults;
        this.pageFaults = pageFaults;
    }

    public ProcessMemory() {

    }

    @JsonProperty
    public long getSize() {
        return size;
    }

    @JsonProperty
    public long getResident() {
        return resident;
    }

    @JsonProperty
    public long getShare() {
        return share;
    }

    @JsonProperty
    public long getMinorFaults() {
        return minorFaults;
    }

    @JsonProperty
    public long getMajorFaults() {
        return majorFaults;
    }

    @JsonProperty
    public long getPageFaults() {
        return pageFaults;
    }
}