package com.ngsign.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngsign.client.model.Document;
import com.ngsign.client.model.SignatureRequest;
import com.ngsign.client.model.SignatureResult;
import com.ngsign.client.model.Signer;
import com.ngsign.client.model.Transaction;
import com.ngsign.client.model.TransactionStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * NGSign Java client source file.
 *
 * @author NGSign R&amp;D (with Claude support)
 */
class NGSignClientTest {

	private static final String STATUS_RESPONSE =
			"{\"object\":{\"uuid\":\"TX-1\",\"status\":\"SIGNED\"}}";

	private final ObjectMapper mapper = new ObjectMapper();
	private HttpServer server;
	private NGSignClient client;
	private volatile String lastLaunchBody;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", exchange -> {
			final String path = exchange.getRequestURI().getPath();
			final byte[] requestBody = exchange.getRequestBody().readAllBytes();
			final String body;
			if (path.endsWith("/pdfs")) {
				body = uploadResponse(requestBody);
			} else if (path.endsWith("/launch")) {
				lastLaunchBody = new String(requestBody, StandardCharsets.UTF_8);
				body = "{}";
			} else {
				body = STATUS_RESPONSE;
			}
			final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, bytes.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(bytes);
			}
		});
		server.start();
		client = NGSignClient.builder()
				.baseUrl("http://localhost:" + server.getAddress().getPort())
				.apiToken("test-token")
				.build();
	}

	private String uploadResponse(final byte[] requestBody) throws IOException {
		final JsonNode files = mapper.readTree(requestBody);
		final StringBuilder pdfs = new StringBuilder();
		for (int i = 0; i < files.size(); i++) {
			if (i > 0) {
				pdfs.append(',');
			}
			pdfs.append("{\"identifier\":\"DOC-").append(i + 1).append("\"}");
		}
		return "{\"object\":{\"uuid\":\"TX-1\",\"pdfs\":[" + pdfs + "]}}";
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void requestSignatureReturnsIdentifiers() {
		final SignatureResult result = client.requestSignature(SignatureRequest.builder()
				.fileName("contract")
				.pdfContent("pdf".getBytes(StandardCharsets.UTF_8))
				.signer(new Signer("Jane", "Doe", "jane@example.com", null))
				.build());
		assertEquals("TX-1", result.transactionId());
		assertEquals("DOC-1", result.documentIdentifier());
	}

	@Test
	void requestSignatureSupportsMultipleDocumentsAndSigners() throws IOException {
		final byte[] pdf = "pdf".getBytes(StandardCharsets.UTF_8);
		final SignatureResult result = client.requestSignature(SignatureRequest.builder()
				.document(Document.of("doc-a", pdf))
				.document(Document.of("doc-b", pdf))
				.signer(new Signer("Jane", "Doe", "jane@example.com", null))
				.signer(new Signer("John", "Roe", "john@example.com", null))
				.build());

		assertEquals(2, result.documentIdentifiers().size());
		assertEquals("DOC-1", result.documentIdentifiers().get(0));
		assertEquals("DOC-2", result.documentIdentifiers().get(1));

		final JsonNode sigConf = mapper.readTree(lastLaunchBody).path("sigConf");
		assertEquals(2, sigConf.size());
		assertEquals(2, sigConf.path(0).path("docsConfigs").size());
		assertEquals("john@example.com",
				sigConf.path(1).path("signer").path("email").asText());
	}

	@Test
	void getTransactionParsesStatus() {
		final Transaction tx = client.getTransaction("TX-1");
		assertEquals(TransactionStatus.SIGNED, tx.status());
		assertTrue(tx.isSigned());
	}

	@Test
	void transactionStatusFallsBackToUnknown() {
		assertEquals(TransactionStatus.UNKNOWN, TransactionStatus.fromApiValue("WEIRD"));
		assertEquals(TransactionStatus.UNKNOWN, TransactionStatus.fromApiValue(null));
	}

	@Test
	void builderRequiresApiToken() {
		assertThrows(NullPointerException.class,
				() -> NGSignClient.builder().baseUrl("http://localhost").build());
	}

	@Test
	void builderRequiresAtLeastOneSigner() {
		assertThrows(NullPointerException.class,
				() -> SignatureRequest.builder()
						.fileName("contract")
						.pdfContent("pdf".getBytes(StandardCharsets.UTF_8))
						.build());
	}
}
