package com.krillsson.sysapi.extension.windows;

import com.krillsson.sysapi.domain.drive.LifecycleData;
import com.krillsson.sysapi.domain.gpu.Gpu;
import com.krillsson.sysapi.domain.gpu.GpuInfo;
import com.krillsson.sysapi.domain.gpu.GpuLoad;
import com.krillsson.sysapi.domain.motherboard.Motherboard;
import com.krillsson.sysapi.domain.storage.HWDiskHealth;
import com.krillsson.sysapi.extension.InfoProvider;
import com.krillsson.sysapi.extension.InfoProviderBase;
import net.sf.jni4net.Bridge;
import ohmwrapper.*;
import org.slf4j.Logger;
import oshi.json.hardware.HWDiskStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.krillsson.sysapi.util.JarLocation.*;
import static com.krillsson.sysapi.util.NullSafeOhmMonitor.nullSafe;

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

    public GpuMonitor[] ohmGpu() {
        monitorManager.Update();
        return monitorManager.GpuMonitors();
    }

/*
    @Override
    public Cpu cpu() {
        Cpu cpu = super.cpu();
        cpu.setStatistics(statistics());
        monitorManager.Update();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            cpu.setFanPercent(nullSafe(cpuMonitor.getFanPercent()).getValue());
            cpu.setFanRpm(nullSafe(cpuMonitor.getFanRPM()).getValue());
            cpu.setTemperature(nullSafe(cpuMonitor.getPackageTemperature()).getValue());
            cpu.setVoltage(nullSafe(cpuMonitor.getVoltage()).getValue());
            cpu.getTotalCpuLoad().setTemperature(nullSafe(cpuMonitor.getPackageTemperature()).getValue());
            if (nullSafe(cpuMonitor.getTemperatures()).length >= 1 &&
                    cpuMonitor.getTemperatures().length == cpu.getCpuLoadPerCore().size()) {
                final List<CpuLoad> cpuLoadPerCore = cpu.getCpuLoadPerCore();
                final OHMSensor[] temperatures = cpuMonitor.getTemperatures();
                for (int i = 0; i < cpuLoadPerCore.size(); i++) {
                    cpuLoadPerCore.get(i).setTemperature(temperatures[i].getValue());
                }
            }
        }
        return cpu;
    }

    @Override
    public System systemSummary(int filesystemId, String nicId) {
        System system = super.systemSummary(filesystemId, nicId);
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            system.getTotalCpuLoad().setTemperature(nullSafe(cpuMonitor.getPackageTemperature()).getValue());
        }
        matchDriveProperties(singletonList(system.getMainFileSystem()), driveMonitors);
        setSpeed(system.getMainNetworkInterface());
        return system;
    }

    @Override
    public CpuLoad getCpuTimeByCoreIndex(int id) {
        CpuLoad cpuLoad = super.getCpuTimeByCoreIndex(id);
        monitorManager.Update();
        if (monitorManager.CpuMonitors().length > 0) {
            CpuMonitor cpuMonitor = monitorManager.CpuMonitors()[0];
            if (nullSafe(cpuMonitor.getTemperatures()).length >= 1 &&
                    cpuMonitor.getTemperatures().length - 1 <= id) {
                final OHMSensor[] temperatures = cpuMonitor.getTemperatures();
                cpuLoad.setTemperature(temperatures[id].getValue());
            }
        }
        return cpuLoad;
    }

    @Override
    public List<Drive> drives() {
        List<Drive> drives = super.drives();
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        matchDriveProperties(drives, driveMonitors);
        return drives;
    }

    @Override
    public List<Drive> getFileSystemsWithCategory(FileSystemType fsType) {
        List<Drive> drives = super.getFileSystemsWithCategory(fsType);
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        matchDriveProperties(drives, driveMonitors);
        return drives;
    }

    @Override
    public Drive getFileSystemById(int name) {
        Drive fileSystemById = super.getFileSystemById(name);
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        matchDriveProperties(singletonList(fileSystemById), driveMonitors);
        return fileSystemById;
    }

    @Override
    public List<Gpu> gpus() {
        List<Gpu> gpus = new ArrayList<>();
        monitorManager.Update();
        final GpuMonitor[] gpuMonitors = monitorManager.GpuMonitors();
        if (gpuMonitors != null && gpuMonitors.length > 0) {
            for (GpuMonitor gpuMonitor : gpuMonitors) {
                GpuLoad gpuLoad = new GpuLoad(
                        nullSafe(gpuMonitor.getTemperature()).getValue(),
                        nullSafe(gpuMonitor.getCoreLoad()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue());
                GpuInfo gpuInfo = new GpuInfo(gpuMonitor.getVendor(),
                        gpuMonitor.getName(),
                        nullSafe(gpuMonitor.getCoreClock()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue()
                );
                Gpu gpu = new Gpu(nullSafe(gpuMonitor.getFanRPM()).getValue(),
                        nullSafe(gpuMonitor.getFanPercent()).getValue(),
                        gpuInfo,
                        gpuLoad
                );
                gpus.add(gpu);
            }
        }
        return gpus;
    }

    @Override
    public NetworkInfo networkInfo() {
        monitorManager.Update();
        NetworkInfo info = super.networkInfo();
        for (NetworkInterfaceConfig conf : info.getNetworkInterfaceConfigs()) {
            setSpeed(conf);
        }
        return info;
    }

    @Override
    public NetworkInterfaceConfig getConfigById(String id) {
        monitorManager.Update();
        NetworkInterfaceConfig config = super.getConfigById(id);
        setSpeed(config);
        return config;
    }

    private void setSpeed(NetworkInterfaceConfig config) {
        NetworkMonitor networkMonitor = monitorManager.getNetworkMonitor();
        NicInfo[] nics = networkMonitor.getNics();
        for (NicInfo info : nics) {
            if (info.getPhysicalAddress().equals(config.getHwaddr())) {
                config.setNetworkInterfaceSpeed(new NetworkInterfaceSpeed((long) (info.getInBandwidth().getValue() * 1000), (long) (info.getOutBandwidth().getValue() * 1000)));
            }
        }
    }

    @Override


    private void matchDriveProperties(List<Drive> drives, DriveMonitor[] driveMonitors) {
        if (driveMonitors != null && driveMonitors.length > 0) {
            for (DriveMonitor driveMonitor : driveMonitors) {
                for (Drive drive : drives) {
                    if (driveNamesAreEqual(driveMonitor, drive)) {
                        drive.setHealth(new HWDiskHealth(nullSafe(driveMonitor.getTemperature()).getValue(),
                                nullSafe(driveMonitor.getRemainingLife()).getValue(),
                                Arrays.asList(nullSafe(driveMonitor.getLifecycleData()))
                                        .stream()
                                        .map(l -> new LifecycleData(l.getLabel(), l.getValue()))
                                        .collect(Collectors.toList())));
                        drive.setLoad(new DriveLoad(driveMonitor.getReadRate(), driveMonitor.getWriteRate()));
                        drive.setDeviceName(driveMonitor.getName());
                    }
                }
            }
        }
    }

    private boolean driveNamesAreEqual(DriveMonitor driveMonitor, Drive drive) {
        if (driveMonitor.getLogicalName() != null) {
            String driveMonitorName = driveMonitor
                    .getLogicalName()
                    .toLowerCase()
                    .replace(":", "")
                    .replace("\\", "");
            String driveName = drive
                    .deviceName()
                    .toLowerCase()
                    .replace(":", "")
                    .replace("\\", "");
            return driveMonitorName.equals(driveName);
        } else {
            return false;
        }
    }*/

    public Motherboard motherboard() {
        monitorManager.Update();
        MainboardMonitor mainboardMonitor = monitorManager.getMainboardMonitor();
        if (mainboardMonitor != null) {
            Map<String, Double> boardTemperatures = Arrays.asList(nullSafe(mainboardMonitor.getBoardTemperatures())).stream().collect(Collectors.toMap(
                    OHMSensor::getLabel,
                    OHMSensor::getValue));
            Map<String, Double> boardFanRpms = Arrays.asList(nullSafe(mainboardMonitor.getBoardFanRPM())).stream().collect(Collectors.toMap(
                    OHMSensor::getLabel,
                    OHMSensor::getValue));
            Map<String, Double> boardFanPercents = Arrays.asList(nullSafe(mainboardMonitor.getBoardFanPercent())).stream().collect(Collectors.toMap(
                    OHMSensor::getLabel,
                    OHMSensor::getValue));
            return new Motherboard(mainboardMonitor.getName(), boardTemperatures, boardFanRpms, boardFanPercents);
        }
        return null;
    }

    public List<Gpu> gpus() {
        List<Gpu> gpus = new ArrayList<>();
        monitorManager.Update();
        final GpuMonitor[] gpuMonitors = monitorManager.GpuMonitors();
        if (gpuMonitors != null && gpuMonitors.length > 0) {
            for (GpuMonitor gpuMonitor : gpuMonitors) {
                GpuLoad gpuLoad = new GpuLoad(
                        nullSafe(gpuMonitor.getTemperature()).getValue(),
                        nullSafe(gpuMonitor.getCoreLoad()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue());
                GpuInfo gpuInfo = new GpuInfo(gpuMonitor.getVendor(),
                        gpuMonitor.getName(),
                        nullSafe(gpuMonitor.getCoreClock()).getValue(),
                        nullSafe(gpuMonitor.getMemoryClock()).getValue()
                );
                Gpu gpu = new Gpu(nullSafe(gpuMonitor.getFanRPM()).getValue(),
                        nullSafe(gpuMonitor.getFanPercent()).getValue(),
                        gpuInfo,
                        gpuLoad
                );
                gpus.add(gpu);
            }
        }
        return gpus;
    }

    private boolean initBridge() {
        Bridge.setVerbose(true);
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
    public HWDiskHealth provideDiskHealth(String name, HWDiskStore diskStore) {
        monitorManager.Update();
        DriveMonitor[] driveMonitors = monitorManager.DriveMonitors();
        for (DriveMonitor driveMonitor : driveMonitors) {
            if (driveMonitor.getLogicalName().equals(name)) {
                List<LifecycleData> lifecycleData = new ArrayList<>();
                addIfSafe(lifecycleData, driveMonitor.getRemainingLife());
                if(driveMonitor.getLifecycleData() != null){
                    for (OHMSensor sensor : driveMonitor.getLifecycleData()) {
                        addIfSafe(lifecycleData, sensor);
                    }
                }

                return new HWDiskHealth(nullSafe(driveMonitor.getTemperature()).getValue(), lifecycleData);
            }
        }
        return null;
    }

    private void addIfSafe(List<LifecycleData> lifecycleData, OHMSensor sensor) {
        OHMSensor ohmSensor = nullSafe(sensor);
        if (ohmSensor.getValue() > 0) {
            lifecycleData.add(new LifecycleData(ohmSensor.getLabel(), ohmSensor.getValue()));
        }
    }
}