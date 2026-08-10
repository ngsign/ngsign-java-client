package com.ngsign.client.model;

import java.util.List;

/**
 * The identifiers returned once a signature request has been launched.
 *
 * <p>Keep these values: they are required to later poll the status and download the signed
 * documents. A request may cover several documents, hence
 * {@link #documentIdentifiers()}; {@link #documentIdentifier()} is a shortcut for the
 * first one.</p>
 */
public final class SignatureResult {

	private final String transactionId;
	private final List<String> documentIdentifiers;

	/**
	 * Creates a single-document signature result.
	 *
	 * @param transactionId      the NGSign transaction id (its {@code uuid})
	 * @param documentIdentifier the identifier of the uploaded document
	 */
	public SignatureResult(final String transactionId, final String documentIdentifier) {
		this(transactionId, List.of(documentIdentifier));
	}

	/**
	 * Creates a signature result covering one or more documents.
	 *
	 * @param transactionId       the NGSign transaction id (its {@code uuid})
	 * @param documentIdentifiers the identifiers of the uploaded documents, in upload order
	 */
	public SignatureResult(final String transactionId,
			final List<String> documentIdentifiers) {
		this.transactionId = transactionId;
		this.documentIdentifiers = List.copyOf(documentIdentifiers);
	}

	/**
	 * Returns the transaction id.
	 *
	 * @return the transaction id
	 */
	public String transactionId() {
		return transactionId;
	}

	/**
	 * Returns the first document identifier (convenience for single-document requests).
	 *
	 * @return the first document identifier
	 */
	public String documentIdentifier() {
		return documentIdentifiers.get(0);
	}

	/**
	 * Returns all document identifiers, in upload order.
	 *
	 * @return the immutable list of document identifiers
	 */
	public List<String> documentIdentifiers() {
		return documentIdentifiers;
	}
}
