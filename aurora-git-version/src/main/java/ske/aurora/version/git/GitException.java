package ske.aurora.version.git;

public class GitException extends RuntimeException {
    public GitException() {
    }

    public GitException(String message) {
        super(message);
    }

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitException(Throwable cause) {
        super(cause);
    }

    public GitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
