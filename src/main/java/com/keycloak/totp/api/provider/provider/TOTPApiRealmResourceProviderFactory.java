package com.keycloak.totp.api.provider.provider;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class TOTPApiRealmResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String PROVIDER_ID = "totp-api";

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
            return new TOTPApiRealmResourceProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
