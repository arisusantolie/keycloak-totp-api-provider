package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
@Data
@NoArgsConstructor
public class RemoveTOTPCredentialRequest {

    @JsonProperty("deviceName")
    private String deviceName;
}
