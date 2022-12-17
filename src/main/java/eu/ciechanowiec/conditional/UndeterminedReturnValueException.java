package eu.ciechanowiec.conditional;

/**
 * Unchecked exception that indicates that not exactly one action
 * was submitted to a given {@link Conditional} and was bound to
 * the value described by that {@link Conditional}.
 */
public class UndeterminedReturnValueException extends RuntimeException {

    /**
     * Constructs an instance of an {@link UndeterminedReturnValueException}.
     * @param message exception message
     */
    UndeterminedReturnValueException(String message) {
        super(message);
    }
}
