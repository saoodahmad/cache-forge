package com.saoodahmad.cacheforge.api;

import com.fasterxml.jackson.databind.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.saoodahmad.cacheforge.common_utils.ApiTestUtils.*;
import static com.saoodahmad.cacheforge.common_utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
public class GetKeyApiTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper om = new ObjectMapper();

    private ApiResp setKey(String key, String value, long ttl, int status) throws Exception {
        String body = om.createObjectNode()
                .put("namespace", "N1")
                .put("key", key)
                .put("value", value)
                .put("ttl", ttl)
                .toString();

        return callJson(mvc, om,
                post("/api/cache/set").content(body),
                status);
    }

    private ApiResp getKey(String key, int status) throws Exception {
        return callJson(mvc, om,
                get("/api/cache/get/N1/{key}", key),
                status);
    }

    private ApiResp state(int status) throws Exception {
        return callJson(mvc, om,
                get("/api/cache/state"),
                status);
    }

    @Test
    void get_blankKey_should400_withErrorResponse() throws Exception {
        ApiResp r = getKey(" ", 400);

        assertError(r, "INVALID_KEY");
    }

    @Test
    void get_missingKey_should200_miss_nullData() throws Exception {
        ApiResp r = getKey("NON_EXISTENT_KEY", 200);

        JsonNode op = assertOp(r, "GET", false, true);
        assertDataNull(op);
    }

    @Test
    void ttl_expiry_shouldMiss_andCleanupOnGet() throws Exception {
        setKey("A", "A", 1, 200);
        sleepMs(1100);

        ApiResp g = getKey("A", 200);
        JsonNode op = assertOp(g, "GET", false, true);
        assertDataNull(op);

        ApiResp st = state(200);
        String keysRaw = st.body.get("keys").toString();
        assertFalse(keysRaw.contains("\"key\":\"T\""));
        assertFalse(st.body.get("lru").toString().contains("T"));
    }

    @Test
    void get_existing_unexpired_key_should200_hit_returnOpOutput_withLatestData() throws Exception {
        setKey("B", "B", 100, 200);

        ApiResp g = getKey("B", 200);
        JsonNode op = assertOp(g, "GET", true, false);

        assertEquals("B", dataVal(op));
        assertEquals(100, dataTtl(op));
    }
}
