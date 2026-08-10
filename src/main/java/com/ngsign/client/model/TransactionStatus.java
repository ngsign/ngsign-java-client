package com.ngsign.client.model;

/**
 * Lifecycle status of an NGSign transaction, as returned by the API.
 *
 * @author NGSign R&amp;D (with Claude support)
 */
public enum TransactionStatus {

	/** The transaction has been created but not configured yet. */
	CREATED,

	/** Signers have been configured; the transaction is awaiting launch/signature. */
	CONFIGURED,

	/** The signature process has been launched and is in progress. */
	SIGNATURE_LAUNCHED,

	/** All required signatures have been applied. */
	SIGNED,

	/** The transaction has been cancelled. */
	CANCELLED,

	/** A signer refused to sign. */
	REFUSED,

	/** The transaction expired before being completed. */
	EXPIRED,

	/** The status returned by the API is not known to this client version. */
	UNKNOWN;

	/**
	 * Maps a raw status value returned by the API to an enum constant.
	 *
	 * @param value the raw status string (may be {@code null})
	 * @return the matching status, or {@link #UNKNOWN} if it is not recognised
	 */
	public static TransactionStatus fromApiValue(final String value) {
		if (value == null) {
			return UNKNOWN;
		}
		for (final TransactionStatus status : values()) {
			if (status != UNKNOWN && status.name().equalsIgnoreCase(value.trim())) {
				return status;
			}
		}
		return UNKNOWN;
	}

	/**
	 * Tells whether this status is terminal (no further change is expected).
	 *
	 * @return {@code true} for {@code SIGNED}, {@code CANCELLED}, {@code REFUSED}
	 *         or {@code EXPIRED}
	 */
	public boolean isFinal() {
		return this == SIGNED || this == CANCELLED || this == REFUSED || this == EXPIRED;
	}
}
