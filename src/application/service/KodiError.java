package application.service;

public class KodiError {

	private final int errorCode;
	private final String description;

	public KodiError(final int errorCode, final String description) {
		this.errorCode = errorCode;
		this.description = description;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getDescription() {
		return description;
	}



}
