package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
@Data
@NoArgsConstructor
public class GenerateTOTPResponse {
    @JsonProperty("encodedSecret")
    private String encodedSecret;

    @JsonProperty("qrCode")
    private String qrCode;

    public GenerateTOTPResponse(String encodedSecret, String qrCode) {
        this.encodedSecret = encodedSecret;
        this.qrCode = qrCode;
    }
}
