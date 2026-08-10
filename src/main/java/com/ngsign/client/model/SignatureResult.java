package com.ngsign.client.model;

/**
 * The identifiers returned once a signature request has been launched.
 *
 * <p>Keep both values: they are required to later poll the status and download the
 * signed document.</p>
 */
public final class SignatureResult {

	private final String transactionId;
	private final String documentIdentifier;

	/**
	 * Creates a signature result.
	 *
	 * @param transactionId      the NGSign transaction id (its {@code uuid})
	 * @param documentIdentifier the identifier of the uploaded document
	 */
	public SignatureResult(final String transactionId, final String documentIdentifier) {
		this.transactionId = transactionId;
		this.documentIdentifier = documentIdentifier;
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
	 * Returns the document identifier.
	 *
	 * @return the document identifier
	 */
	public String documentIdentifier() {
		return documentIdentifier;
	}
}
