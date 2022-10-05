package eu.ciechanowiec.conditional;

/**
 * Unchecked exception that is used to wrap a checked {@link Exception}.
 */
public class WrapperException extends RuntimeException {

    /**
     * Specific details about this exception.
     */
    private final String message;

    /**
     * Constructs an instance of a {@link WrapperException}.
     * @param wrappedException exception that will be wrapped by a created
     * instance of a {@link WrapperException} as its cause
     */
    WrapperException(Exception wrappedException) {
        Class<? extends Exception> wrappedExceptionClass = wrappedException.getClass();
        String wrappedExceptionName = wrappedExceptionClass.getName();
        String wrappedExceptionMessage = wrappedException.getMessage();
        message = String.format("Wrapped exception: '%s'. Wrapped exception message: '%s'. " +
                                "See the cause exception for details",
                                wrappedExceptionName, wrappedExceptionMessage);
        this.initCause(wrappedException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return message;
    }
}
