package com.saoodahmad.cacheforge.api;

import com.saoodahmad.cacheforge.api.dtos.CacheEntryDto;
import com.saoodahmad.cacheforge.api.dtos.CacheStateResponse;
import com.saoodahmad.cacheforge.api.dtos.ErrorResponse;
import com.saoodahmad.cacheforge.api.dtos.SetRequest;
import com.saoodahmad.cacheforge.cache.Cache;
import com.saoodahmad.cacheforge.cache.CacheEntry;
import com.saoodahmad.cacheforge.cache.CacheOperationOutput;

import java.util.List;
import java.util.ArrayList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    public final Cache cache = new Cache();

    @PostMapping("/set")
    public ResponseEntity<?> set(@Valid @RequestBody SetRequest req, BindingResult br) {

        if (br.hasErrors()) {

            FieldError fe = br.getFieldErrors().isEmpty() ? null : br.getFieldErrors().get(0);

            String msg;

            if (fe != null) {
                msg = fe.getDefaultMessage();
            }else {
                msg = "validation failed";
            }

            String code = "VALIDATION_FAILED";

             if(fe != null) {
                if ("key".equals(fe.getField()))  {
                    code = "INVALID_KEY";
                }else if("value".equals(fe.getField())) {
                    code = "INVALID_VALUE";
                }
             }

            System.out.println(msg);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));
        }

        if (req.ttl != -1 && req.ttl <= 0) {
            System.out.println("TTL is not correct");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_TTL", "ttl must be -1 or > 0"));
        }

        CacheOperationOutput output =  cache.setKey(req.key.trim(), req.value.trim(), req.ttl);

        System.out.println(output);

        return ResponseEntity.status(HttpStatus.OK).body(output);

    }

    @GetMapping("/get/{key}")
    public ResponseEntity<?> get(@PathVariable("key") String key) {

        if(key == null || key.trim().isEmpty() ) {

            String code = "INVALID_KEY";

            String msg = "Key is required";

            System.out.println(msg);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }

        CacheOperationOutput output = cache.getKey(key.trim());

        System.out.println(output);

        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @DeleteMapping("/del/{key}")
    public ResponseEntity<?> delete(@PathVariable("key") String key) {

        if(key == null || key.trim().isEmpty() ) {

            String code = "INVALID_KEY";

            String msg = "Key is required";

            System.out.println(msg);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(code, msg));

        }


        CacheOperationOutput output =  cache.deleteKey(key.trim());

        System.out.println(output);

        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @GetMapping("/state")
    public ResponseEntity<?> state() {
        CacheStateResponse resp = new CacheStateResponse();
        resp.capacity = cache.cacheCapacity();

        List<CacheEntryDto> entries = new ArrayList<>();

        for (String key : cache.snapshotKeys()) {
            CacheEntry entry = cache.getKeyDirectlyFromStore(key);

            if (entry != null) {
                entries.add(new CacheEntryDto(key, entry));
            }
        }

        resp.keys = entries;
        resp.lru = cache.snapshotLRU();

        System.out.println("State fetched successfully");

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

}
