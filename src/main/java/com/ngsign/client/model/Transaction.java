package com.ngsign.client.model;

/**
 * A snapshot of an NGSign transaction, as returned by the status endpoint.
 */
public final class Transaction {

	private final String id;
	private final TransactionStatus status;
	private final String rawStatus;

	/**
	 * Creates a transaction snapshot.
	 *
	 * @param id        the transaction id ({@code uuid})
	 * @param status    the parsed lifecycle status
	 * @param rawStatus the raw status string returned by the API, useful when the parsed
	 *                  status is {@link TransactionStatus#UNKNOWN}
	 */
	public Transaction(final String id, final TransactionStatus status,
			final String rawStatus) {
		this.id = id;
		this.status = status;
		this.rawStatus = rawStatus;
	}

	/**
	 * Returns the transaction id.
	 *
	 * @return the transaction id
	 */
	public String id() {
		return id;
	}

	/**
	 * Returns the parsed status.
	 *
	 * @return the parsed status
	 */
	public TransactionStatus status() {
		return status;
	}

	/**
	 * Returns the raw status string returned by the API.
	 *
	 * @return the raw status string
	 */
	public String rawStatus() {
		return rawStatus;
	}

	/**
	 * Convenience accessor telling whether the transaction has been fully signed.
	 *
	 * @return {@code true} if the status is {@link TransactionStatus#SIGNED}
	 */
	public boolean isSigned() {
		return status == TransactionStatus.SIGNED;
	}
}
