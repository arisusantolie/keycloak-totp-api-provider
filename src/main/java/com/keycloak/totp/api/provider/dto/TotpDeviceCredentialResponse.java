package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonSerialize
@Data
@NoArgsConstructor
public class TotpDeviceCredentialResponse {
    @JsonProperty("deviceName")
    private List<String> deviceName;

    public TotpDeviceCredentialResponse(List<String> deviceName) {
        this.deviceName = deviceName;
    }
}
