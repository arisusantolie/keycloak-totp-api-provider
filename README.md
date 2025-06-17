# Keycloak TOTP API Extension (Java Version)

This extension adds RESTful endpoints to Keycloak to support programmatic management of Time-Based One-Time Password (TOTP) credentials. It enables external systems to integrate TOTP setup and verification workflows without relying on the Keycloak UI.

> **Note**: This is a Java-based rewrite of the original [Kotlin implementation](https://github.com/medihause/keycloak-totp-api) by MediHause.

---

## ✨ Features

* Generate TOTP secrets and QR codes (Base32 and base64 image)
* Register TOTP credentials for a user
* Verify user-provided TOTP codes
* API secured with service account bearer token
* Supports credential overwrite logic

---

## ✅ Compatibility

* Tested on **Keycloak 26**

---

## 🔧 Build Instructions

This project uses **Gradle** for building the JAR file.

### 1. Clone the repository

```bash
git clone https://github.com/arisusantolie/keycloak-totp-api-provider.git
cd keycloak-totp-api-provider
```

### 2. Build the JAR

Use the `mvn` to generate JAR with dependencies:

```bash
mvn clean install package -DskipTests
```

The final JAR will be located in `target/`, named similar to:

```
keycloak-totp-api-provider-1.0.jar
```

---

## 📦 Installation

### Option A: Standalone (non-containerized Keycloak)

1. Copy the JAR into the Keycloak `providers` directory:

   ```bash
   cp target/keycloak-totp-api-provider-1.0.jar $KEYCLOAK_HOME/providers/
   ```

2. Rebuild Keycloak:

   ```bash
   $KEYCLOAK_HOME/bin/kc.sh build
   ```

---

### Option B: Docker-based Keycloak

#### Method 1: Mounting JAR at runtime

Update your Docker Compose or `docker run` with:

```yaml
volumes:
  - ./keycloak-totp-api-provider-1.0.jar:/opt/keycloak/providers/keycloak-totp-api-provider-1.0.jar
```

#### Method 2: Custom Keycloak Docker image

Add this to your Dockerfile:

```dockerfile
COPY keycloak-totp-api-provider-1.0.jar /opt/keycloak/providers/
```

Then rebuild your image.

---

## 📡 API Endpoints

All endpoints follow the pattern:

```
{{BASE_URL}}/realms/{{REALM}}/totp-api/{{USER_ID}}/...
```

### 1. 🔐 Generate TOTP Secret

**GET** `/generate`
Generates a TOTP secret and base64-encoded QR code.

**Response**:

```json
{
  "encodedSecret": "OFIWESBQGBLFG432HB5G6TTLIVIEGU2O",
  "qrCode": "iVBORw0KGgoAAAANSUhEUg..."
}
```

---

### 2. 📝 Register TOTP Credential

**POST** `/register`
Registers a TOTP credential for the user.

**Request Body**:

```json
{
  "deviceName": "MyDevice",
  "encodedSecret": "OFIWESBQGBLFG432HB5G6TTLIVIEGU2O",
  "initialCode": "128356",
  "overwrite": true
}
```

**Response**:

```json
{
  "message": "OTP credential registered"
}
```

---

### 3. ✅ Verify TOTP Code

**POST** `/verify`
Validates a user-supplied TOTP code.

**Request Body**:

```json
{
  "deviceName": "MyDevice",
  "code": "128356"
}
```

**Response**:

```json
{
  "message": "OTP code is valid"
}
```

---

## 🔐 Authentication & Permissions

To access the API endpoints, the caller must:

1. Be authenticated using a **Bearer token**.
2. Use a **service account**.
3. Have the `manage-totp` realm-level role assigned.

### How to create a service account in Keycloak

1. Go to the Keycloak Admin Console.
2. Navigate to **Clients** and click **Create**.
3. Set the `Client ID` (e.g., `totp-api-client`) and click **Save**.
4. In the client settings:
    - Set **Client authentication** = ON
    - Set **Service accounts enabled** = ON
    - Save your changes.
5. Go to the **Service Account Roles** tab.
6. Assign the `manage-totp` realm role to the service account user.

### How to get an access token via `curl`

```bash
curl -X POST \
  "https://<KEYCLOAK-HOST>/realms/<REALM>/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=totp-api-client" \
  -d "client_secret=<CLIENT_SECRET>"
```

**Response:**

```json
{
  "access_token": "eyJhbGci...",
  "expires_in": 300,
  ...
}
```

Use the `access_token` in the `Authorization` header for your API calls:

```bash
-H "Authorization: Bearer <access_token>"
```

---

## 📄 License & Attribution

Original Kotlin version created by [MediHause](https://github.com/medihause/keycloak-totp-api).
This version is a Java port for improved compatibility and integration with existing Java-based Keycloak extensions.
