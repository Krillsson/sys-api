package com.krillsson.sysapi.core.windows;

import com.krillsson.sysapi.core.InfoProvider;
import com.krillsson.sysapi.core.InfoProviderBase;
import com.krillsson.sysapi.domain.gpu.Gpu;
import com.krillsson.sysapi.domain.gpu.GpuHealth;
import com.krillsson.sysapi.domain.health.DataType;
import com.krillsson.sysapi.domain.health.HealthData;
import com.krillsson.sysapi.domain.storage.HWDiskHealth;
import net.sf.jni4net.Bridge;
import ohmwrapper.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.krillsson.sysapi.core.windows.util.NullSafeOhmMonitor.nullSafe;
import static com.krillsson.sysapi.util.JarLocation.*;

public class WindowsInfoProvider extends InfoProviderBase implements InfoProvider {

    private Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WindowsInfoProvider.class);

    private static final File OHM_JNI_WRAPPER_DLL = new File(LIB_LOCATION + SEPARATOR + "OhmJniWrapper.dll");
    private static final File OPEN_HARDWARE_MONITOR_LIB_DLL = new File(LIB_LOCATION + SEPARATOR + "OpenHardwareMonitorLib.dll");
    private static final File OHM_JNI_WRAPPER_J4N_DLL = new File(LIB_LOCATION + SEPARATOR + "OhmJniWrapper.j4n.dll");

    private MonitorManager monitorManager;

    public WindowsInfoProvider() {

    }

    @Override
    public boolean canProvide() {
        return OHM_JNI_WRAPPER_DLL.exists() &&
                OPEN_HARDWARE_MONITOR_LIB_DLL.exists() &&
                OHM_JNI_WRAPPER_DLL.exists() &&
                initBridge();
    }

    public Gpu[] gpus() {
        List<Gpu> gpus = new ArrayList<>();
        monitorManager.Update();
        final GpuMonitor[] gpuMonitors = monitorManager.GpuMonitors();
        if (gpuMonitors != null && gpuMonitors.length > 0) {
            for (GpuMonitor gpuMonitor : gpuMonitors) {
                GpuHealth gpuHealth = new GpuHealth(
                        nullSafe(gpuMonitor.getFanRPM()).getValue(), nullSafe(gpuMonitor.getFanPercent()).getValue(), nullSafe(gpuMonitor.getTemperature()).getValue(),
                        nullSafe(gpuMonitor.getCoreLoad()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue());
                Gpu gpu = new Gpu(
                        gpuMonitor.getVendor(),
                        gpuMonitor.getName(),
                        nullSafe(gpuMonitor.getCoreClock()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue(),
                        gpuHealth
                );
                gpus.add(gpu);
            }
        }
        return gpus.toArray(/*type reference*/new Gpu[0]);
    }

    public GpuHealth[] gpuHealths() {
        List<GpuHealth> gpus = new ArrayList<>();
        monitorManager.Update();
        final GpuMonitor[] gpuMonitors = monitorManager.GpuMonitors();
        if (gpuMonitors != null && gpuMonitors.length > 0) {
            for (GpuMonitor gpuMonitor : gpuMonitors) {
                GpuHealth gpuHealth = new GpuHealth(
                        nullSafe(gpuMonitor.getFanRPM()).getValue(), nullSafe(gpuMonitor.getFanPercent()).getValue(), nullSafe(gpuMonitor.getTemperature()).getValue(),
                        nullSafe(gpuMonitor.getCoreLoad()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue());
                gpus.add(gpuHealth);
            }
        }
        return gpus.toArray(/*type reference*/new GpuHealth[0]);
    }

    private boolean initBridge() {
        LOGGER.info("Enabling OHMJNIWrapper impl. Disable this in the configuration.yml (see README.md)");
        Bridge.setDebug(true);
        try {
            Bridge.init();
        } catch (IOException e) {
            LOGGER.error("Trouble while initializing JNI4Net Bridge. Do I have admin privileges?", e);
            return false;
        }

        OHMManagerFactory factory = loadFromInstallDir();
        try {
            factory.init();
            this.monitorManager = factory.GetManager();
            return true;
        } catch (Exception e) {
            LOGGER.error("Trouble while initializing JNI4Net Bridge. Do I have admin privileges?", e);
            return false;
        }
    }

    private OHMManagerFactory loadFromInstallDir() {
        try {
            Bridge.LoadAndRegisterAssemblyFrom(OHM_JNI_WRAPPER_DLL);
            Bridge.LoadAndRegisterAssemblyFrom(OHM_JNI_WRAPPER_J4N_DLL);
            Bridge.LoadAndRegisterAssemblyFrom(OPEN_HARDWARE_MONITOR_LIB_DLL);
            return new OHMManagerFactory();
        } catch (Exception e) {
            LOGGER.error("Unable to load OHM from installation directory {}", SOURCE_LIB_LOCATION, e);
            return null;
        }

    }

    @Override
    public HWDiskHealth diskHealth(String name) {
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        for (DriveMonitor driveMonitor : driveMonitors) {
            if (driveMonitor.getLogicalName().equals(name)) {
                List<HealthData> healthData = new ArrayList<>();
                addIfSafe(healthData, driveMonitor.getRemainingLife());
                if (driveMonitor.getLifecycleData() != null) {
                    for (OHMSensor sensor : driveMonitor.getLifecycleData()) {
                        addIfSafe(healthData, sensor);
                    }
                }

                return new HWDiskHealth(nullSafe(driveMonitor.getTemperature()).getValue(), healthData);
            }
        }
        return null;


    }

    @Override
    public double[] cpuTemperatures() {
        double[] temperatures = new double[0];
        monitorManager.Update();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            temperatures = new double[]{nullSafe(cpuMonitor.getPackageTemperature()).getValue()};
            if (nullSafe(cpuMonitor.getTemperatures()).length >= 1) {
                final OHMSensor[] sensors = cpuMonitor.getTemperatures();
                temperatures = new double[sensors.length];
                for (int i = 0; i < sensors.length; i++) {
                    OHMSensor sensor = sensors[i];
                    temperatures[i] = sensor.getValue();
                }
            }
        }
        return temperatures;
    }

    @Override
    public double cpuFanRpm() {
        monitorManager.Update();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            return nullSafe(cpuMonitor.getFanRPM()).getValue();
        }
        return 0;
    }

    @Override
    public double cpuFanPercent() {
        monitorManager.Update();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            return nullSafe(cpuMonitor.getFanPercent()).getValue();
        }
        return 0;
    }

    @Override
    public List<HealthData> healthData() {
        List<HealthData> list = new ArrayList<>();
        monitorManager.Update();
        MainboardMonitor mainboardMonitor = monitorManager.getMainboardMonitor();
        if (mainboardMonitor != null) {
            addIfSafe(list, nullSafe(mainboardMonitor.getBoardFanPercent()));
            addIfSafe(list, nullSafe(mainboardMonitor.getBoardFanRPM()));
            addIfSafe(list, nullSafe(mainboardMonitor.getBoardTemperatures()));
        }
        return list;
    }

    private void addIfSafe(List<HealthData> healthData, OHMSensor sensor) {
        OHMSensor ohmSensor = nullSafe(sensor);
        if (ohmSensor.getValue() > 0) {
            com.krillsson.sysapi.domain.health.DataType dataType = DataType.valueOf(sensor.getDataType().toString().toUpperCase());
            healthData.add(new HealthData(ohmSensor.getLabel(), ohmSensor.getValue(), dataType));
        }
    }

    private void addIfSafe(List<HealthData> healthDataList, OHMSensor[] sensors) {
        for (OHMSensor sensor : sensors) {
            addIfSafe(healthDataList, sensor);
        }
    }
}
