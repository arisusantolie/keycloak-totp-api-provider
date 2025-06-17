package com.keycloak.totp.api.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonSerialize
@Data
@NoArgsConstructor
public class RegisterTOTPCredentialRequest {

    @JsonProperty("deviceName")
    private String deviceName;

    @JsonProperty("encodedSecret")
    private String encodedSecret;

    @JsonProperty("initialCode")
    private String initialCode;

    @JsonProperty("overwrite")
    private boolean overwrite = false;

    // Equivalent to the companion object validate() function
    public static boolean validate(RegisterTOTPCredentialRequest request) {
        return request.deviceName != null && !request.deviceName.isEmpty()
                && request.encodedSecret != null && !request.encodedSecret.isEmpty()
                && request.initialCode != null && !request.initialCode.isEmpty();
    }
}
