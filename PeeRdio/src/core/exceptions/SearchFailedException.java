package core.exceptions;

public class SearchFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	private Exception innerException;

	public SearchFailedException(String message) {
		super(message);
	}
	
	public SearchFailedException(String message, Exception innerException) {
		super(message);
		this.innerException = innerException;
	}
	
	public Exception getInnerException() {
		return innerException;
	}


}

