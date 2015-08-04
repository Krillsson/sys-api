package com.krillsson.sysapi.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.krillsson.sysapi.sigar.MemoryMetrics;

import static org.mockito.Mockito.mock;

public class MemoryResourceTest {

    private static final MemoryMetrics memoryMock = mock(MemoryMetrics.class);

    @Rule
    public ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MemoryResource(memoryMock))
            .build();

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testAll() throws Exception {

    }
}