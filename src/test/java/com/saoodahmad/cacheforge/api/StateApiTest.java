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
public class StateApiTest {

    @Autowired
    MockMvc mvc;
    private final ObjectMapper om = new ObjectMapper();

    private ApiResp setKey(String key, String value, long ttl, int status) throws Exception {
        String body = om.createObjectNode()
                .put("key", key)
                .put("value", value)
                .put("ttl", ttl)
                .toString();

        return callJson(mvc, om,
                post("/api/cache/set").content(body),
                status
        );
    }

    private ApiResp getKey(String key, int status) throws Exception {
        return callJson(mvc, om,
                get("/api/cache/get/{key}", key),
                status
        );
    }

    private ApiResp state(int status) throws Exception {
        return callJson(mvc, om,
                get("/api/cache/state"),
                status
        );
    }

    @Test
    void state_shouldNotMutateLru_order() throws Exception {
        setKey("A", "A", -1, 200);
        setKey("B", "B", -1, 200);
        setKey("C", "C", -1, 200);

        assertOp(getKey("B", 200), "GET", true, false);

        String before = state(200).body.get("lru").toString();

        for (int i = 0; i < 5; i++) state(200);

        String after = state(200).body.get("lru").toString();
        assertEquals(before, after);
    }
}
