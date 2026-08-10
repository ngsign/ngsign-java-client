package com.ngsign.client.model;

/**
 * A person invited to sign a document.
 */
public final class Signer {

	private final String firstName;
	private final String lastName;
	private final String email;
	private final String phoneNumber;

	/**
	 * Creates a signer.
	 *
	 * @param firstName   the signer's first name
	 * @param lastName    the signer's last name
	 * @param email       the signer's e-mail address (used for {@code BY_MAIL} invitations)
	 * @param phoneNumber the signer's phone number, or {@code null} if not provided
	 */
	public Signer(final String firstName, final String lastName, final String email,
			final String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Returns the first name.
	 *
	 * @return the first name
	 */
	public String firstName() {
		return firstName;
	}

	/**
	 * Returns the last name.
	 *
	 * @return the last name
	 */
	public String lastName() {
		return lastName;
	}

	/**
	 * Returns the e-mail address.
	 *
	 * @return the e-mail address
	 */
	public String email() {
		return email;
	}

	/**
	 * Returns the phone number.
	 *
	 * @return the phone number, or {@code null}
	 */
	public String phoneNumber() {
		return phoneNumber;
	}
}
