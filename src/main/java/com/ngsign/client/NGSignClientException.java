package com.ngsign.client;

/**
 * Unchecked exception thrown when a call to the NGSign API fails.
 *
 * <p>It wraps transport errors (I/O, interruption), non-2xx HTTP responses and
 * unexpected payloads.</p>
 *
 * @author NGSign R&amp;D (with Claude support)
 */
public class NGSignClientException extends RuntimeException {

	/**
	 * Creates a new exception with a detail message.
	 *
	 * @param message the detail message
	 */
	public NGSignClientException(final String message) {
		super(message);
	}

	/**
	 * Creates a new exception with a detail message and an underlying cause.
	 *
	 * @param message the detail message
	 * @param cause   the underlying cause
	 */
	public NGSignClientException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
