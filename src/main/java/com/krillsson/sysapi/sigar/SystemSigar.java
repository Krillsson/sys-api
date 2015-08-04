package com.krillsson.sysapi.sigar;


import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import com.krillsson.sysapi.representation.system.JvmProperties;
import com.krillsson.sysapi.representation.system.Machine;
import com.krillsson.sysapi.representation.system.OperatingSystem;
import com.krillsson.sysapi.representation.system.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SystemSigar extends SigarWrapper {
    protected SystemSigar(Sigar sigar) {
        super(sigar);
    }

    public Machine machineInfo() {
        double uptime = 0.0;
        org.hyperic.sigar.OperatingSystem os;
        List<UserInfo> users;
        String hostname = "";
        try {
            os = org.hyperic.sigar.OperatingSystem.getInstance();
            uptime = sigar.getUptime().getUptime();
            List<org.hyperic.sigar.Who> who = Arrays.asList(sigar.getWhoList());
            users = who.stream().map(UserInfo::fromSigarBean).collect(Collectors.toList()); //Stream magic
            hostname = sigar.getNetInfo().getHostName();
        } catch (SigarException e) {
            // give up
            return null;
        }

        JvmProperties jvmProperties = getJvmProperties();

        return (new Machine(hostname, users, uptime, OperatingSystem.fromSigarBean(os), jvmProperties));

    }

    private JvmProperties getJvmProperties() {
        Properties p = System.getProperties();
        return new JvmProperties(
                p.getProperty("java.home"),
                p.getProperty("java.class.path"),
                p.getProperty("java.vendor"),
                p.getProperty("java.vendor.url"),
                p.getProperty("java.version"),
                p.getProperty("os.arch"),
                p.getProperty("os.name"),
                p.getProperty("os.version"),
                p.getProperty("user.dir"),
                p.getProperty("user.home"),
                p.getProperty("user.name"));
    }


}