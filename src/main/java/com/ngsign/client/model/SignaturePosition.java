package com.ngsign.client.model;

/**
 * A fixed position for the signature stamp on a document.
 *
 * <p>Only used when the signer is not allowed to place the signature interactively.</p>
 */
public final class SignaturePosition {

	private final int page;
	private final double xAxis;
	private final double yAxis;

	/**
	 * Creates a fixed signature position.
	 *
	 * @param page  the 1-based page number the signature is placed on
	 * @param xAxis the horizontal offset, in PDF points
	 * @param yAxis the vertical offset, in PDF points
	 */
	public SignaturePosition(final int page, final double xAxis, final double yAxis) {
		this.page = page;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}

	/**
	 * Returns the page number.
	 *
	 * @return the 1-based page number
	 */
	public int page() {
		return page;
	}

	/**
	 * Returns the horizontal offset.
	 *
	 * @return the horizontal offset, in PDF points
	 */
	public double xAxis() {
		return xAxis;
	}

	/**
	 * Returns the vertical offset.
	 *
	 * @return the vertical offset, in PDF points
	 */
	public double yAxis() {
		return yAxis;
	}
}
