package se.krillsson.maintenance.resources;


import io.dropwizard.auth.Auth;
import se.krillsson.maintenance.representation.config.UserConfiguration;
import se.krillsson.maintenance.representation.system.Machine;
import se.krillsson.maintenance.sigar.SystemMetrics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("system")
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource extends Resource {
    private SystemMetrics systemMetrics;

    public SystemResource(SystemMetrics systemMetrics) {
        this.systemMetrics = systemMetrics;
    }

    @GET
    @Override
    public Machine getRoot(@Auth UserConfiguration user) {
        return systemMetrics.machineInfo();
    }
}