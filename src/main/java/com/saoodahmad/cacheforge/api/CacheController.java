package com.saoodahmad.cacheforge.api;

import com.saoodahmad.cacheforge.api.dtos.*;
import com.saoodahmad.cacheforge.cache.api.CacheApi;
import com.saoodahmad.cacheforge.cache.api.CacheOperationOutput;
import com.saoodahmad.cacheforge.cache.model.CacheEntry;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.saoodahmad.cacheforge.cache.model.CacheKey;
import com.saoodahmad.cacheforge.cache.time.TimeProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {
    public final CacheApi cacheApi;

    private final TimeProvider time;

    public CacheController(CacheApi cacheApi, TimeProvider time) {
        this.cacheApi = cacheApi;
        this.time = time;
    }

    @PostMapping("/set")
    public ResponseEntity<?> set(@Valid @RequestBody SetRequest req, BindingResult br) {

        if (br.hasErrors()) {

            FieldError fe = br.getFieldErrors().isEmpty() ? null : br.getFieldErrors().get(0);

            String msg;

            if (fe != null) {
                msg = fe.getDefaultMessage();
            } else {
                msg = "validation failed";
            }

            String code = "VALIDATION_FAILED";

            if (fe != null) {
                if ("key".equals(fe.getField())) {
                    code = "INVALID_KEY";
                } else if ("value".equals(fe.getField())) {
                    code = "INVALID_VALUE";
                } else if ("namespace".equals(fe.getField())) {
                    code = "INVALID_NAMESPACE";
                }
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));
        }

        if (req.ttl != -1 && req.ttl <= 0) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_TTL", "ttl must be -1 or > 0"));
        }

        CacheOperationOutput output = cacheApi.setKey(req.namespace.trim(), req.key.trim(), req.value.trim(), req.ttl);

        return ResponseEntity.status(HttpStatus.OK).body(output);

    }

    @GetMapping("/get/{namespace}/{key}")
    public ResponseEntity<?> get(@PathVariable("namespace") String namespace, @PathVariable("key") String key) {
        if (namespace == null || namespace.trim().isEmpty()) {

            String code = "INVALID_NAMESPACE";

            String msg = "Namespace is required";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }

        if (key == null || key.trim().isEmpty()) {

            String code = "INVALID_KEY";

            String msg = "Key is required";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }

        CacheOperationOutput output = cacheApi.getKey(namespace.trim(), key.trim());

        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @DeleteMapping("/del/{namespace}/{key}")
    public ResponseEntity<?> delete(@PathVariable("namespace") String namespace, @PathVariable("key") String key) {
        if (namespace == null || namespace.trim().isEmpty()) {

            String code = "INVALID_NAMESPACE";

            String msg = "Namespace is required";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }

        if (key == null || key.trim().isEmpty()) {

            String code = "INVALID_KEY";

            String msg = "Key is required";

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }

        CacheOperationOutput output = cacheApi.deleteKey(namespace.trim(), key.trim());

        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @GetMapping("/state")
    public ResponseEntity<?> state() {
        CacheStateResponse resp = new CacheStateResponse();
        resp.capacity = cacheApi.cacheCapacity();

        Map<Integer, List<CacheEntryDto>> storeEntries = new HashMap<>();
        Map<Integer, List<LRUEntryDto>> lruEntries = new HashMap<>();

        for (Map.Entry<Integer, List<CacheKey>> entry : cacheApi.snapshotKeys().entrySet()) {
            int StripeId = entry.getKey();

            List<CacheKey> cKeys = entry.getValue();

            List<CacheEntryDto> entries = new ArrayList<>();

            for (CacheKey cKey : cKeys) {

                CacheEntry cEntry = cacheApi.getCacheEntryDirectlyFromStore(cKey.getNamespace(), cKey.getKey());

                if (cEntry != null) {
                    entries.add(new CacheEntryDto(cKey.getNamespace(), cKey.getKey(), cEntry, time.nowNs()));
                }
            }

            storeEntries.put(StripeId, entries);
        }

        for (Map.Entry<Integer, List<CacheKey>> entry : cacheApi.snapshotLRU().entrySet()) {
            int StripeId = entry.getKey();

            List<CacheKey> lKeys = entry.getValue();

            List<LRUEntryDto> entries = new ArrayList<>();

            for (CacheKey lKey : lKeys) {

                CacheEntry cEntry = cacheApi.getCacheEntryDirectlyFromStore(lKey.getNamespace(), lKey.getKey());

                if (cEntry != null) {
                    entries.add(new LRUEntryDto(lKey.getNamespace(), lKey.getKey()));
                }
            }

            lruEntries.put(StripeId, entries);
        }

        resp.keys = storeEntries;
        resp.lru = lruEntries;

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }


    @GetMapping()
    public ResponseEntity<?> home() {
        RootResponse resp = new RootResponse("Hello from cache!");

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}
