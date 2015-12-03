package Beerculator;

public class NegativeBACException extends Exception {
	/**
	 * Exception if the BAC value is < 0
	 */
	private static final long serialVersionUID = 1L;

	public NegativeBACException() {
		System.out.println("The BAC value is negative, there must be a mistake!");
	}
}
