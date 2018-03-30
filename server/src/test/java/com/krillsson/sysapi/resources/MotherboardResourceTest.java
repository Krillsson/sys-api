package com.krillsson.sysapi.resources;

import com.krillsson.sysapi.core.domain.motherboard.Motherboard;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import oshi.hardware.ComputerSystem;
import oshi.hardware.UsbDevice;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class MotherboardResourceTest {
    private static final InfoProvider provider = mock(InfoProvider.class);

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new MotherboardResource(provider))
            .build();

    @Test
    public void getMotherboardHappyPath() throws Exception {
        ComputerSystem computerSystem = mock(ComputerSystem.class);
        when(computerSystem.getManufacturer()).thenReturn("ASUS INK");
        when(computerSystem.getModel()).thenReturn("P55D5");
        when(computerSystem.getSerialNumber()).thenReturn("ASDF1234");
        Motherboard motherboard = mock(Motherboard.class);
        when(motherboard.getComputerSystem()).thenReturn(computerSystem);
        when(motherboard.getUsbDevices()).thenReturn(new UsbDevice[0]);

        when(provider.motherboard()).thenReturn(motherboard);

        final com.krillsson.sysapi.dto.motherboard.Motherboard response = RESOURCES.getJerseyTest().target("/motherboard")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(com.krillsson.sysapi.dto.motherboard.Motherboard.class);

        assertNotNull(response);
        assertEquals("ASUS INK", response.getComputerSystem().getManufacturer());
        assertEquals("P55D5", response.getComputerSystem().getModel());
        assertEquals("ASDF1234", response.getComputerSystem().getSerialNumber());
    }

    @Test
    public void getMotherboardSadPath() throws Exception {
        when(provider.motherboard()).thenThrow(new RuntimeException("What"));
        final Response response = RESOURCES.getJerseyTest().target("/motherboard")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertEquals(response.getStatus(), 500);
    }

    @After
    public void tearDown() throws Exception {
        reset(provider);
    }
}