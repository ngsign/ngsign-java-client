# NGSign Java Client

A small, well-documented **Java SDK** for the [NGSign](https://www.ng-sign.com) electronic
signature API. It covers the two operations most integrations need:

1. **Trigger** the electronic signature of one or more PDF documents, by one or more signers.
2. **Retrieve** the status of the resulting transaction (and download the signed PDFs).

- Requires **Java 17+**
- One runtime dependency: Jackson (JSON). HTTP uses the JDK's built-in `java.net.http` client.
- Immutable, thread-safe client — create one and reuse it.

## Coordinates

```xml
<dependency>
    <groupId>com.ngsign</groupId>
    <artifactId>ngsign-java-client</artifactId>
    <version>1.1.0</version>
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
| `SignatureRequest` (+ `Builder`) | Describes the document(s), signer(s) and options to sign |
| `Document` | A PDF to sign: file name + bytes, with an optional fixed position |
| `SignatureResult` | `transactionId` + `documentIdentifier(s)` returned after launch |
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

## Multiple documents & signers

A request can carry several documents and several signers — every signer is invited to sign
every document. Add them with the repeatable `.document(...)` / `.signer(...)` (or the plural
`.documents(list)` / `.signers(list)`):

```java
SignatureResult result = client.requestSignature(SignatureRequest.builder()
        .document(Document.of("contract", contractPdf))
        .document(Document.of("annex", annexPdf, new SignaturePosition(1, 81, 44.28)))
        .signer(new Signer("Jane", "Doe", "jane@example.com", "+21600000000"))
        .signer(new Signer("John", "Roe", "john@example.com", null))
        .signatureType(SignatureType.CERTIFIED_TIMESTAMP)   // control the signature type
        .mode(SignatureMode.BY_MAIL)
        .build());

for (String documentId : result.documentIdentifiers()) {
    // poll the transaction, then download each signed document
}
```

- `Document.of(name, bytes)` — interactive placement; `Document.of(name, bytes, position)`
  pins the signature on that document.
- The signature type, mode and OTP apply to all signers.
- The single-document shortcuts (`.fileName(...)` + `.pdfContent(...)`) and
  `result.documentIdentifier()` still work for the common one-document case.

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

## License

GNU General Public License v3.0 (see [LICENSE](LICENSE)).

Copyright © 2026 NG Technologies. This program is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public
License version 3 as published by the Free Software Foundation.
