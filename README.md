# NGSign Java Client

A small, well-documented **Java SDK** for the [NGSign](https://www.ng-sign.com) electronic
signature API. It covers the two operations most integrations need:

1. **Trigger** the electronic signature of a PDF document.
2. **Retrieve** the status of the resulting transaction (and download the signed PDF).

- Requires **Java 17+**
- One runtime dependency: Jackson (JSON). HTTP uses the JDK's built-in `java.net.http` client.
- Immutable, thread-safe client — create one and reuse it.

## Coordinates

```xml
<dependency>
    <groupId>com.ngsign</groupId>
    <artifactId>ngsign-java-client</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

```java
import com.ngsign.client.NGSignClient;
import com.ngsign.client.model.SignatureRequest;
import com.ngsign.client.model.SignatureResult;
import com.ngsign.client.model.Signer;
import com.ngsign.client.model.Transaction;

// 1. Build the client once (reuse it).
NGSignClient client = NGSignClient.builder()
        .baseUrl("https://sandbox.ng-sign.com")
        .apiToken(System.getenv("NGSIGN_TOKEN"))
        .build();

// 2. Send a PDF for signature (upload + launch in one call).
SignatureResult result = client.requestSignature(SignatureRequest.builder()
        .fileName("contract")
        .pdfContent(pdfBytes)                                  // byte[]
        .signer(new Signer("Jane", "Doe", "jane@example.com", "+21600000000"))
        .build());

String transactionId = result.transactionId();
String documentId = result.documentIdentifier();

// 3. Later, poll the transaction status.
Transaction tx = client.getTransaction(transactionId);
if (tx.isSigned()) {
    byte[] signedPdf = client.downloadSignedDocument(transactionId, documentId);
    // store signedPdf...
}
```

## Public API

| Type | Purpose |
|---|---|
| `NGSignClient` | Entry point. `builder()`, `requestSignature`, `getTransaction`, `downloadSignedDocument` |
| `NGSignClient.Builder` | Configure base URL, API token, request timeout, custom `HttpClient` |
| `SignatureRequest` (+ `Builder`) | Describes the document, signer and options to sign |
| `SignatureResult` | `transactionId` + `documentIdentifier` returned after launch |
| `Transaction` | A status snapshot (`status()`, `isSigned()`) |
| `NGSignClientException` | Unchecked exception thrown on any API failure |

### Options (enums, with the exact API values)

- `SignatureMode` — `BY_MAIL`, `FACE_TO_FACE`, `AUTOMATIC`, `BY_LINK`
- `OtpChoice` — `EMAIL`, `OTP`, `NONE`
- `SignatureType` — `SIGNATURE_WITH_SSCD`, `CERTIFIED_TIMESTAMP`, `CERTIFIED_TIMESTAMP_WACOM`,
  `NGCERT`, `DIGI_GO`, `LATER`, `MOBILE_ID`, `TRUSTED_X`
- `TransactionStatus` — `CREATED`, `CONFIGURED`, `SIGNATURE_LAUNCHED`, `SIGNED`, `CANCELLED`,
  `REFUSED`, `EXPIRED` (plus `UNKNOWN` for forward-compatibility)

Defaults applied by `SignatureRequest.builder()`: `BY_MAIL`, no OTP, `CERTIFIED_TIMESTAMP`,
and the signer places the signature interactively. Set a fixed position with
`.position(new SignaturePosition(page, xAxis, yAxis))`.

### Signature position

By default the signer places the signature on the NGSign signing page. To force a fixed
position instead:

```java
SignatureRequest.builder()
        .fileName("contract")
        .pdfContent(pdfBytes)
        .signer(signer)
        .position(new SignaturePosition(1, 81, 44.28))   // page, xAxis, yAxis
        .build();
```

## Error handling

Any transport error, non-2xx HTTP response, or unexpected payload raises an unchecked
`NGSignClientException`. Configuration mistakes (missing token, missing request fields)
raise `NullPointerException` at build time.

## Build

```bash
mvn clean verify
```

This compiles against Java 17, runs the unit tests, and enforces the **NG Technologies
Checkstyle** rules (`config/checkstyle/checkstyle.xml`). It also attaches the `-sources`
and `-javadoc` jars.

## Requirements

- JDK 17 or newer
- Maven 3.8+

---

© 2026 NG Technologies.
