package com.ngsign.client.model;

/**
 * One-time-password (OTP) channel used to authenticate the signer.
 *
 * <p>The enum name is the exact value expected by the NGSign API.</p>
 *
 * @author NGSign R&amp;D (with Claude support)
 */
public enum OtpChoice {

	/** The OTP is sent to the signer by e-mail. */
	EMAIL,

	/** The OTP is sent through the default OTP channel (typically SMS). */
	OTP,

	/** No OTP is required from the signer. */
	NONE
}
