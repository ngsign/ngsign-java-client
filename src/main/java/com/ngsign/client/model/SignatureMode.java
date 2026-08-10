package com.ngsign.client.model;

/**
 * How a signer is invited to sign a transaction.
 *
 * <p>The enum name is the exact value expected by the NGSign API.</p>
 */
public enum SignatureMode {

	/** The signer receives an e-mail invitation with a link to sign. */
	BY_MAIL,

	/** The signer signs in person, on the initiator's device. */
	FACE_TO_FACE,

	/** The signature is applied automatically, without human interaction. */
	AUTOMATIC,

	/** A signing link is returned by the API instead of an e-mail being sent. */
	BY_LINK
}
