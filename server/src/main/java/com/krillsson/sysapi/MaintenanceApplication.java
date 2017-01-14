package com.krillsson.sysapi;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.krillsson.sysapi.auth.BasicAuthenticator;
import com.krillsson.sysapi.auth.BasicAuthorizer;
import com.krillsson.sysapi.extension.InfoProvider;
import com.krillsson.sysapi.extension.InfoProviderFactory;
import com.krillsson.sysapi.ohm.OhmDisplayResource;
import com.krillsson.sysapi.extension.windows.WindowsInfoProvider;
import com.krillsson.sysapi.oshi.*;
import com.krillsson.sysapi.resources.MetaInfoResource;
import com.krillsson.sysapi.util.OperatingSystem;
import com.krillsson.sysapi.util.TemperatureUtils;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import oshi.json.SystemInfo;
import oshi.json.hardware.HardwareAbstractionLayer;
import oshi.json.hardware.Sensors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.EnumSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


public class MaintenanceApplication extends Application<MaintenanceConfiguration> {
    private Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MaintenanceApplication.class.getSimpleName());
    private Environment environment;

    public static void main(String[] args) throws Exception {
        new MaintenanceApplication().run(args);
    }

    @Override
    public String getName() {
        return "System-Api";
    }

    @Override
    public void initialize(Bootstrap<MaintenanceConfiguration> maintenanceConfigurationBootstrap) {
        maintenanceConfigurationBootstrap.getObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void run(MaintenanceConfiguration config, Environment environment) throws Exception {
        this.environment = environment;

        if (config.forwardHttps()) {
            addHttpsForward(environment.getApplicationContext());
        }
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        final BasicCredentialAuthFilter<UserConfiguration> userBasicCredentialAuthFilter =
                new BasicCredentialAuthFilter.Builder<UserConfiguration>()
                        .setAuthenticator(new BasicAuthenticator(config.getUser()))
                        .setRealm("System-Api")
                        .setAuthorizer(new BasicAuthorizer(config.getUser()))
                        .buildAuthFilter();

        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        oshi.json.software.os.OperatingSystem os = si.getOperatingSystem();
        System.out.println(os);

        environment.jersey().register(new AuthDynamicFeature(userBasicCredentialAuthFilter));
        environment.jersey().register(new AuthValueFactoryProvider.Binder(UserConfiguration.class));
        environment.jersey().register(new MetaInfoResource(getVersionFromManifest()));

        InfoProvider provider = InfoProviderFactory.provide(OperatingSystem.getCurrentOperatingSystem());
        //oshi
        TemperatureUtils temperatureUtils = new TemperatureUtils(OperatingSystem.getCurrentOperatingSystem());
        Sensors sensors = hal.getSensors();
        environment.jersey().register(new SystemResource(provider, temperatureUtils, sensors, os, hal.getComputerSystem(), hal.getProcessor(), hal.getMemory(), hal.getPowerSources(), sensors));
        environment.jersey().register(new DiskStoresResource(hal.getDiskStores(), os.getFileSystem(), provider));
        environment.jersey().register(new FileSystemResource(os.getFileSystem()));
        environment.jersey().register(new MemoryResource(hal.getMemory()));
        environment.jersey().register(new NetworkInterfacesResource(hal.getNetworkIFs()));
        environment.jersey().register(new PowerSourcesResource(hal.getPowerSources()));
        environment.jersey().register(new ProcessesResource(os));
        environment.jersey().register(new CpuResource(sensors, hal.getProcessor(), provider));
        environment.jersey().register(new SensorsResource(sensors));
        environment.jersey().register(new UsbDevicesResource(hal.getUsbDevices(true)));

        if (OperatingSystem.isWindows()) {
            WindowsInfoProvider windowsInfoProvider = new WindowsInfoProvider();
            environment.jersey().register(new OhmDisplayResource(hal.getDisplays(), windowsInfoProvider));

        } else {
            environment.jersey().register(new DisplaysResource(hal.getDisplays()));
        }
    }


    private String libLocation(MaintenanceConfiguration config) {
        if (config.getSigarLocation() != null) {
            return config.getSigarLocation();
        }

        File thisJar = new File(MaintenanceApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile());

        String separator = System.getProperty("file.separator");
        String pathToJar = thisJar.getParent();
        try {
            pathToJar = URLDecoder.decode(pathToJar, "UTF-8");
            return pathToJar + separator + "lib";
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unable to decode the path to UTF-8");
            return "";
        }
    }

    private void addHttpsForward(ServletContextHandler handler) {
        handler.addFilter(new FilterHolder(new Filter() {

            public void init(FilterConfig filterConfig) throws ServletException {
            }

            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                StringBuffer uri = ((HttpServletRequest) request).getRequestURL();
                if (uri.toString().startsWith("http://")) {
                    String location = "https://" + uri.substring("http://".length());
                    ((HttpServletResponse) response).sendRedirect(location);
                } else {
                    chain.doFilter(request, response);
                }
            }

            public void destroy() {
            }
        }), "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private String getVersionFromManifest() throws IOException {
        Class clazz = MaintenanceApplication.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            LOGGER.error("Unable to determine version");
            return "";
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();
        return attr.getValue("Version");
    }

    public Environment getEnvironment() {
        return environment;
    }
}
