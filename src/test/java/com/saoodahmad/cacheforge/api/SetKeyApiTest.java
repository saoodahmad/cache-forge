package com.saoodahmad.cacheforge.api;

import com.fasterxml.jackson.databind.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.saoodahmad.cacheforge.common_utils.ApiTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
public class SetKeyApiTest {

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

    @Test
    void set_blankKey_should400_withErrorResponse() throws Exception {
        ApiResp r = setKey("   ", "V", -1, 400);

        assertError(r, "INVALID_KEY");
    }

    @Test
    void set_invalidTtl_should400_withErrorResponse() throws Exception {
        ApiResp r = setKey("A", "A", 0, 400);
        assertError(r, "INVALID_TTL");
    }

    @Test
    void set_blankVal_should400_withErrorResponse() throws Exception {
        ApiResp r = setKey("A", " ", 0, 400);
        assertError(r, "INVALID_VALUE");
    }

    @Test
    void overwrite_shouldHit_whenNotExpired_andRefreshTtl() throws Exception {
        setKey("A", "A", 2, 200);

        ApiResp s2 = setKey("A", "A2", 3, 200);
        JsonNode op = assertOp(s2, "SET", true, false);
        assertDataPresent(op);

        assertEquals("A2", dataVal(op));
        assertEquals(3, dataTtl(op));
    }

    @Test
    void set_newKey_should200_andReturnOpOutput_withLatestData() throws Exception {
        ApiResp r = setKey("B", "B", -1, 200);

        JsonNode op = assertOp(r, "SET", false, true);
        assertDataPresent(op);

        assertEquals("B", dataVal(op));
        assertEquals(-1, dataTtl(op));
    }

}
