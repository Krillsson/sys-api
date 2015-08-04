package com.krillsson.sysapi;

import io.dropwizard.Application;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.krillsson.sysapi.auth.SimpleAuthenticator;
import com.krillsson.sysapi.health.SigarHealthCheck;
import com.krillsson.sysapi.resources.*;
import com.krillsson.sysapi.sigar.SigarMetrics;


public class MaintenanceApplication extends Application<MaintenanceConfiguration> {
    public static void main(String[] args) throws Exception {
        new MaintenanceApplication().run(args);
    }


    @Override
    public String getName() {
        return "sysapi-api";
    }

    @Override
    public void initialize(Bootstrap<MaintenanceConfiguration> maintenanceConfigurationBootstrap) {

    }

    @Override
    public void run(MaintenanceConfiguration config, Environment environment) throws Exception {
        System.setProperty("org.hyperic.sigar.path", config.getSigarLocation());
        SigarMetrics sigarMetrics = SigarMetrics.getInstance();

        environment.jersey().register(new BasicAuthProvider<>(new SimpleAuthenticator(config.getUser()), "Maintenance-API"));
        environment.jersey().register(new CpuResource(sigarMetrics.cpu()));
        environment.jersey().register(new FilesystemResource(sigarMetrics.filesystems()));
        environment.jersey().register(new MemoryResource(sigarMetrics.memory()));
        environment.jersey().register(new SystemResource(sigarMetrics.system()));
        environment.jersey().register(new NetworkResource(sigarMetrics.network()));
        environment.jersey().register(new ProcessResource(sigarMetrics.process()));

        environment.healthChecks().register("Sigar", new SigarHealthCheck());
    }
}