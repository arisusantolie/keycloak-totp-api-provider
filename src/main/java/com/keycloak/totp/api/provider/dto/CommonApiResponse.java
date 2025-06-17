package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
@Data
@NoArgsConstructor
public class CommonApiResponse {
    @JsonProperty("message")
    private String message;

    public CommonApiResponse(String message) {
        this.message = message;
    }
}
