package com.krillsson.sysapi.core.windows;

import com.krillsson.sysapi.core.SpeedMeasurementManager;
import com.krillsson.sysapi.core.domain.storage.DiskSpeed;
import ohmwrapper.DriveMonitor;
import ohmwrapper.MonitorManager;
import org.junit.Before;
import org.junit.Test;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WindowsDiskProviderTest {

    MonitorManager monitorManager;

    WindowsDiskProvider diskProvider;
    private HWDiskStore store;
    private OSFileStore osFileStore;
    private DriveMonitor driveMonitor;

    @Before
    public void setUp() throws Exception {
        monitorManager = mock(MonitorManager.class);
        store = mock(HWDiskStore.class);
        osFileStore = mock(OSFileStore.class);
        driveMonitor = mock(DriveMonitor.class);

        diskProvider = new WindowsDiskProvider(mock(OperatingSystem.class), mock(HardwareAbstractionLayer.class), mock(SpeedMeasurementManager.class));
        diskProvider.setMonitorManager(monitorManager);
    }

    @Test
    public void happyPath() {
        when(monitorManager.DriveMonitors()).thenReturn(new DriveMonitor[]{driveMonitor});
        when(osFileStore.getMount()).thenReturn("C:/");
        when(driveMonitor.getLogicalName()).thenReturn("C:/");
        when(driveMonitor.getWriteRate()).thenReturn(123d);
        when(driveMonitor.getReadRate()).thenReturn(321d);
        DiskSpeed diskSpeed = diskProvider.diskSpeedForStore(store, osFileStore);
        assertThat(diskSpeed.getWriteBytesPerSecond(), is(123L));
        assertThat(diskSpeed.getReadBytesPerSecond(), is(321L));
        verify(monitorManager).Update();
    }

    @Test
    public void nullReturnsDefaultValues() {
        DiskSpeed actual = diskProvider.diskSpeedForStore(null, null);
        assertThat(actual.getReadBytesPerSecond(), is(0L));
        assertThat(actual.getWriteBytesPerSecond(), is(0L));
    }
}