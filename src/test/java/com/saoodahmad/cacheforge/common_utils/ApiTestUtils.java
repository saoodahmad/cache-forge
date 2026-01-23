package com.saoodahmad.cacheforge.common_utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public final class ApiTestUtils {

    private ApiTestUtils() {}

    // ----------------------------
    // Response wrapper (gives room for extra asserts)
    // ----------------------------
    public static final class ApiResp {
        public final int status;
        public final JsonNode body;
        public final String raw;

        public ApiResp(int status, JsonNode body, String raw) {
            this.status = status;
            this.body = body;
            this.raw = raw;
        }

        // Fluent helpers (optional)
        public ApiResp assertStatus(int expected) {
            assertEquals(expected, this.status, "HTTP status mismatch. Body: " + raw);
            return this;
        }

        public ApiResp assertHasField(String field) {
            assertTrue(body.has(field), "Missing field: " + field + ". Body: " + raw);
            return this;
        }
    }

    // ----------------------------
    // Common caller (no duplicated MockMvc boilerplate)
    // ----------------------------
    public static ApiResp callJson(MockMvc mvc, ObjectMapper om,
                                   MockHttpServletRequestBuilder req,
                                   int expectedStatus) throws Exception {

        var result = mvc.perform(req.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        String raw = result.getResponse().getContentAsString();
        JsonNode body = om.readTree(raw);

        return new ApiResp(status, body, raw);
    }

    // ----------------------------
    // Common assertions (base contract)
    // ----------------------------
    public static JsonNode assertError(ApiResp r, String errorCode) {
        assertNotNull(r.body, "Body must not be null");
        assertTrue(r.body.hasNonNull("errorCode"), "errorCode missing. Body: " + r.raw);
        assertTrue(r.body.hasNonNull("message"), "message missing. Body: " + r.raw);
        assertEquals(errorCode, r.body.get("errorCode").asText(), "errorCode mismatch");
        assertFalse(r.body.get("message").asText().isBlank(), "message should not be blank");
        return r.body;
    }

    public static JsonNode assertOp(ApiResp r, String opType, boolean hit, boolean miss) {
        assertNotNull(r.body, "Body must not be null");
        assertTrue(r.body.hasNonNull("opType"), "opType missing. Body: " + r.raw);
        assertEquals(opType, r.body.get("opType").asText(), "opType mismatch");
        assertEquals(hit, r.body.get("hit").asBoolean(), "hit mismatch");
        assertEquals(miss, r.body.get("miss").asBoolean(), "miss mismatch");
        assertTrue(r.body.has("data"), "data missing. Body: " + r.raw);
        return r.body;
    }

    public static JsonNode assertDataNull(JsonNode opBody) {
        assertTrue(opBody.get("data").isNull(), "Expected data=null");
        return opBody;
    }

    public static JsonNode assertDataPresent(JsonNode opBody) {
        assertFalse(opBody.get("data").isNull(), "Expected data!=null");
        return opBody;
    }

    // Convenience: pull fields safely

    public static String dataVal(JsonNode opBody) {
        return opBody.get("data").get("val").asText();
    }

    public static long dataTtl(JsonNode opBody) {
        return opBody.get("data").get("ttlInSecs").asLong();
    }
}
