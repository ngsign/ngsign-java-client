package com.ngsign.client.model;

/**
 * The kind of signature applied to the document.
 *
 * <p>The enum name is the exact value expected by the NGSign API. When in doubt,
 * {@link #CERTIFIED_TIMESTAMP} is a safe default.</p>
 */
public enum SignatureType {

	/** Signature backed by a Secure Signature Creation Device. */
	SIGNATURE_WITH_SSCD,

	/** Certified signature sealed with a trusted timestamp. */
	CERTIFIED_TIMESTAMP,

	/** Certified timestamped signature captured with a Wacom device. */
	CERTIFIED_TIMESTAMP_WACOM,

	/** Signature relying on an NGCert certificate. */
	NGCERT,

	/** Signature performed through the DigiGo channel. */
	DIGI_GO,

	/** The signature type is chosen later by the signer. */
	LATER,

	/** Signature performed with Mobile ID. */
	MOBILE_ID,

	/** Signature performed through the Trusted X channel. */
	TRUSTED_X
}
