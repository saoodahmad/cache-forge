package com.saoodahmad.cacheforge.api.dtos;

import jakarta.validation.constraints.NotBlank;
public class SetRequest {
    @NotBlank(message = "Key cannot be blank")
    public String key;

    @NotBlank(message = "Value cannot be blank")
    public String value;

    // ttl validated in controller
    public long ttl; // seconds, -1 = no expiry

}
