package com.ngsign.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable description of one or more documents to send for signature.
 *
 * <p>Build instances with the {@linkplain #builder() builder}, which applies sensible
 * defaults ({@code BY_MAIL}, no OTP, certified timestamp, signer places the signature).</p>
 *
 * <p>Supports a single or several {@linkplain Document documents} and a single or several
 * {@linkplain Signer signers}. Every signer signs every document; the invitation mode, OTP
 * channel and signature type apply to all of them.</p>
 *
 * @author NGSign R&amp;D (with Claude support)
 */
public final class SignatureRequest {

	private final List<Document> documents;
	private final List<Signer> signers;
	private final SignatureMode mode;
	private final OtpChoice otp;
	private final SignatureType signatureType;
	private final boolean chooseSignaturePosition;
	private final SignaturePosition position;

	private SignatureRequest(final List<Document> documents, final List<Signer> signers,
			final SignatureMode mode, final OtpChoice otp,
			final SignatureType signatureType, final boolean chooseSignaturePosition,
			final SignaturePosition position) {
		this.documents = List.copyOf(documents);
		this.signers = List.copyOf(signers);
		this.mode = mode;
		this.otp = otp;
		this.signatureType = signatureType;
		this.chooseSignaturePosition = chooseSignaturePosition;
		this.position = position;
	}

	/**
	 * Creates a new builder.
	 *
	 * @return a fresh {@link Builder} with default values applied
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the documents to sign.
	 *
	 * @return the immutable list of documents (never empty)
	 */
	public List<Document> documents() {
		return documents;
	}

	/**
	 * Returns the signers.
	 *
	 * @return the immutable list of signers (never empty)
	 */
	public List<Signer> signers() {
		return signers;
	}

	/**
	 * Returns the first document file name (convenience for single-document requests).
	 *
	 * @return the file name of the first document, without extension
	 */
	public String fileName() {
		return documents.get(0).fileName();
	}

	/**
	 * Returns the first document's PDF bytes (convenience for single-document requests).
	 *
	 * @return the PDF content of the first document
	 */
	public byte[] pdfContent() {
		return documents.get(0).content();
	}

	/**
	 * Returns the first signer (convenience for single-signer requests).
	 *
	 * @return the first signer
	 */
	public Signer signer() {
		return signers.get(0);
	}

	/**
	 * Returns the invitation mode.
	 *
	 * @return the mode
	 */
	public SignatureMode mode() {
		return mode;
	}

	/**
	 * Returns the OTP channel.
	 *
	 * @return the OTP choice
	 */
	public OtpChoice otp() {
		return otp;
	}

	/**
	 * Returns the signature type.
	 *
	 * @return the signature type
	 */
	public SignatureType signatureType() {
		return signatureType;
	}

	/**
	 * Tells whether the signer places the signature interactively.
	 *
	 * @return {@code true} for interactive placement, {@code false} for a fixed position
	 */
	public boolean chooseSignaturePosition() {
		return chooseSignaturePosition;
	}

	/**
	 * Returns the default fixed signature position (used for documents without their own).
	 *
	 * @return the fixed position, or {@code null} when placement is interactive
	 */
	public SignaturePosition position() {
		return position;
	}

	/**
	 * Fluent builder for {@link SignatureRequest}.
	 */
	public static final class Builder {

		private final List<Document> documents = new ArrayList<>();
		private final List<Signer> signers = new ArrayList<>();
		private String fileName;
		private byte[] pdfContent;
		private SignatureMode mode = SignatureMode.BY_MAIL;
		private OtpChoice otp = OtpChoice.NONE;
		private SignatureType signatureType = SignatureType.CERTIFIED_TIMESTAMP;
		private boolean chooseSignaturePosition = true;
		private SignaturePosition position;

		private Builder() {
		}

		/**
		 * Sets the single document file name (without extension). Convenience paired with
		 * {@link #pdfContent(byte[])}; for many, use {@link #document(Document)}.
		 *
		 * @param value the file name
		 * @return this builder
		 */
		public Builder fileName(final String value) {
			this.fileName = value;
			return this;
		}

		/**
		 * Sets the single document PDF bytes. Convenience paired with
		 * {@link #fileName(String)}; for several documents use {@link #document(Document)}.
		 *
		 * @param value the PDF content
		 * @return this builder
		 */
		public Builder pdfContent(final byte[] value) {
			this.pdfContent = value;
			return this;
		}

		/**
		 * Adds a document to sign. Call several times for multiple documents.
		 *
		 * @param value the document
		 * @return this builder
		 */
		public Builder document(final Document value) {
			this.documents.add(Objects.requireNonNull(value, "document is required"));
			return this;
		}

		/**
		 * Adds several documents to sign.
		 *
		 * @param values the documents
		 * @return this builder
		 */
		public Builder documents(final List<Document> values) {
			Objects.requireNonNull(values, "documents is required");
			for (final Document value : values) {
				document(value);
			}
			return this;
		}

		/**
		 * Adds a signer. Call several times for multiple signers.
		 *
		 * @param value the signer
		 * @return this builder
		 */
		public Builder signer(final Signer value) {
			this.signers.add(Objects.requireNonNull(value, "signer is required"));
			return this;
		}

		/**
		 * Adds several signers.
		 *
		 * @param values the signers
		 * @return this builder
		 */
		public Builder signers(final List<Signer> values) {
			Objects.requireNonNull(values, "signers is required");
			for (final Signer value : values) {
				signer(value);
			}
			return this;
		}

		/**
		 * Sets the invitation mode (default {@code BY_MAIL}).
		 *
		 * @param value the mode
		 * @return this builder
		 */
		public Builder mode(final SignatureMode value) {
			this.mode = value;
			return this;
		}

		/**
		 * Sets the OTP channel (default {@code NONE}).
		 *
		 * @param value the OTP choice
		 * @return this builder
		 */
		public Builder otp(final OtpChoice value) {
			this.otp = value;
			return this;
		}

		/**
		 * Sets the signature type (default {@code CERTIFIED_TIMESTAMP}).
		 *
		 * @param value the signature type
		 * @return this builder
		 */
		public Builder signatureType(final SignatureType value) {
			this.signatureType = value;
			return this;
		}

		/**
		 * Sets a default fixed signature position and disables interactive placement.
		 * Documents carrying their own position override this one.
		 *
		 * @param value the fixed position
		 * @return this builder
		 */
		public Builder position(final SignaturePosition value) {
			this.position = value;
			this.chooseSignaturePosition = value == null;
			return this;
		}

		/**
		 * Builds the immutable request. At least one document and one signer are required.
		 *
		 * @return the built {@link SignatureRequest}
		 */
		public SignatureRequest build() {
			if (fileName != null || pdfContent != null) {
				Objects.requireNonNull(fileName, "fileName is required");
				Objects.requireNonNull(pdfContent, "pdfContent is required");
				documents.add(0, Document.of(fileName, pdfContent));
			}
			if (documents.isEmpty()) {
				throw new NullPointerException("at least one document is required");
			}
			if (signers.isEmpty()) {
				throw new NullPointerException("at least one signer is required");
			}
			return new SignatureRequest(documents, signers, mode, otp, signatureType,
					chooseSignaturePosition, position);
		}
	}
}
