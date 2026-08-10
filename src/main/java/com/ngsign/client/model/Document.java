package com.ngsign.client.model;

import java.util.Objects;

/**
 * An immutable PDF document to send for signature.
 *
 * <p>Use {@link #of(String, byte[])} to let the signer place the signature interactively,
 * or {@link #of(String, byte[], SignaturePosition)} to pin it on this document.</p>
 */
public final class Document {

	private final String fileName;
	private final byte[] content;
	private final SignaturePosition position;

	/**
	 * Creates a document.
	 *
	 * @param fileName the file name, without extension
	 * @param content  the raw PDF bytes
	 * @param position a fixed signature position for this document, or {@code null}
	 */
	public Document(final String fileName, final byte[] content,
			final SignaturePosition position) {
		this.fileName = Objects.requireNonNull(fileName, "fileName is required");
		this.content = Objects.requireNonNull(content, "content is required");
		this.position = position;
	}

	/**
	 * Creates a document whose signature is placed interactively by the signer.
	 *
	 * @param fileName the file name, without extension
	 * @param content  the raw PDF bytes
	 * @return the document
	 */
	public static Document of(final String fileName, final byte[] content) {
		return new Document(fileName, content, null);
	}

	/**
	 * Creates a document with a fixed signature position.
	 *
	 * @param fileName the file name, without extension
	 * @param content  the raw PDF bytes
	 * @param position the fixed signature position
	 * @return the document
	 */
	public static Document of(final String fileName, final byte[] content,
			final SignaturePosition position) {
		return new Document(fileName, content, position);
	}

	/**
	 * Returns the file name.
	 *
	 * @return the file name, without extension
	 */
	public String fileName() {
		return fileName;
	}

	/**
	 * Returns the raw PDF bytes.
	 *
	 * @return the PDF content
	 */
	public byte[] content() {
		return content;
	}

	/**
	 * Returns the per-document fixed signature position.
	 *
	 * @return the fixed position, or {@code null} for interactive placement
	 */
	public SignaturePosition position() {
		return position;
	}
}
