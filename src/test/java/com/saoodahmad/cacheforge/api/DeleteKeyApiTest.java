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
public class DeleteKeyApiTest {

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

    private ApiResp deleteKey(String key, int status) throws Exception {
        return callJson(mvc, om,
                delete("/api/cache/del/N1/{key}", key),
                status);
    }

    @Test
    void del_blankKey_should400_withErrorResponse() throws Exception {
        ApiResp r = deleteKey(" ", 400);

        assertError(r, "INVALID_KEY");
    }

    @Test
    void del_missingKey_should200_miss_nullData() throws Exception {
        ApiResp r = deleteKey("NON_EXISTENT_KEY", 200);

        JsonNode op = assertOp(r, "DELETE", false, true);
        assertDataNull(op);
    }

    @Test
    void ttl_expiry_shouldMiss_andCleanupOnDelete() throws Exception {
        setKey("A", "A", 1, 200);
        sleepMs(1100);

        ApiResp r = deleteKey("A", 200);

        JsonNode op = assertOp(r, "DELETE", false, true);
        assertDataNull(op);
    }

    @Test
    void delete_existing_unexpired_key_should200_hit_returnOpOutput_withPreDeleteData() throws Exception {
        setKey("B", "B", -1, 200);

        ApiResp r = deleteKey("B", 200);

        JsonNode op = assertOp(r, "DELETE", true, false);

        assertDataPresent(op);

        assertEquals("B", dataVal(op));
        assertEquals(-1, dataTtl(op));

    }
}
