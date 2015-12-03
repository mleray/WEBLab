package Beerculator;

public class GenderException extends Exception {
	/**
	 * Exception if the gender of the user is neither male or female
	 */
	private static final long serialVersionUID = 1L;

	public GenderException() {
		System.out.println("This gender does not exist!");
	}
}
