package com.krillsson.sysapi.oshi;

import com.krillsson.sysapi.UserConfiguration;
import com.krillsson.sysapi.auth.BasicAuthorizer;
import io.dropwizard.auth.Auth;
import oshi.hardware.NetworkIF;
import oshi.hardware.Sensors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("networkinterfaces")
@Produces(MediaType.APPLICATION_JSON)
public class NetworkInterfacesResource {
    private final NetworkIF[] networkIFs;

    public NetworkInterfacesResource(NetworkIF[] networkIFs) {
        this.networkIFs = networkIFs;
    }

    @GET
    @RolesAllowed(BasicAuthorizer.AUTHENTICATED_ROLE)
    public NetworkIF[] getRoot(@Auth UserConfiguration user) {
        return networkIFs;
    }
}
