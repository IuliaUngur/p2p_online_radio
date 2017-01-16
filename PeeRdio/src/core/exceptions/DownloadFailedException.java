package core.exceptions;

public class DownloadFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	private Exception innerException;

	public DownloadFailedException(String message) {
		super(message);
	}
	
	public DownloadFailedException(String message, Exception innerException) {
		super(message);
		this.innerException = innerException;
	}
	
	public Exception getInnerException() {
		return innerException;
	}


}
