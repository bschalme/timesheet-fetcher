package timesheet.fetcher.exception;

public class MissingJobCodeXrefException extends RuntimeException {

    private static final long serialVersionUID = -6161904972801501449L;

    public MissingJobCodeXrefException() {
    }

    public MissingJobCodeXrefException(String message) {
        super(message);
    }

    public MissingJobCodeXrefException(Throwable cause) {
        super(cause);
    }

    public MissingJobCodeXrefException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingJobCodeXrefException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
