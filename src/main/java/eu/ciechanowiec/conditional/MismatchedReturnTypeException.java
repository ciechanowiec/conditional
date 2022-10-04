package eu.ciechanowiec.conditional;

/**
 * Unchecked exception that indicates mismatch between
 * an actual return type and a demanded return type.
 */
public class MismatchedReturnTypeException extends RuntimeException {

    /**
     * Specific details about this exception.
     */
    private final String message;


    /**
     * Constructs an instance of a {@link MismatchedReturnTypeException}
     * with a template message.
     * @param actualReturnTypeAsString - actual return type represented as string
     * @param demandedReturnTypeAsString  - demanded return type represented as string
     */
    MismatchedReturnTypeException(String actualReturnTypeAsString,
                                  String demandedReturnTypeAsString) {
        message = String.format("Actual type: '%s'. Demanded type: '%s'",
                                actualReturnTypeAsString, demandedReturnTypeAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return message;
    }
}
