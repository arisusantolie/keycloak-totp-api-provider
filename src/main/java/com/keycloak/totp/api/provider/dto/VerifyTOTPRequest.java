package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
@Data
@NoArgsConstructor
public class VerifyTOTPRequest {

    @JsonProperty("deviceName")
    private String deviceName;

    @JsonProperty("code")
    private String code;

    public static boolean validate(VerifyTOTPRequest request) {
        return request.deviceName != null && !request.deviceName.isEmpty()
                && request.code != null && !request.code.isEmpty();
    }
}
