package com.ngsign.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class NGSignClientTest {

	private static final String UPLOAD_RESPONSE =
			"{\"object\":{\"uuid\":\"TX-1\",\"pdfs\":[{\"identifier\":\"DOC-1\"}]}}";
	private static final String STATUS_RESPONSE =
			"{\"object\":{\"uuid\":\"TX-1\",\"status\":\"SIGNED\"}}";

	private HttpServer server;
	private NGSignClient client;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", exchange -> {
			final String path = exchange.getRequestURI().getPath();
			final String body;
			if (path.endsWith("/pdfs")) {
				body = UPLOAD_RESPONSE;
			} else if (path.endsWith("/launch")) {
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
}
