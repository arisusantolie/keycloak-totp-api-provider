package com.keycloak.totp.api.provider.api;

import com.keycloak.totp.api.provider.dto.CommonApiResponse;
import com.keycloak.totp.api.provider.dto.GenerateTOTPResponse;
import com.keycloak.totp.api.provider.dto.RegisterTOTPCredentialRequest;
import com.keycloak.totp.api.provider.dto.VerifyTOTPRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.OTPCredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.Base32;
import org.keycloak.models.utils.CredentialValidation;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.utils.CredentialHelper;
import org.keycloak.utils.TotpUtils;

@Slf4j
public class TOTPResourceApi {

    private final KeycloakSession keycloakSession;
    private static final int TOTP_SECRET_LENGTH = 20;

    public TOTPResourceApi(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    private UserModel authenticateSessionAndGetUser(String userId) {
        AppAuthManager.BearerTokenAuthenticator auth = new AppAuthManager.BearerTokenAuthenticator(keycloakSession);
        AppAuthManager.AuthResult result = auth.authenticate();

        if (result == null) {
            throw new NotAuthorizedException("Token not valid");
        } else if (result.getUser().getServiceAccountClientLink() == null) {
            throw new NotAuthorizedException("User is not a service account");
        } else if (result.getToken().getRealmAccess() == null ||
                !result.getToken().getRealmAccess().isUserInRole("manage-totp")) {
            throw new NotAuthorizedException("User is not an admin");
        }

        UserModel user = keycloakSession.users().getUserById(keycloakSession.getContext().getRealm(), userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (user.getServiceAccountClientLink() != null) {
            throw new BadRequestException("Cannot manage service account");
        }

        return user;
    }

    @GET
    @Path("/{userId}/generate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response generateTOTP(@PathParam("userId") String userId) {
        log.info("Generating TOTP for user {}", userId);
        UserModel user = authenticateSessionAndGetUser(userId);
        var realm = keycloakSession.getContext().getRealm();

        String secret = HmacOTP.generateSecret(TOTP_SECRET_LENGTH);
        String qrCode = TotpUtils.qrCode(secret, realm, user);
        String encodedSecret = Base32.encode(secret.getBytes());
        log.info("TOTP Generated for user {}", userId);

        return Response.ok(new GenerateTOTPResponse(encodedSecret, qrCode)).build();
    }

    @POST
    @Path("/{userId}/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyTOTP(VerifyTOTPRequest request, @PathParam("userId") String userId) {
        UserModel user = authenticateSessionAndGetUser(userId);
        log.info("Verifying TOTP for user {}", userId);
        if (!VerifyTOTPRequest.validate(request)) {
            log.warn("TOTP verification failed : Invalid Request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CommonApiResponse("Invalid request")).build();
        }

        var credentialModel = user.credentialManager()
                .getStoredCredentialByNameAndType(request.getDeviceName(), OTPCredentialModel.TYPE);

        if (credentialModel == null) {
            log.warn("TOTP verification failed : TOTP credential not found");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new CommonApiResponse("TOTP credential not found")).build();
        }

        CredentialProvider<?> totpCredentialProvider = keycloakSession.getProvider(CredentialProvider.class, "keycloak-otp");
        OTPCredentialModel totpCredentialModel = OTPCredentialModel.createFromCredentialModel(credentialModel);
        String credentialId = totpCredentialModel.getId();

        boolean isValid = user.credentialManager().isValid(
                new UserCredentialModel(credentialId, totpCredentialProvider.getType(), request.getCode()));

        return isValid
                ? Response.ok(new CommonApiResponse("OTP code is valid")).build()
                : Response.status(Response.Status.UNAUTHORIZED)
                .entity(new CommonApiResponse("Invalid OTP code")).build();
    }

    @POST
    @Path("/{userId}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerTOTP(RegisterTOTPCredentialRequest request, @PathParam("userId") String userId) {
        UserModel user = authenticateSessionAndGetUser(userId);
        log.info("Registering TOTP for user {}", userId);
        if (!RegisterTOTPCredentialRequest.validate(request)) {
            log.warn("TOTP registration failed : Invalid Request");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CommonApiResponse("Invalid request")).build();
        }

        String encodedTOTP = request.getEncodedSecret();
        String secret = new String(Base32.decode(encodedTOTP));

        if (secret.length() != TOTP_SECRET_LENGTH) {
            log.warn("Totp secret does not match TOTP secret length");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CommonApiResponse("Invalid secret")).build();
        }

        var realm = keycloakSession.getContext().getRealm();
        var credentialModel = user.credentialManager()
                .getStoredCredentialByNameAndType(request.getDeviceName(), OTPCredentialModel.TYPE);

        if (credentialModel != null && !request.isOverwrite()) {
            log.warn("TOTP credential already exists for user = {} with deviceName = {}",userId,request.getDeviceName());
            return Response.status(Response.Status.CONFLICT)
                    .entity(new CommonApiResponse("OTP credential already exists")).build();
        }

        OTPCredentialModel totpCredentialModel =
                OTPCredentialModel.createFromPolicy(realm, secret, request.getDeviceName());

        boolean isValid = CredentialValidation.validOTP(
                request.getInitialCode(), totpCredentialModel, 0);

        if (!isValid) {
            log.warn("Invalid initial OTP code");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new CommonApiResponse("Invalid initial OTP code")).build();
        }

        if(request.isOverwrite() && credentialModel != null){
            log.info("Do OTP Overwrite because credential already exists");
            new OTPCredentialProvider(keycloakSession)
                    .deleteCredential(realm, user, credentialModel.getId());
        }

        CredentialHelper.createOTPCredential(
                keycloakSession, realm, user, request.getInitialCode(), totpCredentialModel);

        log.info("OTP credential registered");
        return Response.status(Response.Status.CREATED)
                .entity(new CommonApiResponse("OTP credential registered")).build();
    }
}
