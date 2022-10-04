package eu.ciechanowiec.conditional;

/**
 * Unchecked exception that is used to wrap a checked {@link Exception}.
 */
public class WrapperException extends RuntimeException {

    /**
     * Constructs an instance of a {@link WrapperException}.
     * @param wrappedException exception that will be wrapped by a created
     * instance of {@link WrapperException} as its cause
     */
    WrapperException(Exception wrappedException) {
        super("This exception wraps another exception. See the cause exception for details", wrappedException);
    }
}
