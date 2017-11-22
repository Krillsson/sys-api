package com.krillsson.sysapi.resources;

import com.krillsson.sysapi.core.InfoProvider;
import com.krillsson.sysapi.core.domain.metadata.Meta;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public class MetaInfoResourceTest {
    private static final InfoProvider provider = mock(InfoProvider.class);

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new MetaInfoResource("1.0", new String[]{"test0", "test1"}, 100))
            .build();

    @Test
    public void getRootHappyPath() throws Exception {
        final com.krillsson.sysapi.dto.metadata.Meta response = RESOURCES.getJerseyTest().target("/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(com.krillsson.sysapi.dto.metadata.Meta.class);

        assertNotNull(response);
        assertEquals(response.getVersion(), "1.0");
        assertEquals(response.getEndpoints()[0], "test0");
        assertEquals(response.getEndpoints()[1], "test1");
    }

    @Test
    public void getVersionHappyPath() throws Exception {
        final String response = RESOURCES.getJerseyTest().target("/version")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertNotNull(response);
        assertEquals(response, "1.0");
    }

    @After
    public void tearDown() throws Exception {
        reset(provider);
    }
}