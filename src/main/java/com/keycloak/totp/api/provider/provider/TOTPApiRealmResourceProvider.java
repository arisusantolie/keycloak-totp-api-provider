package com.keycloak.totp.api.provider.provider;

import com.keycloak.totp.api.provider.api.TOTPResourceApi;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class TOTPApiRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession keycloakSession;

    public TOTPApiRealmResourceProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public Object getResource() {
        return new TOTPResourceApi(keycloakSession);
    }

    @Override
    public void close() {
        // no-op
    }
}