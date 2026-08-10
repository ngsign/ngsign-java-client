package com.ngsign.client;

import com.ngsign.client.model.Document;
import com.ngsign.client.model.SignaturePosition;
import com.ngsign.client.model.SignatureRequest;
import com.ngsign.client.model.SignatureResult;
import com.ngsign.client.model.Signer;
import com.ngsign.client.model.Transaction;
import com.ngsign.client.model.TransactionStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A small, dependency-light client for the NGSign electronic signature API.
 *
 * <p>It covers the two operations most integrations need: sending a PDF document for
 * signature and checking the status of the resulting transaction. Instances are
 * immutable and thread-safe; create one and reuse it.</p>
 *
 * <p>Every remote call throws {@link NGSignClientException} on failure (transport error,
 * non-2xx response or unexpected payload).</p>
 *
 * <pre>{@code
 * NGSignClient client = NGSignClient.builder()
 *         .baseUrl("https://sandbox.ng-sign.com")
 *         .apiToken(System.getenv("NGSIGN_TOKEN"))
 *         .build();
 *
 * SignatureResult result = client.requestSignature(SignatureRequest.builder()
 *         .fileName("contract")
 *         .pdfContent(pdfBytes)
 *         .signer(new Signer("Jane", "Doe", "jane@example.com", null))
 *         .build());
 *
 * Transaction tx = client.getTransaction(result.transactionId());
 * if (tx.isSigned()) {
 *     byte[] signed = client.downloadSignedDocument(
 *             result.transactionId(), result.documentIdentifier());
 * }
 * }</pre>
 */
public final class NGSignClient {

	private static final String UPLOAD_PATH = "/server/protected/transaction/pdfs";
	private static final int HTTP_OK = 200;
	private static final int HTTP_MULTIPLE_CHOICES = 300;

	private final String baseUrl;
	private final String apiToken;
	private final Duration requestTimeout;
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	private NGSignClient(final Builder builder) {
		this.baseUrl = stripTrailingSlash(builder.baseUrl);
		this.apiToken = builder.apiToken;
		this.requestTimeout = builder.requestTimeout;
		this.objectMapper = new ObjectMapper();
		if (builder.httpClient != null) {
			this.httpClient = builder.httpClient;
		} else {
			this.httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.build();
		}
	}

	/**
	 * Creates a new client builder.
	 *
	 * @return a fresh {@link Builder}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Uploads the request's document(s) and launches their signature in a single call.
	 *
	 * <p>Every {@linkplain SignatureRequest#signers() signer} is invited to sign every
	 * {@linkplain SignatureRequest#documents() document}.</p>
	 *
	 * @param request the signature request
	 * @return the transaction and document identifiers to poll and download later
	 */
	public SignatureResult requestSignature(final SignatureRequest request) {
		Objects.requireNonNull(request, "request is required");
		final SignatureResult uploaded = uploadDocuments(request.documents());
		launchSignature(uploaded, request);
		return uploaded;
	}

	/**
	 * Fetches the current state of a transaction.
	 *
	 * @param transactionId the transaction id returned by {@link #requestSignature}
	 * @return a snapshot of the transaction
	 */
	public Transaction getTransaction(final String transactionId) {
		Objects.requireNonNull(transactionId, "transactionId is required");
		final HttpResponse<String> response = get(transactionPath(transactionId));
		final JsonNode object = readTree(response.body()).path("object");
		final String raw = object.path("status").asText(null);
		final String id = object.path("uuid").asText(transactionId);
		return new Transaction(id, TransactionStatus.fromApiValue(raw), raw);
	}

	/**
	 * Downloads the (signed) PDF of a document within a transaction.
	 *
	 * @param transactionId      the transaction id
	 * @param documentIdentifier the document identifier returned at upload time
	 * @return the raw PDF bytes
	 */
	public byte[] downloadSignedDocument(final String transactionId,
			final String documentIdentifier) {
		Objects.requireNonNull(transactionId, "transactionId is required");
		Objects.requireNonNull(documentIdentifier, "documentIdentifier is required");
		final String path = documentPath(transactionId, documentIdentifier);
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(requestTimeout)
				.header("Authorization", "Bearer " + apiToken)
				.GET()
				.build();
		return sendForBytes(request).body();
	}

	private SignatureResult uploadDocuments(final List<Document> docs) {
		final ArrayNode payload = objectMapper.createArrayNode();
		for (final Document doc : docs) {
			final ObjectNode file = payload.addObject();
			file.put("fileName", doc.fileName());
			file.put("fileExtension", "pdf");
			file.put("fileBase64", Base64.getEncoder().encodeToString(doc.content()));
		}
		final HttpResponse<String> response = post(UPLOAD_PATH, payload);
		final JsonNode object = readTree(response.body()).path("object");
		final String transactionId = object.path("uuid").asText(null);
		final List<String> identifiers = new ArrayList<>();
		for (final JsonNode pdf : object.path("pdfs")) {
			final String id = pdf.path("identifier").asText(null);
			if (id != null) {
				identifiers.add(id);
			}
		}
		if (transactionId == null || identifiers.size() != docs.size()) {
			throw new NGSignClientException(
					"Unexpected NGSign upload response: " + response.body());
		}
		return new SignatureResult(transactionId, identifiers);
	}

	private void launchSignature(final SignatureResult uploaded,
			final SignatureRequest request) {
		final ObjectNode root = objectMapper.createObjectNode();
		final ArrayNode sigConf = root.putArray("sigConf");
		final List<Document> docs = request.documents();
		final List<String> identifiers = uploaded.documentIdentifiers();
		for (final Signer signer : request.signers()) {
			final ObjectNode entry = sigConf.addObject();
			final ObjectNode signerNode = entry.putObject("signer");
			signerNode.put("firstName", signer.firstName());
			signerNode.put("lastName", signer.lastName());
			signerNode.put("email", signer.email());
			final String phone = signer.phoneNumber();
			signerNode.put("phoneNumber", phone == null ? "" : phone);
			entry.put("sigType", request.signatureType().name());
			entry.put("choosePosition", request.chooseSignaturePosition());
			final ArrayNode docsConfigs = entry.putArray("docsConfigs");
			for (int i = 0; i < docs.size(); i++) {
				final Document document = docs.get(i);
				final ObjectNode doc = docsConfigs.addObject();
				doc.put("documentName", document.fileName());
				doc.put("documentExtension", "pdf");
				doc.put("identifier", identifiers.get(i));
				addPosition(doc, request, document);
			}
			entry.put("mode", request.mode().name());
			entry.put("otp", request.otp().name());
		}
		post(launchPath(uploaded.transactionId()), root);
	}

	private static void addPosition(final ObjectNode doc, final SignatureRequest request,
			final Document document) {
		if (request.chooseSignaturePosition()) {
			return;
		}
		final SignaturePosition pos = document.position() != null
				? document.position() : request.position();
		if (pos != null) {
			doc.put("page", pos.page());
			doc.put("xAxis", pos.xAxis());
			doc.put("yAxis", pos.yAxis());
		}
	}

	private HttpResponse<String> post(final String path, final JsonNode body) {
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(requestTimeout)
				.header("Authorization", "Bearer " + apiToken)
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(writeJson(body)))
				.build();
		return sendForString(request);
	}

	private HttpResponse<String> get(final String path) {
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(requestTimeout)
				.header("Authorization", "Bearer " + apiToken)
				.GET()
				.build();
		return sendForString(request);
	}

	private HttpResponse<String> sendForString(final HttpRequest request) {
		try {
			final HttpResponse<String> response = httpClient.send(
					request, BodyHandlers.ofString());
			ensureSuccess(response.statusCode(), response.body());
			return response;
		} catch (final IOException e) {
			throw new NGSignClientException("NGSign API call failed", e);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NGSignClientException("NGSign API call was interrupted", e);
		}
	}

	private HttpResponse<byte[]> sendForBytes(final HttpRequest request) {
		try {
			final HttpResponse<byte[]> response = httpClient.send(
					request, BodyHandlers.ofByteArray());
			ensureSuccess(response.statusCode(), "<binary body>");
			return response;
		} catch (final IOException e) {
			throw new NGSignClientException("NGSign API call failed", e);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new NGSignClientException("NGSign API call was interrupted", e);
		}
	}

	private void ensureSuccess(final int statusCode, final String body) {
		if (statusCode < HTTP_OK || statusCode >= HTTP_MULTIPLE_CHOICES) {
			throw new NGSignClientException(
					"NGSign API returned HTTP " + statusCode + ": " + body);
		}
	}

	private String writeJson(final JsonNode node) {
		try {
			return objectMapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			throw new NGSignClientException("Failed to serialise request body", e);
		}
	}

	private JsonNode readTree(final String json) {
		try {
			return objectMapper.readTree(json);
		} catch (final JsonProcessingException e) {
			throw new NGSignClientException("Failed to parse NGSign response", e);
		}
	}

	private static String launchPath(final String transactionId) {
		return "/server/protected/transaction/" + transactionId + "/launch";
	}

	private static String transactionPath(final String transactionId) {
		return "/server/any/transaction/" + transactionId;
	}

	private static String documentPath(final String transactionId, final String documentId) {
		return "/server/any/transaction/" + transactionId + "/pdfs/" + documentId;
	}

	private static String stripTrailingSlash(final String url) {
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}

	/**
	 * Fluent builder for {@link NGSignClient}.
	 */
	public static final class Builder {

		private String baseUrl;
		private String apiToken;
		private Duration requestTimeout = Duration.ofMinutes(5);
		private HttpClient httpClient;

		private Builder() {
		}

		/**
		 * Sets the NGSign server base URL, for example {@code https://sandbox.ng-sign.com}.
		 *
		 * @param value the base URL
		 * @return this builder
		 */
		public Builder baseUrl(final String value) {
			this.baseUrl = value;
			return this;
		}

		/**
		 * Sets the API bearer token.
		 *
		 * @param value the API token
		 * @return this builder
		 */
		public Builder apiToken(final String value) {
			this.apiToken = value;
			return this;
		}

		/**
		 * Sets the per-request timeout (default five minutes). The NGSign sandbox can be
		 * slow, so keep this generous.
		 *
		 * @param value the request timeout
		 * @return this builder
		 */
		public Builder requestTimeout(final Duration value) {
			this.requestTimeout = value;
			return this;
		}

		/**
		 * Supplies a preconfigured {@link HttpClient} (proxy, TLS, executor...). Optional.
		 *
		 * @param value the HTTP client to use
		 * @return this builder
		 */
		public Builder httpClient(final HttpClient value) {
			this.httpClient = value;
			return this;
		}

		/**
		 * Builds the immutable client. The base URL and API token are required.
		 *
		 * @return the configured {@link NGSignClient}
		 */
		public NGSignClient build() {
			Objects.requireNonNull(baseUrl, "baseUrl is required");
			Objects.requireNonNull(apiToken, "apiToken is required");
			return new NGSignClient(this);
		}
	}
}
