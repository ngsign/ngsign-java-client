package com.ngsign.client.model;

import java.util.Objects;

/**
 * An immutable description of a document to send for signature.
 *
 * <p>Build instances with the {@linkplain #builder() builder}, which applies sensible
 * defaults ({@code BY_MAIL}, no OTP, certified timestamp, signer places the signature).</p>
 */
public final class SignatureRequest {

	private final String fileName;
	private final byte[] pdfContent;
	private final Signer signer;
	private final SignatureMode mode;
	private final OtpChoice otp;
	private final SignatureType signatureType;
	private final boolean chooseSignaturePosition;
	private final SignaturePosition position;

	private SignatureRequest(final String fileName, final byte[] pdfContent,
			final Signer signer, final SignatureMode mode, final OtpChoice otp,
			final SignatureType signatureType, final boolean chooseSignaturePosition,
			final SignaturePosition position) {
		this.fileName = fileName;
		this.pdfContent = pdfContent;
		this.signer = signer;
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
	 * Returns the document file name.
	 *
	 * @return the file name, without extension
	 */
	public String fileName() {
		return fileName;
	}

	/**
	 * Returns the raw PDF bytes to sign.
	 *
	 * @return the PDF content
	 */
	public byte[] pdfContent() {
		return pdfContent;
	}

	/**
	 * Returns the signer.
	 *
	 * @return the signer
	 */
	public Signer signer() {
		return signer;
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
	 * Returns the fixed signature position.
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

		private String fileName;
		private byte[] pdfContent;
		private Signer signer;
		private SignatureMode mode = SignatureMode.BY_MAIL;
		private OtpChoice otp = OtpChoice.NONE;
		private SignatureType signatureType = SignatureType.CERTIFIED_TIMESTAMP;
		private boolean chooseSignaturePosition = true;
		private SignaturePosition position;

		private Builder() {
		}

		/**
		 * Sets the document file name (without extension).
		 *
		 * @param value the file name
		 * @return this builder
		 */
		public Builder fileName(final String value) {
			this.fileName = value;
			return this;
		}

		/**
		 * Sets the raw PDF bytes to sign.
		 *
		 * @param value the PDF content
		 * @return this builder
		 */
		public Builder pdfContent(final byte[] value) {
			this.pdfContent = value;
			return this;
		}

		/**
		 * Sets the signer.
		 *
		 * @param value the signer
		 * @return this builder
		 */
		public Builder signer(final Signer value) {
			this.signer = value;
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
		 * Sets a fixed signature position and disables interactive placement.
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
		 * Builds the immutable request. The file name, PDF content and signer are required.
		 *
		 * @return the built {@link SignatureRequest}
		 */
		public SignatureRequest build() {
			Objects.requireNonNull(fileName, "fileName is required");
			Objects.requireNonNull(pdfContent, "pdfContent is required");
			Objects.requireNonNull(signer, "signer is required");
			return new SignatureRequest(fileName, pdfContent, signer, mode, otp,
					signatureType, chooseSignaturePosition, position);
		}
	}
}
